package jan.porowski.super_wallet.application;

import jan.porowski.super_wallet.core.Token;
import jan.porowski.super_wallet.core.WalletEvent;
import jan.porowski.super_wallet.core.WalletEventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static jan.porowski.super_wallet.core.WalletEvent.*;
import static org.assertj.core.api.Assertions.assertThat;

class InMemoryWalletEventStoreTest {
    private Map<UUID, List<WalletEvent>> map;
    private WalletEventStore eventStore;

    @BeforeEach
    void setUp() {
        map = new ConcurrentHashMap<>();
        eventStore = new InMemoryWalletEventStore(map);
    }

    @Test
    void shouldSaveEvent() {
        WalletCreated walletCreated = new WalletCreated(UUID.randomUUID(), UUID.randomUUID());

        eventStore.save(walletCreated)
                .as(StepVerifier::create)
                .verifyComplete();

        assertThat(map).containsEntry(walletCreated.walletId(), List.of(walletCreated));
    }

    @Test
    void shouldGetWallet() {
        UUID walletId = UUID.randomUUID();
        WalletCreated walletCreated = new WalletCreated(walletId, UUID.randomUUID());
        FundsAdded fundsAdded = new FundsAdded(walletId, new Token("BTC", BigDecimal.ONE), UUID.randomUUID());

        map.put(walletId, List.of(walletCreated, fundsAdded));

        eventStore.getWallet(walletId)
                .as(StepVerifier::create)
                .assertNext(wallet -> {
                    assertThat(wallet).isNotNull();
                    assertThat(wallet.getId()).isEqualTo(walletId);
                    assertThat(wallet.getBalances()).containsEntry("BTC", BigDecimal.ONE);
                    assertThat(wallet.getBlockedAmounts()).isEmpty();
                    assertThat(wallet.getVersion()).isEqualTo(2);
                })
                .verifyComplete();

    }
}