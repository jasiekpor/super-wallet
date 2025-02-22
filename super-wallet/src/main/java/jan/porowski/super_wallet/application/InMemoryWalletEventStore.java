package jan.porowski.super_wallet.application;

import jan.porowski.super_wallet.core.WalletEvent;
import jan.porowski.super_wallet.core.WalletEventStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
class InMemoryWalletEventStore implements WalletEventStore {
    private final Map<UUID, List<WalletEvent>> eventStore;

    public InMemoryWalletEventStore() {
        this.eventStore = new ConcurrentHashMap<>();
    }

    @Override
    public Flux<WalletEvent> getEvents(UUID walletId) {
        return Flux.fromIterable(eventStore.getOrDefault(walletId, List.of()));
    }

    @Override
    public Mono<Void> save(WalletEvent event) {
        return Mono.justOrEmpty(eventStore.get(event.walletId()))
                .map(ArrayList::new)
                .defaultIfEmpty(new ArrayList<>())
                .doOnNext(events -> events.add(event))
                .doOnNext(events -> eventStore.put(event.walletId(), events))
                .then();
    }
}
