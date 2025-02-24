package jan.porowski.super_wallet.application.persistence;

import jan.porowski.super_wallet.application.persistence.dao.WalletEntity;
import jan.porowski.super_wallet.core.Token;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static jan.porowski.super_wallet.core.WalletEvent.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MongoWalletEventStoreTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private ReactiveMongoTemplate reactiveMongoTemplate;

    @InjectMocks
    private MongoWalletEventStore eventStore;

    @Test
    void getWallet() {
        UUID walletId = UUID.randomUUID();
        WalletCreated walletCreated = new WalletCreated(walletId, UUID.randomUUID(), UUID.randomUUID(), Instant.now());
        FundsAdded fundsAdded = new FundsAdded(walletId, new Token("BTC", BigDecimal.ONE), UUID.randomUUID(), UUID.randomUUID(), Instant.now());

        when(walletRepository.findById(walletId)).thenReturn(Mono.just(new WalletEntity(walletId, List.of(walletCreated, fundsAdded), 2)));

        eventStore.getWallet(walletId)
                .as(StepVerifier::create)
                .assertNext(wallet -> {
                    assertThat(wallet).isNotNull();
                    assertThat(wallet.id()).isEqualTo(walletId);
                    assertThat(wallet.balances()).containsEntry("BTC", BigDecimal.ONE);
                    assertThat(wallet.blockedAmounts()).isEmpty();
                    assertThat(wallet.version()).isEqualTo(2);
                })
                .verifyComplete();
    }

    @Test
    void saveNewWallet() {
        UUID walletId = UUID.randomUUID();
        WalletCreated walletCreated = new WalletCreated(walletId, UUID.randomUUID(), UUID.randomUUID(), Instant.now());

        when(walletRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        eventStore.save(walletCreated)
                .as(StepVerifier::create)
                .assertNext(wallet -> {
                    assertThat(wallet.id()).isEqualTo(walletId);
                    assertThat(wallet.balances()).isEmpty();
                    assertThat(wallet.blockedAmounts()).isEmpty();
                    assertThat(wallet.version()).isEqualTo(1);
                    assertThat(wallet.updateTime()).isEqualTo(walletCreated.time());
                })
                .verifyComplete();
    }

    @Test
    void saveNewEvent() {
        UUID walletId = UUID.randomUUID();
        WalletCreated walletCreated = new WalletCreated(walletId, UUID.randomUUID(), UUID.randomUUID(), Instant.now());
        FundsAdded fundsAdded = new FundsAdded(walletId, new Token("BTC", BigDecimal.ONE), UUID.randomUUID(), UUID.randomUUID(), Instant.now());

        when(walletRepository.findById(walletId)).thenReturn(Mono.just(new WalletEntity(walletId, List.of(walletCreated), 1)));

        when(reactiveMongoTemplate.findAndModify(any(), any(), any(FindAndModifyOptions.class), any()))
                .thenReturn(Mono.just(new WalletEntity(walletId, List.of(walletCreated, fundsAdded), 2)));

        eventStore.save(fundsAdded)
                .as(StepVerifier::create)
                .assertNext(wallet -> {
                    assertThat(wallet.id()).isEqualTo(walletId);
                    assertThat(wallet.balances()).containsEntry("BTC", BigDecimal.ONE);
                    assertThat(wallet.blockedAmounts()).isEmpty();
                    assertThat(wallet.version()).isEqualTo(2);
                    assertThat(wallet.updateTime()).isEqualTo(fundsAdded.time());
                })
                .verifyComplete();
    }
}