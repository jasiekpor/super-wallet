package jan.porowski.super_wallet.application.messaging.producers;

import jan.porowski.super_wallet.application.OutboxStore;
import jan.porowski.super_wallet.application.persistence.dao.EventMessage;
import jan.porowski.super_wallet.core.WalletEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
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
public class WalletEventPublisher {

    @Value("${kafka.topics.producers.events}")
    private String eventTopic;

    private final OutboxStore<EventMessage> snapshotMessageOutboxStore;
    private final KafkaSender<UUID, WalletEvent> kafkaSender;
    private final TransactionalOperator transactionalOperator;

    @Scheduled(fixedRate = 5000)
    public void sendUnpublished() {
        snapshotMessageOutboxStore.getAllUnpublished()
                .concatMap(this::processOne)
                .subscribe();
    }

    private Mono<Void> processOne(EventMessage snapshotMessage) {
        return publish(snapshotMessage.walletEvent())
                .then(snapshotMessageOutboxStore.markAsPublished(snapshotMessage.id()))
                .as(transactionalOperator::transactional)
                .doOnError(t -> log.error("Error publishing event {}", snapshotMessage.id(), t));
    }

    private Mono<Void> publish(WalletEvent event) {
        return Mono.just(new ProducerRecord<>(eventTopic, event.walletId(), event))
                .map(record -> SenderRecord.create(record, UUID.randomUUID().toString()))
                .as(kafkaSender::send)
                .doOnComplete(() -> log.info("Published event {}", event))
                .then();
    }
}
