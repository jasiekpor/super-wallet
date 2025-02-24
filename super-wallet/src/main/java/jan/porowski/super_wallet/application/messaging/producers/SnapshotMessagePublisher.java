package jan.porowski.super_wallet.application.messaging.producers;

import jan.porowski.super_wallet.application.OutboxStore;
import jan.porowski.super_wallet.application.persistence.dao.SnapshotMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotMessagePublisher {

    private final String snapshotTopic = "wallet-snapshots";

    private final OutboxStore<SnapshotMessage> snapshotMessageOutboxStore;
    private final KafkaSender<UUID, WalletSnapshot> kafkaSender;
    private final TransactionalOperator transactionalOperator;

    @Scheduled(fixedRate = 5000)
    public void sendUnpublished() {
        snapshotMessageOutboxStore.getAllUnpublished()
                .concatMap(this::processOne)
                .subscribe();
    }

    private Mono<Void> processOne(SnapshotMessage snapshotMessage) {
        return Mono.just(WalletSnapshot.from(snapshotMessage.walletAggregate()))
                .flatMap(this::publish)
                .then(snapshotMessageOutboxStore.markAsPublished(snapshotMessage.id()))
                .as(transactionalOperator::transactional)
                .doOnError(t-> log.error("Error publishing snapshot {}", snapshotMessage.id(), t));
    }

    private Mono<Void> publish(WalletSnapshot snapshot) {
        return Mono.just(new ProducerRecord<>(snapshotTopic, snapshot.walletId(), snapshot))
                .map(record -> SenderRecord.create(record, UUID.randomUUID().toString()))
                .as(kafkaSender::send)
                .doOnComplete(() -> log.info("Published snapshot {}", snapshot))
                .then();
    }
}
