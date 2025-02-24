package jan.porowski.super_wallet.core;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.UUID;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = WalletEvent.WalletCreated.class, name = "walletCreated"),
        @JsonSubTypes.Type(value = WalletEvent.FundsAdded.class, name = "fundsAdded"),
        @JsonSubTypes.Type(value = WalletEvent.FundsBlocked.class, name = "fundsBlocked"),
        @JsonSubTypes.Type(value = WalletEvent.FundsWithdrawn.class, name = "fundsWithdrawn"),
        @JsonSubTypes.Type(value = WalletEvent.FundsReleased.class, name = "fundsReleased"),
        @JsonSubTypes.Type(value = WalletEvent.OperationFailed.class, name = "operationFailed")
})
public sealed interface WalletEvent {
    UUID walletId();

    UUID eventId();

    UUID commandId();

    Instant time();

    record WalletCreated(
            UUID walletId,
            UUID eventId,
            UUID commandId,
            Instant time
    ) implements WalletEvent {
    }

    record FundsAdded(
            UUID walletId,
            Token token,
            UUID eventId,
            UUID commandId,
            Instant time
    ) implements WalletEvent {
    }

    record FundsBlocked(
            UUID walletId,
            UUID blockId,
            Token token,
            UUID eventId,
            UUID commandId,
            Instant time
    ) implements WalletEvent {
    }

    record FundsWithdrawn(
            UUID walletId,
            UUID blockId,
            UUID eventId,
            UUID commandId,
            Instant time
    ) implements WalletEvent {
    }

    record FundsReleased(
            UUID walletId,
            UUID blockId,
            UUID eventId,
            UUID commandId,
            Instant time
    ) implements WalletEvent {
    }

    record OperationFailed(
            UUID walletId,
            String reason,
            UUID eventId,
            UUID commandId,
            Instant time
    ) implements WalletEvent {
    }
}
