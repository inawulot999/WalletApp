package com.inawulot.wallet.dto;

import com.inawulot.wallet.domain.LedgerEntry;
import com.inawulot.wallet.domain.LedgerSide;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LedgerEntryResponse(
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
    public static LedgerEntryResponse from(LedgerEntry entry) {
        return new LedgerEntryResponse(
                entry.id(),
                entry.transactionId(),
                entry.createdAt(),
                entry.accountReference(),
                entry.currency(),
                entry.side(),
                entry.amount(),
                entry.balanceAfter(),
                entry.memo()
        );
    }
}
