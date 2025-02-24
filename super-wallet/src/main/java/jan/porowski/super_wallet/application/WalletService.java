package jan.porowski.super_wallet.application;

import jan.porowski.super_wallet.application.persistence.dao.EventMessage;
import jan.porowski.super_wallet.application.persistence.dao.SnapshotMessage;
import jan.porowski.super_wallet.core.*;
import jan.porowski.super_wallet.core.exceptions.DuplicatedEventException;
import jan.porowski.super_wallet.core.exceptions.WalletException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;
import java.util.function.Function;

import static jan.porowski.super_wallet.core.WalletEvent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {

    private final WalletEventStore walletEventStore;
    private final OutboxStore<SnapshotMessage> snapshotMessageOutboxStore;
    private final OutboxStore<EventMessage> eventMessageOutboxStore;
    private final TransactionalOperator transactionalOperator;

    public Mono<Void> handle(WalletEvent event) {
        return handleEvent(event)
                .onErrorResume(DuplicatedEventException.class, _ -> {
                    log.warn("Duplicated event: {}", event);
                    return Mono.empty();
                })
                .onErrorResume(fallback(event))
                .as(transactionalOperator::transactional)
                .then();
    }

    private Mono<Void> handleEvent(WalletEvent event) {
        return walletEventStore.save(event)
                .delayUntil(_ -> eventMessageOutboxStore.save(new EventMessage(event)))
                .delayUntil(wallet -> snapshotMessageOutboxStore.save(new SnapshotMessage(wallet)))
                .then();
    }

    private Function<Throwable, Mono<? extends Void>> fallback(WalletEvent event) {
        return error ->
                Mono.just(errorEvent(event, error))
                .delayUntil(walletEventStore::save)
                .delayUntil(_ -> eventMessageOutboxStore.save(new EventMessage(event)))
                .then();
    }

    private static OperationFailed errorEvent(WalletEvent event, Throwable throwable) {
        if (throwable instanceof WalletException walletException) {
            return new OperationFailed(event.walletId(), walletException.getClass().getName(), UUID.randomUUID(), event.commandId(), Instant.now());
        }
        return new OperationFailed(event.walletId(), throwable.getMessage(), UUID.randomUUID(), event.commandId(), Instant.now());
    }
}
