package com.inawulot.wallet.dto;

import com.inawulot.wallet.domain.CryptoNetwork;
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
        String complianceNote,
        Instant updatedAt,
        String assetType,
        CryptoNetwork network,
        String senderAddress,
        String recipientAddress,
        BigDecimal totalDeduction,
        String txHash,
        String statusMessage
) {
    public static TransferResponse from(TransferRecord transfer) {
        return new TransferResponse(
                transfer.getId(),
                transfer.getCreatedAt(),
                transfer.getSourceUserId(),
                transfer.getTransferType(),
                transfer.getStatus(),
                transfer.getSourceCurrency(),
                transfer.getTargetCurrency(),
                transfer.getSourceAmount(),
                transfer.getFeeAmount(),
                transfer.getExchangeRate(),
                transfer.getEstimatedTargetAmount(),
                transfer.getRecipientName(),
                transfer.getDestinationCountry(),
                transfer.getDestinationReference(),
                transfer.getComplianceNote(),
                transfer.getUpdatedAt(),
                transfer.getAssetType(),
                transfer.getNetwork(),
                transfer.getSenderAddress(),
                transfer.getRecipientAddress(),
                transfer.getTotalDeduction(),
                transfer.getTxHash(),
                transfer.getStatusMessage()
        );
    }
}
