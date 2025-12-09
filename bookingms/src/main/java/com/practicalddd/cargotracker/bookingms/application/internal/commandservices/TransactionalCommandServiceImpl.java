package com.practicalddd.cargotracker.bookingms.application.internal.commandservices;

import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import java.util.function.Supplier;

/**
 * Implementação do serviço transacional.
 * NÃO possui métodos final para ser proxyable pelo CDI.
 */
@Default
@ApplicationScoped
public class TransactionalCommandServiceImpl implements TransactionalService {
    
    @Inject
    private UserTransaction userTransaction;

    @Override
    public <T> T executeInTransaction(Supplier<T> operation) throws Exception {
        boolean transactionStarted = false;
        try {
            // Verificar se já existe transação ativa
            if (userTransaction.getStatus() == javax.transaction.Status.STATUS_NO_TRANSACTION) {
                userTransaction.begin();
                transactionStarted = true;
            }
            
            T result = operation.get();
            
            if (transactionStarted) {
                userTransaction.commit();
            }
            
            return result;
        } catch (Exception e) {
            if (transactionStarted) {
                try {
                    userTransaction.rollback();
                } catch (Exception rollbackEx) {
                    System.err.println("Error during transaction rollback: " + rollbackEx.getMessage());
                }
            }
            throw e;
        }
    }

    @Override
    public void executeInTransaction(Runnable operation) throws Exception {
        executeInTransaction(() -> {
            operation.run();
            return null;
        });
    }

    @Override
    public void executeInTransaction(Runnable... operations) throws Exception {
        executeInTransaction(() -> {
            for (Runnable op : operations) {
                op.run();
            }
        });
    }
}
