package com.practicalddd.cargotracker.rabbitmqadaptor;


import com.rabbitmq.client.Connection;

public interface ConnectionListener {
    
    void onConnectionEstablished(Connection connection);
    
    void onConnectionLost(Connection connection);
    
    void onConnectionClosed(Connection connection);

}
