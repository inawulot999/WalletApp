package com.inawulot.wallet.domain;

import java.math.BigDecimal;
import java.util.Objects;

public class MoneyAccount {
    private final String reference;
    private final String ownerReference;
    private final String currency;
    private BigDecimal balance;

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
