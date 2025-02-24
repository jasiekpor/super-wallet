package jan.porowski.super_wallet.application;

import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.TransactionOptions;
import com.mongodb.WriteConcern;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.ReactiveMongoTransactionManager;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.ReactiveTransactionManager;

import java.util.concurrent.TimeUnit;

@EnableScheduling
@Configuration
public class Config {

    @Bean
    public ReactiveTransactionManager transactionManager(ReactiveMongoDatabaseFactory reactiveMongoDatabaseFactory) {
        TransactionOptions options = TransactionOptions.builder()
                .readConcern(ReadConcern.SNAPSHOT)
                .writeConcern(WriteConcern.MAJORITY.withJournal(true))
                .readPreference(ReadPreference.primary())
                .maxCommitTime(10L, TimeUnit.SECONDS)
                .build();
        return new ReactiveMongoTransactionManager(reactiveMongoDatabaseFactory, options);
    }
}
