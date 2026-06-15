package com.inawulot.wallet.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferRecord(
        UUID id,
        Instant createdAt,
        UUID sourceUserId,
        TransferType transferType,
        TransferStatus status,
        String sourceCurrency,
        String targetCurrency,
        BigDecimal sourceAmount,
        BigDecimal feeAmount,
        BigDecimal exchangeRate,
        BigDecimal estimatedTargetAmount,
        String recipientName,
        String destinationCountry,
        String destinationReference,
        String complianceNote
) {
}
