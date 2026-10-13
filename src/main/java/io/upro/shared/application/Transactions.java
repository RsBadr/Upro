package io.upro.shared.application;

import java.util.function.Supplier;

/**
 * Port technique : execute un cas d'usage dans une transaction, sans que la couche
 * application ne depende de Spring.
 */
public interface Transactions {

    <T> T inTransaction(Supplier<T> work);

    /** Pour les tests et les adapters sans transaction. */
    static Transactions none() {
        return new Transactions() {
            @Override
            public <T> T inTransaction(Supplier<T> work) {
                return work.get();
            }
        };
    }
}
