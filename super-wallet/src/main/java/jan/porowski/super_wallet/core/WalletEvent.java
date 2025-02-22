package jan.porowski.super_wallet.core;

import java.util.UUID;

public sealed interface WalletEvent  {
    UUID walletId();

    UUID transactionId();

    record WalletCreated(
            UUID walletId,
            UUID transactionId
    ) implements WalletEvent {
    }

    record FundsAdded(
            UUID walletId,
            Token token,
            UUID transactionId
    ) implements WalletEvent {
    }

    record FundsBlocked(
            UUID walletId,
            UUID blockId,
            Token token,
            UUID transactionId
    ) implements WalletEvent {
    }

    record FundsWithdrawn(
            UUID walletId,
            UUID blockId,
            UUID transactionId
    ) implements WalletEvent {
    }

    record FundsReleased(
            UUID walletId,
            UUID blockId,
            UUID transactionId
    ) implements WalletEvent {
    }

    record OperationFailed(
            UUID walletId,
            String reason,
            UUID transactionId
    ) implements WalletEvent {
    }
}
