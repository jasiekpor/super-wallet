package jan.porowski.super_wallet;

import jan.porowski.super_wallet.application.messaging.consumers.WalletCommand;
import jan.porowski.super_wallet.application.messaging.producers.WalletSnapshot;
import jan.porowski.super_wallet.core.WalletEvent;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
@ContextConfiguration(classes = TestKafkaConfig.class)
class SuperWalletAggregateApplicationTests {

    @Autowired
    private KafkaTemplate<UUID, WalletCommand> sender;

    @Autowired
    KafkaConsumer<UUID, WalletEvent> eventKafkaConsumer;

    @Autowired
    KafkaConsumer<UUID, WalletSnapshot> snapshotKafkaConsumer;

    @Container
    static ConfluentKafkaContainer kafkaContainer = new ConfluentKafkaContainer("confluentinc/cp-kafka:7.4.0");

    @Container
    static MongoDBContainer mongoContainer = new MongoDBContainer("mongo:latest");

    @DynamicPropertySource
    static void configureKafka(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", kafkaContainer::getBootstrapServers);
        registry.add("spring.data.mongodb.uri", mongoContainer::getReplicaSetUrl);
    }

    @Value("${kafka.topics.consumer.commands}")
    String kafkaTopic;

    @Test
    public void testWalletEventListener() {
        UUID walletId = UUID.randomUUID();
        WalletCommand.CreateWallet createWallet = new WalletCommand.CreateWallet(walletId, UUID.randomUUID());
        sender.executeInTransaction(operations -> operations.send(kafkaTopic, createWallet.walletId(), createWallet));

        assertThat(pullEvent()).isInstanceOfSatisfying(WalletEvent.WalletCreated.class, event -> {
                    System.out.println(event);
                    assertThat(event.walletId()).isEqualTo(walletId);
                }
        );

        assertThat(pullSnapshot()).isInstanceOfSatisfying(WalletSnapshot.class, event -> {
            System.out.println(event);
            assertThat(event.walletId()).isEqualTo(walletId);
            assertThat(event.balances()).isEmpty();
        });

    }

    private WalletEvent pullEvent() {
        return eventKafkaConsumer.poll(Duration.ofSeconds(10)).iterator().next().value();
    }

    private WalletSnapshot pullSnapshot() {
        return snapshotKafkaConsumer.poll(Duration.ofSeconds(10)).iterator().next().value();
    }

    @Test
    void contextLoads() {

    }

}
