package jan.porowski.super_wallet.application.persistence.dao;

import jan.porowski.super_wallet.core.WalletAggregate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.UUID;

@Document(collection = "snapshotMessages")
public record SnapshotMessage(
        @Id
        UUID id,
        WalletAggregate walletAggregate,
        boolean published

) implements Message {

    public SnapshotMessage(WalletAggregate walletAggregate) {
        this(UUID.randomUUID(), walletAggregate, false);
    }
}
