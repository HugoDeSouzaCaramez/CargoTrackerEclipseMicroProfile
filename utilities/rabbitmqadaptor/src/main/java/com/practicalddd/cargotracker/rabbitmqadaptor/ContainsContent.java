package com.practicalddd.cargotracker.rabbitmqadaptor;

public interface ContainsContent<T> {
    void setContent(T content);
    T getContent();
}
