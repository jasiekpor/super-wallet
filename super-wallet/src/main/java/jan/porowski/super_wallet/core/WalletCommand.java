package jan.porowski.super_wallet.core;

import java.util.UUID;

public sealed interface WalletCommand {
    UUID walletId();

    record CreateWallet(
            UUID walletId,
            String transactionId
    ) implements WalletCommand {
    }

    record DepositFunds(
            UUID walletId,
            Token token,
            String transactionId
    ) implements WalletCommand {
    }

    record BlockFunds(
            UUID walletId,
            UUID blockId,
            Token token,
            String transactionId
    ) implements WalletCommand {
    }

    record WithdrawFunds(
            UUID walletId,
            Token token,
            String transactionId
    ) implements WalletCommand {
    }

    record ReleaseFunds(
            UUID walletId,
            UUID blockId,
            String transactionId
    ) implements WalletCommand {
    }
}
