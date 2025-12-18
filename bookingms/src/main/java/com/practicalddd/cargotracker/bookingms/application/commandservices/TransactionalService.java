package com.practicalddd.cargotracker.bookingms.application.commandservices;

import java.util.function.Supplier;

public interface TransactionalService {
    <T> T executeInTransaction(Supplier<T> operation) throws Exception;
    void executeInTransaction(Runnable operation) throws Exception;
    void executeInTransaction(Runnable... operations) throws Exception;
}
