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
@Table(name = "ledger_entries")
public class LedgerEntry {
    @Id
    private UUID id;
    @Column(nullable = false)
    private UUID transactionId;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private String accountReference;
    @Column(nullable = false, length = 12)
    private String currency;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LedgerSide side;
    @Column(nullable = false, precision = 28, scale = 8)
    private BigDecimal amount;
    @Column(nullable = false, precision = 28, scale = 8)
    private BigDecimal balanceAfter;
    @Column(length = 512)
    private String memo;

    protected LedgerEntry() {
    }

    public LedgerEntry(
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
        this.id = id;
        this.transactionId = transactionId;
        this.createdAt = createdAt;
        this.accountReference = accountReference;
        this.currency = currency;
        this.side = side;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.memo = memo;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTransactionId() {
        return transactionId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getAccountReference() {
        return accountReference;
    }

    public String getCurrency() {
        return currency;
    }

    public LedgerSide getSide() {
        return side;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    public String getMemo() {
        return memo;
    }

    public UUID id() {
        return id;
    }

    public UUID transactionId() {
        return transactionId;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String accountReference() {
        return accountReference;
    }

    public String currency() {
        return currency;
    }

    public LedgerSide side() {
        return side;
    }

    public BigDecimal amount() {
        return amount;
    }

    public BigDecimal balanceAfter() {
        return balanceAfter;
    }

    public String memo() {
        return memo;
    }
}
