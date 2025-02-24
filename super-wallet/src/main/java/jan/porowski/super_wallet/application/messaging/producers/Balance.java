package jan.porowski.super_wallet.application.messaging.producers;

import java.math.BigDecimal;

public record Balance(
        String token,
        BigDecimal available,
        BigDecimal blocked
) {
}
