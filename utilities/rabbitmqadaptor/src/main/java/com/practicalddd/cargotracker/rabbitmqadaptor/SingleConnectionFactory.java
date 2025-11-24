package com.practicalddd.cargotracker.rabbitmqadaptor;


import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.ShutdownListener;
import com.rabbitmq.client.ShutdownSignalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeoutException;

public class SingleConnectionFactory extends ConnectionFactory {
    
    private enum State {
        NEVER_CONNECTED,

        CONNECTING,

        CONNECTED,

        CLOSED
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleConnectionFactory.class);

    public static final int CONNECTION_HEARTBEAT_IN_SEC = 3;
    public static final int CONNECTION_TIMEOUT_IN_MS = 1000;
    public static final int CONNECTION_ESTABLISH_INTERVAL_IN_MS = 500;
    
    ShutdownListener connectionShutdownListener;
    List<ConnectionListener> connectionListeners;
    volatile Connection connection;
    volatile State state = State.NEVER_CONNECTED;
    private ExecutorService executorService;

    private final Object operationOnConnectionMonitor = new Object();

    public SingleConnectionFactory() {
        super();
        setRequestedHeartbeat(CONNECTION_HEARTBEAT_IN_SEC);
        setConnectionTimeout(CONNECTION_TIMEOUT_IN_MS);
        connectionListeners = Collections.synchronizedList(new LinkedList<ConnectionListener>());
        connectionShutdownListener = new ConnectionShutDownListener();
    }
    
    public Connection newConnection() throws IOException, TimeoutException {
        if (state == State.CLOSED) {
            throw new IOException("Attempt to retrieve a connection from a closed connection factory");
        }

        if (state == State.NEVER_CONNECTED) {
            establishConnection();
        }

        if (connection != null && connection.isOpen()) {
            return connection;
        }

        LOGGER.error("Unable to retrieve connection");
        throw new IOException("Unable to retrieve connection");
    }
    
    @PreDestroy
    public void close() {
        synchronized (operationOnConnectionMonitor) {
            if (state == State.CLOSED) {
                LOGGER.warn("Attempt to close connection factory which is already closed");
                return;
            }
            LOGGER.info("Closing connection factory");
            if (connection != null) {
                try {
                    connection.close();
                    connection = null;
                } catch (IOException e) {
                    if (! connection.isOpen()) {
                        LOGGER.warn("Attempt to close an already closed connection");
                    } else {
                        LOGGER.error("Unable to close current connection", e);
                    }
                }
            }
            changeState(State.CLOSED);
            LOGGER.info("Closed connection factory");
        }
    }
    
    public void registerListener(ConnectionListener connectionListener) {
        connectionListeners.add(connectionListener);
    }

    public void removeConnectionListener(ConnectionListener connectionListener) {
        connectionListeners.remove(connectionListener);
    }

    public void setExecutorService(ExecutorService executorService) {
        if (this.executorService != null) {
            throw new IllegalStateException("ExecutorService already set, trying to change it");
        }
        this.executorService = executorService;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    void changeState(State newState) {
        state = newState;
        notifyListenersOnStateChange();
    }

    void notifyListenersOnStateChange() {
        LOGGER.debug("Notifying connection listeners about state change to {}", state);
        
        for (ConnectionListener listener : connectionListeners) {
            switch (state) {
                case CONNECTED:
                    listener.onConnectionEstablished(connection);
                    break;
                case CONNECTING:
                    listener.onConnectionLost(connection);
                    break;
                case CLOSED:
                    listener.onConnectionClosed(connection);
                    break;
                default:
                    break;
            }
        }
    }

    void establishConnection () throws IOException, TimeoutException {
        synchronized (operationOnConnectionMonitor) {
            if (state == State.CLOSED) {
                throw new IOException("Attempt to establish a connection with a closed connection factory");
            } else if (state == State.CONNECTED) {
                LOGGER.warn("Establishing new connection although a connection is already established");
            }
            try {
                LOGGER.info("Trying to establish connection to {}:{}", getHost(), getPort());
                connection = super.newConnection(executorService);
                connection.addShutdownListener(connectionShutdownListener);
                LOGGER.info("Established connection to {}:{}", getHost(), getPort());
                changeState(State.CONNECTED);
            } catch (IOException e) {
                LOGGER.error("Failed to establish connection to {}:{}", getHost(), getPort());
                throw e;
            }
        }
    }
    
    private class ConnectionShutDownListener implements ShutdownListener {

        public void shutdownCompleted(ShutdownSignalException cause) {
            if (! cause.isHardError()) {
                return;
            }

            synchronized (operationOnConnectionMonitor) {
                if (state == State.CLOSED || state == State.CONNECTING) {
                    return;
                }
                changeState(State.CONNECTING);
            }
            LOGGER.error("Connection to {}:{} lost", getHost(), getPort());
            while (state == State.CONNECTING) {
                try {
                    establishConnection();
                    return;
                } catch (IOException e) {
                    LOGGER.info("Next reconnect attempt in {} ms", CONNECTION_ESTABLISH_INTERVAL_IN_MS);
                    try {
                        Thread.sleep(CONNECTION_ESTABLISH_INTERVAL_IN_MS);
                    } catch (InterruptedException ie) {
                        return;
                    }
                } catch (TimeoutException te) {
                    LOGGER.info("Next reconnect attempt in {} ms", CONNECTION_ESTABLISH_INTERVAL_IN_MS);
                    try {
                        Thread.sleep(CONNECTION_ESTABLISH_INTERVAL_IN_MS);
                    } catch (InterruptedException ie) {
                        return;
                    }
                }
            }
        }
    }
}
