package jan.porowski.super_wallet.application.persistence.dao;

import jan.porowski.super_wallet.core.WalletEvent;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "eventMessages")
public record EventMessage(
        @Id
        UUID id,
        WalletEvent walletEvent,
        boolean published
) implements Message {

    public EventMessage(WalletEvent walletEvent) {
        this(UUID.randomUUID(), walletEvent, false);
    }
}
