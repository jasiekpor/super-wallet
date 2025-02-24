package jan.porowski.super_wallet.application.messaging.consumers;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jan.porowski.super_wallet.application.WalletService;
import jan.porowski.super_wallet.core.WalletEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletCommandConsumer {

    private final KafkaReceiver<UUID, WalletCommand> kafkaReceiver;
    private final KafkaSender<UUID, WalletEvent> kafkaSender;
    private final WalletService walletService;

    @PostConstruct
    public void startProcessing() {
        kafkaReceiver.receive()
                .publishOn(Schedulers.boundedElastic())
                .concatMap(this::processReceiverRecord)
                .subscribe(
                    _ -> {},
                    t -> log.error("error while consuming messages", t)
                );
    }

    private Mono<Void> processReceiverRecord(ReceiverRecord<UUID, WalletCommand> receiverRecord) {
        return Mono.just(receiverRecord.value())
                .map(WalletCommand::toEvent)
                .flatMap(walletService::handle)
                .doOnSuccess(_ -> receiverRecord.receiverOffset().acknowledge())
                .doOnError(e -> System.err.println("Error consuming event: " + e.getMessage()))
                .onErrorResume(e -> Mono.empty());
    }

    @PreDestroy
    public void close() {
        kafkaSender.close();
    }
}
