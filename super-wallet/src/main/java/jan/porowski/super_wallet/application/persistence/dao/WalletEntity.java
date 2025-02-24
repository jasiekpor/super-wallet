package jan.porowski.super_wallet.application.persistence.dao;

import jan.porowski.super_wallet.core.WalletEvent;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.UUID;

@Document(collection = "wallets")
public record WalletEntity(
        @Id
        UUID id,
        List<WalletEvent> events,
        @Version
        long version
) {

}
