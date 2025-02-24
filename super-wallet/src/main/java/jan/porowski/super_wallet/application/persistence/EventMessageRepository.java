package jan.porowski.super_wallet.application.persistence;

import jan.porowski.super_wallet.application.persistence.dao.EventMessage;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.UUID;

interface EventMessageRepository extends ReactiveMongoRepository<EventMessage, UUID> {

    Flux<EventMessage> findAllByPublishedIsFalse();

}
