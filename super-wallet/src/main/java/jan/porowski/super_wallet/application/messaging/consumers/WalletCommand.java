package jan.porowski.super_wallet.application.messaging.consumers;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jan.porowski.super_wallet.core.Token;
import jan.porowski.super_wallet.core.WalletEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static jan.porowski.super_wallet.core.WalletEvent.*;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = WalletCommand.CreateWallet.class, name = "createWallet"),
        @JsonSubTypes.Type(value = WalletCommand.DepositFunds.class, name = "depositFunds"),
        @JsonSubTypes.Type(value = WalletCommand.BlockFunds.class, name = "blockFunds"),
        @JsonSubTypes.Type(value = WalletCommand.WithdrawFunds.class, name = "withdrawFunds"),
        @JsonSubTypes.Type(value = WalletCommand.ReleaseFunds.class, name = "releaseFunds")
})
public sealed interface WalletCommand {
    UUID walletId();

    UUID commandId();

    WalletEvent toEvent();

    record CreateWallet(
            UUID walletId,
            UUID commandId
    ) implements WalletCommand {
        @Override
        public WalletEvent toEvent() {
            return new WalletCreated(walletId, UUID.randomUUID(), commandId, Instant.now());
        }
    }

    record DepositFunds(
            UUID walletId,
            String symbol,
            BigDecimal amount,
            UUID commandId
    ) implements WalletCommand {
        @Override
        public WalletEvent toEvent() {
            return new FundsAdded(walletId, new Token(symbol, amount), UUID.randomUUID(), commandId, Instant.now());
        }
    }

    record BlockFunds(
            UUID walletId,
            UUID blockId,
            String symbol,
            BigDecimal amount,
            UUID commandId
    ) implements WalletCommand {
        @Override
        public WalletEvent toEvent() {
            return new FundsBlocked(walletId, blockId, new Token(symbol, amount), UUID.randomUUID(), commandId, Instant.now());
        }
    }

    record WithdrawFunds(
            UUID walletId,
            UUID blockId,
            UUID commandId
    ) implements WalletCommand {
        @Override
        public WalletEvent toEvent() {
            return new FundsWithdrawn(walletId, blockId, UUID.randomUUID(), commandId, Instant.now());
        }
    }

    record ReleaseFunds(
            UUID walletId,
            UUID blockId,
            UUID commandId
    ) implements WalletCommand {
        @Override
        public WalletEvent toEvent() {
            return new FundsReleased(walletId, blockId, UUID.randomUUID(), commandId, Instant.now());
        }
    }
}
