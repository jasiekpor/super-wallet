package jan.porowski.super_wallet.core.exceptions;

import java.util.UUID;

public class WalletNotFoundException extends RuntimeException {

    public final UUID walletId;

    public WalletNotFoundException(UUID walletId) {
        super();
        this.walletId = walletId;
    }
}
