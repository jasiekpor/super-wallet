package jan.porowski.super_wallet.core;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WalletEventStore {
    Flux<WalletEvent> getEvents(UUID walletId);

    Mono<Void> save(WalletEvent event);

    default Mono<WalletAggregate> getWallet(UUID id) {
        return getEvents(id)
                .reduce(WalletAggregate.empty(), WalletAggregate::apply);
    }
}
