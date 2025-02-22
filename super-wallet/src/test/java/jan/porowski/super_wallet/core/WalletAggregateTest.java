package jan.porowski.super_wallet.core;

import jan.porowski.super_wallet.core.exceptions.WalletException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.stream.Stream;

import static jan.porowski.super_wallet.core.WalletEvent.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class WalletAggregateTest {

    @Test
    void shouldCreateWalletAggregate() {
        WalletAggregate wallet = WalletAggregate.empty();

        WalletCreated walletCreated = new WalletCreated(UUID.randomUUID(), UUID.randomUUID());
        WalletAggregate result = wallet.apply(walletCreated);

        assertThat(result.getId()).isEqualTo(walletCreated.walletId());
        assertThat(result.getBalances()).isEmpty();
        assertThat(result.getBlockedAmounts()).isEmpty();
        assertThat(result.getVersion()).isEqualTo(1);
    }

    @Test
    void shouldAddFunds() {
        WalletCreated walletCreated = new WalletCreated(UUID.randomUUID(), UUID.randomUUID());
        WalletAggregate wallet = WalletAggregate.empty().apply(walletCreated);

        FundsAdded fundsAdded = new FundsAdded(walletCreated.walletId(), new Token("BTC", BigDecimal.ONE), UUID.randomUUID());
        WalletAggregate result = wallet.apply(fundsAdded);

        assertThat(result.getId()).isEqualTo(walletCreated.walletId());
        assertThat(result.getBalances()).containsEntry("BTC", BigDecimal.ONE);
        assertThat(result.getBlockedAmounts()).isEmpty();
        assertThat(result.getVersion()).isEqualTo(2);
    }

    @Test
    void shouldBlockFunds() {
        WalletCreated walletCreated = new WalletCreated(UUID.randomUUID(), UUID.randomUUID());
        FundsAdded fundsAdded = new FundsAdded(walletCreated.walletId(), new Token("BTC", BigDecimal.ONE), UUID.randomUUID());
        WalletAggregate wallet = WalletAggregate.empty().apply(walletCreated)
                .apply(fundsAdded);

        FundsBlocked fundsBlocked = new FundsBlocked(walletCreated.walletId(), UUID.randomUUID(), new Token("BTC", BigDecimal.ONE), UUID.randomUUID());
        WalletAggregate result = wallet.apply(fundsBlocked);

        assertThat(result.getId()).isEqualTo(walletCreated.walletId());
        assertThat(result.getBalances()).containsEntry("BTC", BigDecimal.ONE);
        assertThat(result.getBlockedAmounts()).containsEntry(fundsBlocked.blockId(), new Token("BTC", BigDecimal.ONE));
        assertThat(result.getVersion()).isEqualTo(3);
    }

    @Test
    void shouldReleaseFunds() {
        WalletCreated walletCreated = new WalletCreated(UUID.randomUUID(), UUID.randomUUID());
        FundsAdded fundsAdded = new FundsAdded(walletCreated.walletId(), new Token("BTC", BigDecimal.ONE), UUID.randomUUID());
        FundsBlocked fundsBlocked = new FundsBlocked(walletCreated.walletId(), UUID.randomUUID(), new Token("BTC", BigDecimal.ONE), UUID.randomUUID());
        WalletAggregate wallet = WalletAggregate.empty().apply(walletCreated)
                .apply(fundsAdded)
                .apply(fundsBlocked);

        FundsReleased fundsReleased = new FundsReleased(walletCreated.walletId(), fundsBlocked.blockId(), UUID.randomUUID());
        WalletAggregate result = wallet.apply(fundsReleased);

        assertThat(result.getId()).isEqualTo(walletCreated.walletId());
        assertThat(result.getBalances()).containsEntry("BTC", BigDecimal.ONE);
        assertThat(result.getBlockedAmounts()).isEmpty();
        assertThat(result.getVersion()).isEqualTo(4);
    }

    @Test
    void shouldWithdrawFunds() {
        WalletCreated walletCreated = new WalletCreated(UUID.randomUUID(), UUID.randomUUID());
        FundsAdded fundsAdded = new FundsAdded(walletCreated.walletId(), new Token("BTC", BigDecimal.ONE), UUID.randomUUID());
        FundsBlocked fundsBlocked = new FundsBlocked(walletCreated.walletId(), UUID.randomUUID(), new Token("BTC", BigDecimal.ONE), UUID.randomUUID());
        WalletAggregate wallet = WalletAggregate.empty().apply(walletCreated)
                .apply(fundsAdded)
                .apply(fundsBlocked);

        FundsWithdrawn fundsWithdrawn = new FundsWithdrawn(walletCreated.walletId(), fundsBlocked.blockId(), UUID.randomUUID());
        WalletAggregate result = wallet.apply(fundsWithdrawn);

        assertThat(result.getId()).isEqualTo(walletCreated.walletId());
        assertThat(result.getBalances()).containsEntry("BTC", BigDecimal.ZERO);
        assertThat(result.getBlockedAmounts()).isEmpty();
        assertThat(result.getVersion()).isEqualTo(4);
    }

    @Test
    void shouldDoNothingOnOperationFailed() {
        WalletCreated walletCreated = new WalletCreated(UUID.randomUUID(), UUID.randomUUID());
        WalletAggregate wallet = WalletAggregate.empty().apply(walletCreated);

        OperationFailed operationFailed = new OperationFailed(walletCreated.walletId(), "something failed", UUID.randomUUID());
        WalletAggregate result = wallet.apply(operationFailed);

        assertThat(result).isEqualTo(wallet);
    }

    @Test
    void shouldThrowWalletNotYetCreated(){
        WalletAggregate empty = WalletAggregate.empty();
        FundsAdded fundsAdded = new FundsAdded(UUID.randomUUID(), new Token("BTC", BigDecimal.ONE), UUID.randomUUID());

        assertThrows(WalletException.class, () -> empty.apply(fundsAdded), "Wallet not yet created");
    }

    @Test
    void shouldThrowWalletAlreadyExists() {
        WalletCreated walletCreated = new WalletCreated(UUID.randomUUID(), UUID.randomUUID());
        WalletAggregate wallet = WalletAggregate.empty().apply(walletCreated);

        WalletCreated event = new WalletCreated(UUID.randomUUID(), UUID.randomUUID());

        assertThrows(WalletException.class, () -> wallet.apply(event), "Wallet already exists");
    }

    @Test
    void shouldThrowInsufficientFunds() {
        WalletCreated walletCreated = new WalletCreated(UUID.randomUUID(), UUID.randomUUID());
        FundsAdded fundsAdded = new FundsAdded(walletCreated.walletId(), new Token("BTC", BigDecimal.ONE), UUID.randomUUID());
        FundsBlocked fundsBlocked = new FundsBlocked(walletCreated.walletId(), UUID.randomUUID(), new Token("BTC", BigDecimal.ONE), UUID.randomUUID());
        WalletAggregate wallet = WalletAggregate.empty().apply(walletCreated)
                .apply(fundsAdded)
                .apply(fundsBlocked);

        FundsBlocked blockTen = new FundsBlocked(walletCreated.walletId(), UUID.randomUUID(), new Token("BTC", BigDecimal.TEN), UUID.randomUUID());

        assertThrows(WalletException.class, () -> wallet.apply(blockTen), "Insufficient funds");
    }

    @ParameterizedTest
    @MethodSource("events")
    void shouldThrowBlockNotFound(WalletEvent event) {
        WalletCreated walletCreated = new WalletCreated(UUID.randomUUID(), UUID.randomUUID());
        WalletAggregate wallet = WalletAggregate.empty().apply(walletCreated);

        assertThrows(WalletException.class, () -> wallet.apply(event), "Blocked amount not found");
    }

    private static Stream<WalletEvent> events() {
        return Stream.of(
                new FundsWithdrawn(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()),
                new FundsReleased(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID())
        );
    }
}