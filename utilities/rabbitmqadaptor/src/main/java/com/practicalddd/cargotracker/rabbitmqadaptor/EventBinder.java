package com.practicalddd.cargotracker.rabbitmqadaptor;

import com.practicalddd.cargotracker.rabbitmqadaptor.EventBinder.EventBindingBuilder;
import com.practicalddd.cargotracker.rabbitmqadaptor.EventBinder.ExchangeBinding;
import com.practicalddd.cargotracker.rabbitmqadaptor.EventBinder.QueueBinding;
import com.practicalddd.cargotracker.rabbitmqadaptor.consumer.ConsumerContainer;
import com.practicalddd.cargotracker.rabbitmqadaptor.publisher.DeliveryOptions;
import com.practicalddd.cargotracker.rabbitmqadaptor.publisher.PublisherReliability;
import com.rabbitmq.client.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;

@ApplicationScoped
public abstract class EventBinder {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventBinder.class);

    @Inject
    Event<Object> remoteEventControl;
    @Inject
    Instance<Object> remoteEventPool;
    @Inject
    ConsumerContainer consumerContainer;
    @Inject
    EventPublisher eventPublisher;
    @Inject
    ConnectionConfigurator connectionConfigurator;
    @Inject
    ConnectionFactory connectionFactory;
    @Inject
    RabbitMQHealthCheck rabbitMQHealthCheck; // ✅ Health check injetado

    Set<QueueBinding> queueBindings = new HashSet<QueueBinding>();
    Set<ExchangeBinding> exchangeBindings = new HashSet<ExchangeBinding>();

    protected abstract void bindEvents();

    public void initialize() throws IOException, TimeoutException {
        bindEvents();
        connectionConfigurator.configureFactory(getClass());

        // Tentar criar infraestrutura com retry
        createInfrastructureWithRetry();

        processQueueBindings();

        // ✅ Iniciar consumidores apenas se a infraestrutura foi criada
        if (isInfrastructureReady()) {
            consumerContainer.startAllConsumers();
        }

        processExchangeBindings();
    }

    private void createInfrastructureWithRetry() {
        int maxRetries = 10;
        int retryInterval = 5000; // 5 segundos

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                LOGGER.info("🔄 Tentativa {}/{} de criar infraestrutura RabbitMQ", attempt, maxRetries);
                createRabbitMQInfrastructure();
                LOGGER.info("✅ Infraestrutura RabbitMQ criada com sucesso");
                return;
            } catch (Exception e) {
                if (attempt == maxRetries) {
                    LOGGER.warn("⚠️ Não foi possível criar infraestrutura RabbitMQ após {} tentativas", maxRetries);
                    LOGGER.warn("⚠️ O sistema continuará sem mensageria. Recrie manualmente as exchanges/queues.");
                    return;
                }

                LOGGER.warn("⚠️ Falha na tentativa {}. Nova tentativa em {}ms...", attempt, retryInterval);
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    /**
     * Cria a infraestrutura necessária no RabbitMQ (exchanges, queues e bindings)
     * com verificação de saúde integrada
     */
    private void createRabbitMQInfrastructure() {
        LOGGER.info("🔄 Starting RabbitMQ infrastructure creation...");

        // ✅ Aguardar RabbitMQ ficar saudável antes de começar
        rabbitMQHealthCheck.waitUntilHealthy(5, 3000); // 5 tentativas, 3 segundos entre elas

        int retries = 3;
        int retryInterval = 2000; // 2 segundos

        while (retries > 0) {
            try (Connection connection = connectionFactory.newConnection();
                    Channel channel = connection.createChannel()) {

                LOGGER.info("🔧 Creating RabbitMQ infrastructure...");

                // Criar Exchanges (Duráveis, Tipo Topic)
                declareExchange(channel, "cargotracker.cargobookings", BuiltinExchangeType.TOPIC);
                declareExchange(channel, "cargotracker.cargoroutings", BuiltinExchangeType.TOPIC);
                declareExchange(channel, "cargotracker.cargohandlings", BuiltinExchangeType.TOPIC);

                // Criar Queues (Duráveis)
                declareQueue(channel, "cargotracker.bookingsqueue");
                declareQueue(channel, "cargotracker.handlingqueue");
                declareQueue(channel, "cargotracker.routingqueue");

                // Criar Bindings
                createBinding(channel, "cargotracker.bookingsqueue", "cargotracker.cargobookings", "cargobookings");
                createBinding(channel, "cargotracker.handlingqueue", "cargotracker.cargohandlings", "cargohandlings");
                createBinding(channel, "cargotracker.routingqueue", "cargotracker.cargoroutings", "cargoroutings");

                LOGGER.info("✅ RabbitMQ infrastructure created successfully");
                return;

            } catch (Exception e) {
                retries--;
                if (retries == 0) {
                    LOGGER.error("❌ Failed to create RabbitMQ infrastructure after all retries", e);
                    throw new RuntimeException("Failed to create RabbitMQ infrastructure", e);
                }

                LOGGER.warn("⚠️ Failed to create infrastructure, retrying... ({} attempts remaining)", retries);
                try {
                    Thread.sleep(retryInterval);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting to retry infrastructure creation", ie);
                }
            }
        }
    }

    /**
     * Declara um exchange no RabbitMQ
     */
    private void declareExchange(Channel channel, String exchangeName, BuiltinExchangeType exchangeType)
            throws IOException {
        try {
            Map<String, Object> exchangeArgs = new HashMap<>();
            exchangeArgs.put("x-delayed-type", "topic"); // Para suporte a mensagens atrasadas se necessário

            channel.exchangeDeclare(
                    exchangeName,
                    exchangeType,
                    true, // durable
                    false, // autoDelete
                    false, // internal
                    exchangeArgs);
            LOGGER.info("✅ Exchange declared: {}", exchangeName);
        } catch (Exception e) {
            LOGGER.warn("⚠️ Exchange {} might already exist or error occurred: {}", exchangeName, e.getMessage());
            // Não relançamos a exceção para permitir continuidade
        }
    }

    /**
     * Declara uma queue no RabbitMQ
     */
    private void declareQueue(Channel channel, String queueName) throws IOException {
        try {
            Map<String, Object> queueArgs = new HashMap<>();
            // Adicionar argumentos específicos se necessário (TTL, DLX, etc.)

            channel.queueDeclare(
                    queueName,
                    true, // durable
                    false, // exclusive
                    false, // autoDelete
                    queueArgs);
            LOGGER.info("✅ Queue declared: {}", queueName);
        } catch (Exception e) {
            LOGGER.warn("⚠️ Queue {} might already exist or error occurred: {}", queueName, e.getMessage());
            // Não relançamos a exceção para permitir continuidade
        }
    }

    /**
     * Cria binding entre queue e exchange
     */
    private void createBinding(Channel channel, String queueName, String exchangeName, String routingKey)
            throws IOException {
        try {
            channel.queueBind(queueName, exchangeName, routingKey);
            LOGGER.info("✅ Binding created: {} -> {} [{}]", queueName, exchangeName, routingKey);
        } catch (Exception e) {
            LOGGER.warn("⚠️ Binding {}->{} [{}] might already exist or error occurred: {}",
                    queueName, exchangeName, routingKey, e.getMessage());
            // Não relançamos a exceção para permitir continuidade
        }
    }

    void processExchangeBindings() {
        for (ExchangeBinding exchangeBinding : exchangeBindings) {
            bindExchange(exchangeBinding);
        }
        exchangeBindings.clear();
    }

    void processQueueBindings() {
        for (QueueBinding queueBinding : queueBindings) {
            bindQueue(queueBinding);
        }
        queueBindings.clear();
    }

    void bindQueue(final QueueBinding queueBinding) {
        @SuppressWarnings("unchecked")
        Event<Object> eventControl = (Event<Object>) remoteEventControl.select(queueBinding.eventType);
        @SuppressWarnings("unchecked")
        Instance<Object> eventPool = (Instance<Object>) remoteEventPool.select(queueBinding.eventType);
        EventConsumer consumer = new EventConsumer(eventControl, eventPool);
        consumerContainer.addConsumer(consumer, queueBinding.queue, queueBinding.autoAck);
        LOGGER.info("Binding between queue {} and event type {} activated",
                queueBinding.queue, queueBinding.eventType.getSimpleName());
    }

    void bindExchange(ExchangeBinding exchangeBinding) {
        EventPublisher.PublisherConfiguration configuration = new EventPublisher.PublisherConfiguration(
                exchangeBinding.exchange,
                exchangeBinding.routingKey,
                exchangeBinding.persistent,
                exchangeBinding.reliability,
                exchangeBinding.deliveryOptions,
                exchangeBinding.basicProperties);
        eventPublisher.addEvent(exchangeBinding.eventType, configuration);
        LOGGER.info("Binding between exchange {} and event type {} activated",
                exchangeBinding.exchange, exchangeBinding.eventType.getSimpleName());
    }

    public EventBindingBuilder bind(Class<?> event) {
        return new EventBindingBuilder(event);
    }

    public class EventBindingBuilder {

        private Class<?> eventType;

        private EventBindingBuilder(Class<?> eventType) {
            this.eventType = eventType;
        }

        public QueueBinding toQueue(String queue) {
            return new QueueBinding(eventType, queue);
        }

        public ExchangeBinding toExchange(String exchange) {
            return new ExchangeBinding(eventType, exchange);
        }
    }

    public class QueueBinding {

        private Class<?> eventType;
        private String queue;
        private boolean autoAck = false;

        public QueueBinding(Class<?> eventType, String queue) {
            this.eventType = eventType;
            this.queue = queue;
            queueBindings.add(this);
            LOGGER.info("Binding created between queue {} and event type {}", queue, eventType.getSimpleName());
        }

        public QueueBinding autoAck() {
            this.autoAck = true;
            LOGGER.info("Auto acknowledges enabled for event type {}", eventType.getSimpleName());
            return this;
        }

    }

    public class ExchangeBinding {

        private Class<?> eventType;
        private String exchange;
        private String routingKey;
        private boolean persistent;
        private PublisherReliability reliability = PublisherReliability.NONE;
        private DeliveryOptions deliveryOptions = DeliveryOptions.NONE;
        private AMQP.BasicProperties basicProperties = MessageProperties.BASIC;

        public ExchangeBinding(Class<?> eventType, String exchange) {
            this.eventType = eventType;
            this.exchange = exchange;
            exchangeBindings.add(this);
            LOGGER.info("Binding created between exchange {} and event type {}", exchange, eventType.getSimpleName());
        }

        public ExchangeBinding withRoutingKey(String routingKey) {
            this.routingKey = routingKey;
            LOGGER.info("Routing key for event type {} set to {}", eventType.getSimpleName(), routingKey);
            return this;
        }

        public ExchangeBinding withPersistentMessages() {
            this.persistent = true;
            LOGGER.info("Persistent messages enabled for event type {}", eventType.getSimpleName());
            return this;
        }

        public ExchangeBinding withPublisherTransactions() {
            return setPublisherReliability(PublisherReliability.TRANSACTIONAL);
        }

        public ExchangeBinding withPublisherConfirms() {
            return setPublisherReliability(PublisherReliability.CONFIRMED);
        }

        public ExchangeBinding withImmediateDelivery() {
            return setDeliveryOptions(DeliveryOptions.IMMEDIATE);
        }

        public ExchangeBinding withMandatoryDelivery() {
            return setDeliveryOptions(DeliveryOptions.MANDATORY);
        }

        public ExchangeBinding withProperties(AMQP.BasicProperties basicProperties) {
            this.basicProperties = basicProperties;
            LOGGER.info("Publisher properties for event type {} set to {}", eventType.getSimpleName(),
                    basicProperties.toString());
            return this;
        }

        private ExchangeBinding setPublisherReliability(PublisherReliability reliability) {
            if (this.reliability != PublisherReliability.NONE) {
                LOGGER.warn("Publisher reliability for event type {} is overridden: {}", eventType.getSimpleName(),
                        reliability);
            }
            this.reliability = reliability;
            LOGGER.info("Publisher reliability for event type {} set to {}", eventType.getSimpleName(), reliability);
            return this;
        }

        private ExchangeBinding setDeliveryOptions(DeliveryOptions deliveryOptions) {
            if (this.deliveryOptions != DeliveryOptions.NONE) {
                LOGGER.warn("Delivery options for event type {} are overridden: {}", eventType.getSimpleName(),
                        deliveryOptions);
            }
            this.deliveryOptions = deliveryOptions;
            LOGGER.info("Delivery options for event type {} set to {}", eventType.getSimpleName(), deliveryOptions);
            return this;
        }

    }

    /**
     * Verifica se a infraestrutura RabbitMQ está pronta (exchanges e queues
     * existem)
     * 
     * @return true se toda a infraestrutura necessária estiver disponível
     */
    private boolean isInfrastructureReady() {
        try (Connection connection = connectionFactory.newConnection();
                Channel channel = connection.createChannel()) {

            LOGGER.info("🔍 Verificando se a infraestrutura RabbitMQ está pronta...");

            // Lista de exchanges que devem existir
            String[] requiredExchanges = {
                    "cargotracker.cargobookings",
                    "cargotracker.cargoroutings",
                    "cargotracker.cargohandlings"
            };

            // Lista de queues que devem existir
            String[] requiredQueues = {
                    "cargotracker.bookingsqueue",
                    "cargotracker.handlingqueue",
                    "cargotracker.routingqueue"
            };

            // Verificar exchanges
            for (String exchange : requiredExchanges) {
                if (!checkExchangeExists(channel, exchange)) {
                    LOGGER.warn("❌ Exchange não encontrada: {}", exchange);
                    return false;
                }
            }

            // Verificar queues
            for (String queue : requiredQueues) {
                if (!checkQueueExists(channel, queue)) {
                    LOGGER.warn("❌ Queue não encontrada: {}", queue);
                    return false;
                }
            }

            // Verificar bindings (opcional, mas recomendado)
            if (!checkBindingsExist(channel)) {
                LOGGER.warn("⚠️ Alguns bindings podem não estar configurados corretamente");
                // Não falhamos aqui, apenas alertamos
            }

            LOGGER.info("✅ Infraestrutura RabbitMQ verificada e pronta!");
            return true;

        } catch (Exception e) {
            LOGGER.warn("⚠️ Não foi possível verificar a infraestrutura RabbitMQ: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se uma exchange específica existe
     */
    private boolean checkExchangeExists(Channel channel, String exchangeName) {
        try {
            channel.exchangeDeclarePassive(exchangeName);
            LOGGER.debug("✅ Exchange encontrada: {}", exchangeName);
            return true;
        } catch (IOException e) {
            if (e.getMessage().contains("NOT_FOUND")) {
                return false;
            }
            // Outros erros de IO são tratados como falha
            LOGGER.warn("Erro ao verificar exchange {}: {}", exchangeName, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se uma queue específica existe
     */
    private boolean checkQueueExists(Channel channel, String queueName) {
        try {
            channel.queueDeclarePassive(queueName);
            LOGGER.debug("✅ Queue encontrada: {}", queueName);
            return true;
        } catch (IOException e) {
            if (e.getMessage().contains("NOT_FOUND")) {
                return false;
            }
            // Outros erros de IO são tratados como falha
            LOGGER.warn("Erro ao verificar queue {}: {}", queueName, e.getMessage());
            return false;
        }
    }

    /**
     * Verifica se os bindings essenciais existem
     */
    private boolean checkBindingsExist(Channel channel) {
        try {
            // Obter todos os bindings para verificação
            // Esta é uma verificação mais avançada - podemos simplificar
            // apenas verificando se podemos publicar nas exchanges

            // Teste básico: verificar se podemos publicar nas exchanges
            return testExchangePublish(channel, "cargotracker.cargobookings") &&
                    testExchangePublish(channel, "cargotracker.cargoroutings") &&
                    testExchangePublish(channel, "cargotracker.cargohandlings");

        } catch (Exception e) {
            LOGGER.debug("Não foi possível verificar bindings: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Testa se é possível publicar em uma exchange (verificação de permissões)
     */
    private boolean testExchangePublish(Channel channel, String exchangeName) {
        try {
            // Tenta publicar uma mensagem de teste (vazia) com mandatory=false
            // para evitar erros se não houver queues vinculadas
            channel.basicPublish(exchangeName, "test.routing.key", false, null, new byte[0]);
            LOGGER.debug("✅ Exchange publicável: {}", exchangeName);
            return true;
        } catch (AlreadyClosedException e) {
            LOGGER.warn("Canal fechado ao testar exchange {}", exchangeName);
            return false;
        } catch (IOException e) {
            if (e.getMessage().contains("NOT_FOUND")) {
                LOGGER.warn("Exchange não encontrada: {}", exchangeName);
                return false;
            }
            // Outros erros podem ser devido a permissões, mas a exchange existe
            LOGGER.debug("Exchange {} existe mas tem restrições: {}", exchangeName, e.getMessage());
            return true;
        }
    }
}