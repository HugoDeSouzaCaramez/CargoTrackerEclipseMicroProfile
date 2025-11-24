package com.practicalddd.cargotracker.rabbitmqadaptor;

public interface ContainsData {
    void setData(byte[] data);
    byte[] getData();
}
