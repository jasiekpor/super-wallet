package jan.porowski.super_wallet.application;

import jan.porowski.super_wallet.application.persistence.dao.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OutboxStore<M extends Message> {
    Mono<M> save(M message);

    Flux<M> getAllUnpublished();

    Mono<Void> markAsPublished(UUID id);
}
