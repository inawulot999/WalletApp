package com.inawulot.wallet.dto;

import com.inawulot.wallet.domain.TransferRecord;
import com.inawulot.wallet.domain.TransferStatus;
import com.inawulot.wallet.domain.TransferType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
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
    public static TransferResponse from(TransferRecord transfer) {
        return new TransferResponse(
                transfer.id(),
                transfer.createdAt(),
                transfer.sourceUserId(),
                transfer.transferType(),
                transfer.status(),
                transfer.sourceCurrency(),
                transfer.targetCurrency(),
                transfer.sourceAmount(),
                transfer.feeAmount(),
                transfer.exchangeRate(),
                transfer.estimatedTargetAmount(),
                transfer.recipientName(),
                transfer.destinationCountry(),
                transfer.destinationReference(),
                transfer.complianceNote()
        );
    }
}
