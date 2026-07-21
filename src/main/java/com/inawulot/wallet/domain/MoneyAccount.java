package com.inawulot.wallet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "money_accounts")
public class MoneyAccount {
    @Id
    private String reference;
    @Column(nullable = false)
    private String ownerReference;
    @Column(nullable = false, length = 12)
    private String currency;
    @Column(nullable = false, precision = 28, scale = 8)
    private BigDecimal balance;

    protected MoneyAccount() {
    }

    public MoneyAccount(String reference, String ownerReference, String currency) {
        this.reference = reference;
        this.ownerReference = ownerReference;
        this.currency = currency.toUpperCase();
        this.balance = BigDecimal.ZERO;
    }

    public String getReference() {
        return reference;
    }

    public String getOwnerReference() {
        return ownerReference;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void debit(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public boolean isInternal() {
        return ownerReference.equals("PLATFORM");
    }

    public void requireSameCurrency(MoneyAccount other) {
        if (!Objects.equals(currency, other.currency)) {
            throw new IllegalArgumentException("Ledger postings must use accounts with the same currency");
        }
    }
}
