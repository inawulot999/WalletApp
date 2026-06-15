package com.inawulot.wallet.dto;

import java.math.BigDecimal;

public record WalletBalanceResponse(
        String accountReference,
        String currency,
        BigDecimal balance
) {
}
