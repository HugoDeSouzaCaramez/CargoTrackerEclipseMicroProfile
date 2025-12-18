package com.practicalddd.cargotracker.bookingms.infrastructure.services;

import com.practicalddd.cargotracker.bookingms.application.commandservices.TransactionalService;

import javax.enterprise.context.ApplicationScoped;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import java.util.function.Supplier;

@ApplicationScoped
public class JtaTransactionalService implements TransactionalService {

    @Override
    @Transactional(TxType.REQUIRED)
    public <T> T executeInTransaction(Supplier<T> operation) throws Exception {
        return operation.get();
    }

    @Override
    @Transactional(TxType.REQUIRED)
    public void executeInTransaction(Runnable operation) throws Exception {
        operation.run();
    }

    @Override
    @Transactional(TxType.REQUIRED)
    public void executeInTransaction(Runnable... operations) throws Exception {
        for (Runnable operation : operations) {
            operation.run();
        }
    }
}
