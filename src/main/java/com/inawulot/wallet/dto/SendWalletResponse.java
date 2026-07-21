package com.inawulot.wallet.dto;

import com.inawulot.wallet.domain.CryptoNetwork;
import com.inawulot.wallet.domain.TransferRecord;
import com.inawulot.wallet.domain.TransferStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record SendWalletResponse(
        UUID transactionId,
        Instant createdAt,
        TransferStatus status,
        String asset,
        CryptoNetwork network,
        BigDecimal amount,
        BigDecimal fee,
        BigDecimal totalDeduction,
        String senderAddress,
        String recipientAddress,
        String txHash,
        String statusMessage
) {
    public static SendWalletResponse from(TransferRecord transfer) {
        return new SendWalletResponse(
                transfer.getId(),
                transfer.getCreatedAt(),
                transfer.getStatus(),
                transfer.getAssetType(),
                transfer.getNetwork(),
                transfer.getSourceAmount(),
                transfer.getFeeAmount(),
                transfer.getTotalDeduction(),
                transfer.getSenderAddress(),
                transfer.getRecipientAddress(),
                transfer.getTxHash(),
                transfer.getStatusMessage()
        );
    }
}
