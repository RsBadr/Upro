package io.upro.shared.infrastructure;

import io.upro.shared.application.Transactions;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.function.Supplier;

/**
 * Adapter du port {@link Transactions} sur le gestionnaire de transactions Spring.
 */
public class SpringTransactions implements Transactions {

    private final TransactionTemplate template;

    public SpringTransactions(TransactionTemplate template) {
        this.template = template;
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return template.execute(status -> work.get());
    }
}
