package com.inawulot.wallet.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntry(
        UUID id,
        UUID transactionId,
        Instant createdAt,
        String accountReference,
        String currency,
        LedgerSide side,
        BigDecimal amount,
        BigDecimal balanceAfter,
        String memo
) {
}
