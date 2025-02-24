package jan.porowski.super_wallet.core;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static jan.porowski.super_wallet.core.WalletEvent.*;
import static jan.porowski.super_wallet.core.exceptions.WalletException.*;

public record WalletAggregate(
        UUID id,
        Map<String, BigDecimal> balances,
        Map<UUID, Token> blockedAmounts,
        long version,
        Instant updateTime
) {


    public WalletAggregate apply(WalletEvent event) {
        return switch (event) {
            case WalletCreated e -> applyWalletCreated(e);
            case FundsAdded e -> applyFundsAdded(e);
            case FundsBlocked e -> applyFundsBlocked(e);
            case FundsWithdrawn e -> applyFundsWithdrawn(e);
            case FundsReleased e -> applyFundsReleased(e);
            case OperationFailed _ -> this;
        };
    }

    public static WalletAggregate empty() {
        return new WalletAggregate(null, null, null, 0, Instant.EPOCH);
    }

    private WalletAggregate applyWalletCreated(WalletCreated event) {
        if (id != null || version != 0) {
            throw new WalletAlreadyExistsException(event);
        }
        return new WalletAggregate(event.walletId(), Map.of(), Map.of(), version + 1, event.time());
    }

    private WalletAggregate applyFundsAdded(FundsAdded event) {
        validateWalletCreated(event);

        Map<String, BigDecimal> newBalances = new HashMap<>(balances);
        String symbol = event.token().symbol();
        BigDecimal currentAmount = newBalances.getOrDefault(event.token().symbol(), BigDecimal.ZERO);
        BigDecimal newAmount = currentAmount.add(event.token().amount());

        newBalances.put(symbol, newAmount);

        return new WalletAggregate(id, newBalances, blockedAmounts, version + 1, event.time());
    }

    private WalletAggregate applyFundsBlocked(FundsBlocked event) {
        validateWalletCreated(event);

        if (blockedAmounts.containsKey(event.blockId())) {
            throw new BlockAlreadyExistsException(event);
        }

        Map<UUID, Token> newBlocked = new HashMap<>(blockedAmounts);
        String symbol = event.token().symbol();
        BigDecimal currentAmount = availableBalance(symbol);
        BigDecimal newAmount = currentAmount.subtract(event.token().amount());

        if (newAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InsufficientFundsException(event);
        }

        newBlocked.put(event.blockId(), event.token());

        return new WalletAggregate(id, balances, newBlocked, version + 1, event.time());
    }

    private WalletAggregate applyFundsReleased(FundsReleased event) {
        validateWalletCreated(event);

        Map<UUID, Token> newBlocked = new HashMap<>(blockedAmounts);
        Token blockedAmount = newBlocked.get(event.blockId());

        if (blockedAmount == null) {
            throw new BlockNotFoundException(event);
        }

        newBlocked.remove(event.blockId());

        return new WalletAggregate(id, balances, newBlocked, version + 1, event.time());
    }

    private WalletAggregate applyFundsWithdrawn(FundsWithdrawn event) {
        validateWalletCreated(event);

        Map<String, BigDecimal> newBalances = new HashMap<>(balances);
        Map<UUID, Token> newBlocked = new HashMap<>(blockedAmounts);
        Token blockedAmount = newBlocked.get(event.blockId());

        if (blockedAmount == null) {
            throw new BlockNotFoundException(event);
        }

        BigDecimal newAmount = newBalances.get(blockedAmount.symbol()).subtract(blockedAmount.amount());
        newBalances.put(blockedAmount.symbol(), newAmount);

        newBlocked.remove(event.blockId());

        return new WalletAggregate(id, newBalances, newBlocked, version + 1, event.time());
    }

    public BigDecimal availableBalance(String symbol) {
        BigDecimal balance = balances.getOrDefault(symbol, BigDecimal.ZERO);
        BigDecimal blockedBalance = blockedAmounts.values().stream()
                .filter(token -> token.symbol().equals(symbol))
                .map(Token::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return balance.subtract(blockedBalance);
    }

    private void validateWalletCreated(WalletEvent event) {
        if (id == null || balances == null || blockedAmounts == null) {
            throw new WalletNotCreatedException(event);
        }
    }
}
