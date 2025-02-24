package jan.porowski.super_wallet.application.persistence;

import jan.porowski.super_wallet.application.persistence.dao.WalletEntity;
import jan.porowski.super_wallet.core.*;
import jan.porowski.super_wallet.core.exceptions.DuplicatedEventException;
import jan.porowski.super_wallet.core.exceptions.WalletNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Repository
@RequiredArgsConstructor
class MongoWalletEventStore implements WalletEventStore {

    private final WalletRepository walletRepository;
    private final ReactiveMongoTemplate reactiveMongoTemplate;

    @Override
    public Mono<WalletAggregate> getWallet(UUID walletId) {
        return walletRepository.findById(walletId)
                .switchIfEmpty(Mono.error(() -> new WalletNotFoundException(walletId)))
                .map(WalletEntity::events)
                .flatMapMany(Flux::fromIterable)
                .reduce(WalletAggregate.empty(), WalletAggregate::apply);
    }

    @Override
    public Mono<WalletAggregate> save(WalletEvent event) {
        if (event instanceof WalletEvent.WalletCreated) {
            return createWallet(event);
        }
        return addEvent(event);
    }

    private Mono<WalletAggregate> createWallet(WalletEvent event) {
        return walletRepository.save(new WalletEntity(event.walletId(), List.of(event), 0))
                .map(WalletEntity::events)
                .flatMapMany(Flux::fromIterable)
                .reduce(WalletAggregate.empty(), WalletAggregate::apply);
    }

    private Mono<WalletAggregate> addEvent(WalletEvent event) {
        return walletRepository.findById(event.walletId())
                .switchIfEmpty(Mono.error(() -> new WalletNotFoundException(event.walletId())))
                .flatMap(entity -> Flux.fromIterable(entity.events())
                        .doOnNext(validateDuplicates(event))
                        .concatWithValues(event)
                        .reduce(WalletAggregate.empty(), WalletAggregate::apply)
                        .delayUntil(_ -> appendEvent(event, entity.version())
                                .switchIfEmpty(Mono.error(RuntimeException::new))
                        )
                );
    }

    private static Consumer<WalletEvent> validateDuplicates(WalletEvent walletEvent) {
        return existingEvent -> {
            if (existingEvent.commandId().equals(walletEvent.commandId())) {
                throw new DuplicatedEventException();
            }
        };
    }

    private Mono<WalletEntity> appendEvent(WalletEvent event, long expectedVersion) {
        return reactiveMongoTemplate.findAndModify(
                Query.query(Criteria.where("id").is(event.walletId()).and("version").is(expectedVersion)),
                new Update().push("events", event).inc("version", 1),
                FindAndModifyOptions.options().returnNew(true),
                WalletEntity.class
        );
    }
}
