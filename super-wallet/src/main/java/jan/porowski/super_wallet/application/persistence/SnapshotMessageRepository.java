package jan.porowski.super_wallet.application.persistence;

import jan.porowski.super_wallet.application.persistence.dao.SnapshotMessage;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

public interface SnapshotMessageRepository extends ReactiveMongoRepository<SnapshotMessage, UUID> {

    Flux<SnapshotMessage> findAllByPublishedIsFalse();

}
