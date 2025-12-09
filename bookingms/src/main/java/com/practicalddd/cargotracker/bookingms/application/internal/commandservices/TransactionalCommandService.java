package com.practicalddd.cargotracker.bookingms.application.internal.commandservices;

import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.enterprise.context.ApplicationScoped;
import java.util.function.Supplier;

/**
 * Unit of Work
 * Serviço utilitário para controle transacional explícito.
 * Usa composição em vez de herança.
 */
@ApplicationScoped
public class TransactionalCommandService {
    
    @Inject
    private UserTransaction userTransaction;

    /**
     * Executa uma operação dentro de uma transação programática.
     * Se a operação falhar, a transação é revertida.
     */
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

    /**
     * Executa uma operação void dentro de uma transação programática.
     */
    public void executeInTransaction(Runnable operation) throws Exception {
        executeInTransaction(() -> {
            operation.run();
            return null;
        });
    }

    /**
     * Executa múltiplas operações em uma única transação.
     */
    @SafeVarargs
    public final void executeInTransaction(Runnable... operations) throws Exception {
        executeInTransaction(() -> {
            for (Runnable op : operations) {
                op.run();
            }
        });
    }
}
