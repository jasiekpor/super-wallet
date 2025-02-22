package jan.porowski.super_wallet.core.exceptions;

import jan.porowski.super_wallet.core.WalletEvent;
import lombok.RequiredArgsConstructor;

public class WalletException extends RuntimeException {
    private final WalletEvent failedOn;

    public WalletException(String message, WalletEvent failedOn) {
        super(message);
        this.failedOn = failedOn;
    }
}
