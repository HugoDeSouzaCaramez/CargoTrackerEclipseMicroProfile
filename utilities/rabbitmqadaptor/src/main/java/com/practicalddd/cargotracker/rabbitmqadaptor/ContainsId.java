package com.practicalddd.cargotracker.rabbitmqadaptor;

public interface ContainsId<T> {
    void setId(T id);
    T getId();
}
