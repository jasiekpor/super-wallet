package jan.porowski.super_wallet.application.messaging.producers;

import jan.porowski.super_wallet.core.WalletEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.UUIDSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonSerializer;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class ProducersConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public Map<String, Object> producerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, UUIDSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return props;
    }

    @Bean
    public SenderOptions<UUID, WalletEvent> senderOptions() {
        return SenderOptions.create(producerConfigs());
    }

    @Bean
    public KafkaSender<UUID, WalletEvent> walletEventSender() {
        return KafkaSender.create(senderOptions());
    }

    @Bean
    public SenderOptions<UUID, WalletSnapshot> walletSnapshotSenderOptions() {
        return SenderOptions.create(producerConfigs());
    }

    @Bean
    public KafkaSender<UUID, WalletSnapshot> walletSnapshotKafkaSender() {
        return KafkaSender.create(walletSnapshotSenderOptions());
    }

}
