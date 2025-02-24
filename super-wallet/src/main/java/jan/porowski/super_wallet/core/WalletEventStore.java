package jan.porowski.super_wallet.core;

import reactor.core.publisher.Mono;

import java.util.UUID;

public interface WalletEventStore {

    Mono<WalletAggregate> save(WalletEvent event);

    Mono<WalletAggregate> getWallet(UUID walletId);
}
