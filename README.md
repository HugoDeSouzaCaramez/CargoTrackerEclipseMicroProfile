# DDD Cargo Tracking System

## 📋 Visão Geral do Projeto

O **Cargo Tracking System** é uma aplicação baseada em Domain-Driven Design (DDD) e microserviços para gerenciamento e rastreamento de cargas marítimas. O sistema é composto por quatro microserviços principais que trabalham em conjunto através de eventos e APIs REST.

## 🏗️ Arquitetura do Sistema

### Microserviços

| Serviço | Porta | Descrição | Banco de Dados |
|---------|-------|-----------|----------------|
| **bookingms** | 8080 | Gestão de reservas e roteamento | bookingmsdb |
| **routingms** | 8081 | Fornecimento de rotas e voyages | routingmsdb |
| **trackingms** | 8082 | Rastreamento e monitoramento | trackingmsdb |
| **handlingms** | 8084 | Registro de atividades de manuseio | handlingmsdb |

### Tecnologias Utilizadas

- **Java EE** com CDI e JPA
- **MySQL** para persistência de dados
- **RabbitMQ** para mensageria assíncrona
- **Helidon MP** como runtime
- **Docker** para containerização

## 🔄 Fluxo de Dados Principal

### 1. Reserva de Carga (Booking)

**Endpoint:** `POST /cargobooking`

```json
{
  "bookingAmount": 1000,
  "originLocation": "CNHKG",
  "destLocation": "USNYC", 
  "destArrivalDeadline": "2024-10-30"
}
```

**Fluxo:**
1. Cliente faz reserva via `bookingms`
2. Sistema gera `BookingId` único
3. Cria agregação `Cargo` com especificação de rota
4. Publica `CargoBookedEvent` no RabbitMQ
5. Retorna `BookingId` para o cliente

**Evento Publicado:**
```java
CargoBookedEvent {
  id: "A1B2C3" // BookingId gerado
}
```

### 2. Roteamento de Carga

**Endpoint:** `POST /cargorouting`

```json
{
  "bookingId": "A1B2C3"
}
```

**Fluxo:**
1. `bookingms` consulta `routingms` para rota ótima
2. `routingms` busca voyages compatíveis no banco
3. Retorna `TransitPath` com legs da viagem
4. `bookingms` associa rota ao cargo
5. Publica `CargoRoutedEvent`

**Consulta ao RoutingMS:**
```http
GET /cargoRouting/optimalRoute?origin=CNHKG&destination=USNYC&deadline=2024-10-30
```

**Evento Publicado:**
```java
CargoRoutedEvent {
  content: {
    bookingId: "A1B2C3"
  }
}
```

### 3. Atribuição de Tracking

**Consumo de Evento:** `CargoRoutedEvent`

**Fluxo:**
1. `trackingms` consome `CargoRoutedEvent`
2. Gera número de tracking único
3. Cria `TrackingActivity` no banco
4. Associa `BookingId` com `TrackingNumber`

**Ação no Banco (trackingmsdb):**
```sql
INSERT INTO tracking_activity (tracking_number, booking_id) 
VALUES ('TRK123', 'A1B2C3');
```

### 4. Registro de Atividades de Manuseio

**Endpoint:** `POST /cargohandling`

```json
{
  "bookingId": "A1B2C3",
  "voyageNumber": "V0100",
  "unLocode": "CNHKG",
  "handlingType": "RECEIVE",
  "completionTime": "2024-10-01"
}
```

**Fluxo:**
1. `handlingms` registra atividade de handling
2. Valida tipo de atividade vs. necessidade de voyage
3. Persiste `HandlingActivity` no banco
4. Publica `CargoHandledEvent`

**Evento Publicado:**
```java
CargoHandledEvent {
  content: {
    bookingId: "A1B2C3",
    handlingType: "RECEIVE",
    handlingLocation: "CNHKG",
    voyageNumber: "V0100",
    handlingCompletionTime: "2024-10-01T10:00:00"
  }
}
```

### 5. Atualização de Tracking

**Consumo de Evento:** `CargoHandledEvent`

**Fluxo:**
1. `trackingms` consome `CargoHandledEvent`
2. Busca `TrackingActivity` por `BookingId`
3. Adiciona `TrackingEvent` ao histórico
4. Atualiza status de entrega

**Ação no Banco:**
```sql
INSERT INTO tracking_handling_events 
  (tracking_activity_id, voyage_number, location_id, event_type, event_time)
VALUES 
  (1, 'V0100', 'CNHKG', 'RECEIVE', '2024-10-01 10:00:00');
```

## 🗃️ Estrutura de Bancos de Dados

### bookingmsdb
```sql
-- Agregação principal de Cargo
cargo (id, booking_id, booking_amount, origin_id, spec_origin_id, spec_destination_id, spec_arrival_deadline)

-- Itinerário da carga
leg (id, cargo_id, voyage_number, load_location_id, unload_location_id, load_time, unload_time)

-- Status de entrega
-- (campos embedded na entidade Cargo)
```

### routingmsdb  
```sql
-- Catálogo de voyages
voyage (id, voyage_number)

-- Movimentos de carrier
carrier_movement (id, voyage_id, departure_location_id, arrival_location_id, departure_date, arrival_date)
```

