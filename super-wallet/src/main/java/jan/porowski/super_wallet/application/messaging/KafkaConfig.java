package jan.porowski.super_wallet.application.messaging;

import jan.porowski.super_wallet.application.messaging.consumers.WalletCommand;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.UUIDDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;

    @Value("${kafka.topics.consumer.commands}")
    private String inputTopic;

    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, UUIDDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(JsonDeserializer.REMOVE_TYPE_INFO_HEADERS, "false");
        props.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        props.put(JsonDeserializer.VALUE_DEFAULT_TYPE, WalletCommand.class);
        return props;
    }


    @Bean
    public ReceiverOptions<UUID, WalletCommand> receiverOptions() {
        ReceiverOptions<UUID, WalletCommand> basicReceiverOptions = ReceiverOptions.create(consumerConfigs());
        return basicReceiverOptions.subscription(Collections.singletonList(inputTopic));
    }

    @Bean
    public KafkaReceiver<UUID, WalletCommand> kafkaReceiver(ReceiverOptions<UUID, WalletCommand> receiverOptions) {
        return KafkaReceiver.create(receiverOptions);
    }
}
