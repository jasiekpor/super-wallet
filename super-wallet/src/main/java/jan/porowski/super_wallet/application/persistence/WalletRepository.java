package jan.porowski.super_wallet.application.persistence;

import jan.porowski.super_wallet.application.persistence.dao.WalletEntity;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

import java.util.UUID;

public interface WalletRepository extends ReactiveMongoRepository<WalletEntity, UUID> {
}