### trackingmsdb
```sql
-- Atividade de tracking
tracking_activity (id, tracking_number, booking_id)

-- Eventos de tracking
tracking_handling_events (id, tracking_activity_id, voyage_number, location_id, event_type, event_time)
```

### handlingmsdb
```sql
-- Histórico de atividades
handling_activity (id, booking_id, event_type, location, voyage_number, event_completion_time)
```

## 🔔 Sistema de Mensageria (RabbitMQ)

### Exchanges e Routing Keys

| Microserviço | Exchange | Routing Key | Eventos |
|-------------|----------|-------------|---------|
| **bookingms** | `cargotracker.cargobookings` | `cargobookings` | `CargoBookedEvent` |
| **bookingms** | `cargotracker.cargoroutings` | `cargoroutings` | `CargoRoutedEvent` |
| **handlingms** | `cargotracker.cargohandlings` | `cargohandlings` | `CargoHandledEvent` |

### Queues de Consumo (trackingms)

| Queue | Evento Consumido | Ação |
|-------|------------------|------|
| `cargotracker.bookingsqueue` | `CargoBookedEvent` | Log apenas (teste) |
| `cargotracker.routingqueue` | `CargoRoutedEvent` | Cria tracking number |
| `cargotracker.handlingqueue` | `CargoHandledEvent` | Adiciona evento de tracking |

### Configuração de EventBinders

Cada microserviço inicializa seus `EventBinder` no startup:

```java
// Exemplo: TrackingMS inicializa todos os binders
initializeAllEventBinders(beanManager);
```

## 🚢 Domínio e Modelagem

### Agregações Principais

1. **Cargo** (bookingms)
   - `BookingId` - Identificador único
   - `RouteSpecification` - Origem, destino, deadline
   - `CargoItinerary` - Rota atribuída (lista de Legs)
   - `Delivery` - Status de entrega (calculado)

2. **Voyage** (routingms)  
   - `VoyageNumber` - Identificador do voyage
   - `Schedule` - Lista de CarrierMovements

3. **TrackingActivity** (trackingms)
   - `TrackingNumber` - Número de rastreamento
   - `BookingId` - Referência ao booking
   - `TrackingEvents` - Histórico de eventos

4. **HandlingActivity** (handlingms)
   - `CargoBookingId` - Referência ao booking
   - `Type` - Tipo de atividade (LOAD, UNLOAD, RECEIVE, etc.)
   - `Location` - Local do evento
   - `VoyageNumber` - Voyage associado (quando aplicável)

### Value Objects

- `Location` - Código UN/LOCODE
- `VoyageNumber` - Identificador de voyage
- `TrackingEventType` - Tipo e timestamp do evento
- `RouteSpecification` - Especificação completa de rota

## 🐳 Deploy com Docker

### Serviços Configurados

```yaml
services:
  mysql:          # Banco de dados MySQL
  rabbitmq:       # Broker de mensagens
  bookingms:      # Microserviço de booking
  routingms:      # Microserviço de rotas
  trackingms:     # Microserviço de tracking
  handlingms:     # Microserviço de handling
```

### Health Checks
- **MySQL**: Verificação via `mysqladmin ping`
- **RabbitMQ**: Verificação via `rabbitmqctl status`
- Dependências configuradas para inicialização ordenada

## 📊 Fluxo Completo de Rastreamento

1. **Reserva** → `CargoBookedEvent` (publicado)
2. **Roteamento** → `CargoRoutedEvent` (publicado) → Tracking criado (consumido)
3. **Manuseio** → `CargoHandledEvent` (publicado) → Tracking atualizado (consumido)
4. **Monitoramento** → Histórico completo disponível via tracking number

## 🔍 Exemplo de Sequência Completa

```bash
# 1. Fazer reserva
curl -X POST http://localhost:8080/cargobooking \
  -H "Content-Type: application/json" \
  -d '{"bookingAmount": 500, "originLocation": "JPTYO", "destLocation": "SGSIN", "destArrivalDeadline": "2024-10-15"}'

# Retorna: {"bookingId": "ABC123"}

# 2. Rotear carga
curl -X POST http://localhost:8080/cargorouting \
  -H "Content-Type: application/json" \
  -d '{"bookingId": "ABC123"}'

# 3. Registrar recebimento
curl -X POST http://localhost:8084/cargohandling \
  -H "Content-Type: application/json" \
  -d '{"bookingId": "ABC123", "unLocode": "JPTYO", "handlingType": "RECEIVE", "completionTime": "2024-10-01"}'

# 4. Consultar tracking (implementação dependente)
# Retornaria eventos: RECEIVE em JPTYO, etc.
```

## ⚠️ Considerações Importantes

- **Validação de Rotas**: Sistema valida se rota não está vazia antes de atribuir
- **Tipos de Handling**: Alguns tipos requerem voyage (LOAD/UNLOAD), outros não
- **Persistência**: Uso de JPA com geração automática de tabelas
- **Transações**: Controle transacional com `@Transactional`
- **Resiliência**: Tratamento de eventos mesmo quando TrackingActivity não existe

Este sistema oferece uma base sólida para rastreamento de cargas com separação clara de responsabilidades e comunicação assíncrona robusta através de eventos.