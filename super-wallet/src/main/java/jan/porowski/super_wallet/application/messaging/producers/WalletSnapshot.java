package jan.porowski.super_wallet.application.messaging.producers;

import jan.porowski.super_wallet.core.Token;
import jan.porowski.super_wallet.core.WalletAggregate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record WalletSnapshot(
        UUID walletId,
        List<Balance> balances,
        Instant updatedAt
) {
    public static WalletSnapshot from(WalletAggregate walletAggregate) {
        List<Balance> balances = walletAggregate.balances().entrySet().stream()
                .map(entry -> {
                    BigDecimal blocked = walletAggregate.blockedAmounts().values().stream()
                            .filter(token -> token.symbol().equals(entry.getKey()))
                            .map(Token::amount)
                            .reduce(BigDecimal::add)
                            .orElse(BigDecimal.ZERO);
                    return new Balance(entry.getKey(), entry.getValue().subtract(blocked), blocked);
                }).toList();
        return new WalletSnapshot(walletAggregate.id(), balances, walletAggregate.updateTime());
    }
}
