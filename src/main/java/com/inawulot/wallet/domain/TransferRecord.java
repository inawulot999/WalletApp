package com.inawulot.wallet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfer_records")
public class TransferRecord {
    @Id
    private UUID id;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;
    @Column(nullable = false)
    private UUID sourceUserId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferType transferType;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransferStatus status;
    @Column(nullable = false, length = 12)
    private String sourceCurrency;
    @Column(nullable = false, length = 12)
    private String targetCurrency;
    @Column(nullable = false, precision = 28, scale = 8)
    private BigDecimal sourceAmount;
    @Column(nullable = false, precision = 28, scale = 8)
    private BigDecimal feeAmount;
    @Column(nullable = false, precision = 28, scale = 8)
    private BigDecimal exchangeRate;
    @Column(nullable = false, precision = 28, scale = 8)
    private BigDecimal estimatedTargetAmount;
    @Column(nullable = false)
    private String recipientName;
    @Column(nullable = false, length = 64)
    private String destinationCountry;
    @Column(nullable = false)
    private String destinationReference;
    @Column(length = 512)
    private String complianceNote;
    @Column(length = 12)
    private String assetType;
    @Enumerated(EnumType.STRING)
    private CryptoNetwork network;
    private String senderAddress;
    private String recipientAddress;
    @Column(precision = 28, scale = 8)
    private BigDecimal totalDeduction;
    private String txHash;
    @Column(length = 512)
    private String statusMessage;

    protected TransferRecord() {
    }

    public TransferRecord(
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
        this.id = id;
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.sourceUserId = sourceUserId;
        this.transferType = transferType;
        this.status = status;
        this.sourceCurrency = sourceCurrency;
        this.targetCurrency = targetCurrency;
        this.sourceAmount = sourceAmount;
        this.feeAmount = feeAmount;
        this.exchangeRate = exchangeRate;
        this.estimatedTargetAmount = estimatedTargetAmount;
        this.recipientName = recipientName;
        this.destinationCountry = destinationCountry;
        this.destinationReference = destinationReference;
        this.complianceNote = complianceNote;
        this.assetType = targetCurrency;
        this.totalDeduction = sourceAmount;
        this.statusMessage = complianceNote;
    }

    public static TransferRecord pendingOnChain(
            UUID id,
            Instant createdAt,
            UUID sourceUserId,
            String assetType,
            CryptoNetwork network,
            BigDecimal amount,
            BigDecimal fee,
            String senderAddress,
            String recipientAddress,
            String recipientLabel,
            String memo
    ) {
        TransferRecord record = new TransferRecord(
                id,
                createdAt,
                sourceUserId,
                TransferType.EXCHANGE_WALLET,
                TransferStatus.PENDING,
                assetType,
                assetType,
                amount,
                fee,
                BigDecimal.ONE.setScale(8),
                amount,
                recipientLabel,
                "GLOBAL",
                recipientAddress,
                "Queued for network broadcast"
        );
        record.assetType = assetType;
        record.network = network;
        record.senderAddress = senderAddress;
        record.recipientAddress = recipientAddress;
        record.totalDeduction = amount.add(fee);
        record.statusMessage = memo == null || memo.isBlank() ? "Awaiting blockchain confirmation" : memo;
        return record;
    }

    public void markCompleted(String txHash) {
        this.status = TransferStatus.COMPLETED;
        this.txHash = txHash;
        this.updatedAt = Instant.now();
        this.statusMessage = "Transfer confirmed on " + network;
    }

    public void markFailed(String reason) {
        this.status = TransferStatus.FAILED;
        this.updatedAt = Instant.now();
        this.statusMessage = reason;
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public UUID getSourceUserId() {
        return sourceUserId;
    }

    public TransferType getTransferType() {
        return transferType;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public String getTargetCurrency() {
        return targetCurrency;
    }

    public BigDecimal getSourceAmount() {
        return sourceAmount;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public BigDecimal getExchangeRate() {
        return exchangeRate;
    }

    public BigDecimal getEstimatedTargetAmount() {
        return estimatedTargetAmount;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getDestinationCountry() {
        return destinationCountry;
    }

    public String getDestinationReference() {
        return destinationReference;
    }

    public String getComplianceNote() {
        return complianceNote;
    }

    public String getAssetType() {
        return assetType;
    }

    public CryptoNetwork getNetwork() {
        return network;
    }

    public String getSenderAddress() {
        return senderAddress;
    }

    public String getRecipientAddress() {
        return recipientAddress;
    }

    public BigDecimal getTotalDeduction() {
        return totalDeduction;
    }

    public String getTxHash() {
        return txHash;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public UUID id() {
        return id;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public UUID sourceUserId() {
        return sourceUserId;
    }

    public TransferType transferType() {
        return transferType;
    }

    public TransferStatus status() {
        return status;
    }

    public String sourceCurrency() {
        return sourceCurrency;
    }

    public String targetCurrency() {
        return targetCurrency;
    }

    public BigDecimal sourceAmount() {
        return sourceAmount;
    }

    public BigDecimal feeAmount() {
        return feeAmount;
    }

    public BigDecimal exchangeRate() {
        return exchangeRate;
    }

    public BigDecimal estimatedTargetAmount() {
        return estimatedTargetAmount;
    }

    public String recipientName() {
        return recipientName;
    }

    public String destinationCountry() {
        return destinationCountry;
    }

    public String destinationReference() {
        return destinationReference;
    }

    public String complianceNote() {
        return complianceNote;
    }
}
