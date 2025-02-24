package jan.porowski.super_wallet.core.exceptions;

import jan.porowski.super_wallet.core.WalletEvent;
import lombok.Getter;

@Getter
public sealed abstract class WalletException extends RuntimeException {
    private final WalletEvent failedOn;

    public WalletException(WalletEvent failedOn) {
        super();
        this.failedOn = failedOn;
    }

    public static final class WalletAlreadyExistsException extends WalletException {
        public WalletAlreadyExistsException(WalletEvent failedOn) {
            super(failedOn);
        }
    }

    public static final class InsufficientFundsException extends WalletException {
        public InsufficientFundsException(WalletEvent failedOn) {
            super(failedOn);
        }
    }

    public static final class BlockAlreadyExistsException extends WalletException {
        public BlockAlreadyExistsException(WalletEvent failedOn) {
            super(failedOn);
        }
    }

    public static final class BlockNotFoundException extends WalletException {
        public BlockNotFoundException(WalletEvent failedOn) {
            super(failedOn);
        }
    }

    public static final class WalletNotCreatedException extends WalletException {
        public WalletNotCreatedException(WalletEvent failedOn) {
            super(failedOn);
        }
    }

}
