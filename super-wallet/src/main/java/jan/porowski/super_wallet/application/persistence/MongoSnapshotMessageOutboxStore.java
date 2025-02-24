package jan.porowski.super_wallet.application.persistence;

import jan.porowski.super_wallet.application.OutboxStore;
import jan.porowski.super_wallet.application.persistence.dao.EventMessage;
import jan.porowski.super_wallet.application.persistence.dao.SnapshotMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class MongoSnapshotMessageOutboxStore implements OutboxStore<SnapshotMessage> {

    private final SnapshotMessageRepository repository;
    private final ReactiveMongoTemplate mongoTemplate;

    @Override
    public Mono<SnapshotMessage> save(SnapshotMessage M) {
        return repository.save(M);
    }

    @Override
    public Flux<SnapshotMessage> getAllUnpublished() {
        return repository.findAllByPublishedIsFalse();
    }

    @Override
    public Mono<Void> markAsPublished(UUID id) {
        return mongoTemplate.findAndModify(
                        Query.query(Criteria.where("_id").is(id)),
                        new Update().set("published", true),
                        FindAndModifyOptions.options().returnNew(true),
                        SnapshotMessage.class
                )
                .then();
    }
}
