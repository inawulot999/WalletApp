package com.inawulot.wallet.service;

import com.inawulot.wallet.domain.LedgerEntry;
import com.inawulot.wallet.domain.LedgerSide;
import com.inawulot.wallet.domain.MoneyAccount;
import com.inawulot.wallet.exception.InsufficientFundsException;
import com.inawulot.wallet.exception.NotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WalletService {
    private final UserService userService;
    private final Map<String, MoneyAccount> accounts = new ConcurrentHashMap<>();
    private final List<LedgerEntry> ledger = new ArrayList<>();

    public WalletService(UserService userService) {
        this.userService = userService;
    }

    public synchronized MoneyAccount fundUserWallet(UUID userId, String currency, BigDecimal amount, String memo) {
        userService.requireVerified(userId);
        String normalizedCurrency = normalizeCurrency(currency);
        MoneyAccount fundingAccount = getOrCreateInternalAccount("FUNDING", normalizedCurrency);
        MoneyAccount userAccount = getOrCreateUserAccount(userId, normalizedCurrency);
        post(UUID.randomUUID(), fundingAccount, userAccount, normalizeAmount(amount), safeMemo(memo, "Simulated funding"));
        return userAccount;
    }

    public synchronized void debitUserToSettlement(UUID transactionId, UUID userId, String currency, BigDecimal amount, String memo) {
        userService.requireVerified(userId);
        String normalizedCurrency = normalizeCurrency(currency);
        MoneyAccount userAccount = getOrCreateUserAccount(userId, normalizedCurrency);
        MoneyAccount settlementAccount = getOrCreateInternalAccount("PAYOUT_SETTLEMENT", normalizedCurrency);
        post(transactionId, userAccount, settlementAccount, normalizeAmount(amount), memo);
    }

    public List<MoneyAccount> getUserWallets(UUID userId) {
        userService.getUser(userId);
        String ownerReference = userOwnerReference(userId);
        return accounts.values().stream()
                .filter(account -> account.getOwnerReference().equals(ownerReference))
                .sorted(Comparator.comparing(MoneyAccount::getCurrency))
                .toList();
    }

    public List<LedgerEntry> getUserLedger(UUID userId) {
        userService.getUser(userId);
        String ownerReference = userOwnerReference(userId);
        return ledger.stream()
                .filter(entry -> {
                    MoneyAccount account = accounts.get(entry.accountReference());
                    return account != null && account.getOwnerReference().equals(ownerReference);
                })
                .sorted(Comparator.comparing(LedgerEntry::createdAt).reversed())
                .toList();
    }

    private void post(UUID transactionId, MoneyAccount debitAccount, MoneyAccount creditAccount, BigDecimal amount, String memo) {
        debitAccount.requireSameCurrency(creditAccount);
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
        if (!debitAccount.isInternal() && debitAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientFundsException("Insufficient wallet balance");
        }

        debitAccount.debit(amount);
        ledger.add(new LedgerEntry(
                UUID.randomUUID(),
                transactionId,
                Instant.now(),
                debitAccount.getReference(),
                debitAccount.getCurrency(),
                LedgerSide.DEBIT,
                amount,
                debitAccount.getBalance(),
                memo
        ));

        creditAccount.credit(amount);
        ledger.add(new LedgerEntry(
                UUID.randomUUID(),
                transactionId,
                Instant.now(),
                creditAccount.getReference(),
                creditAccount.getCurrency(),
                LedgerSide.CREDIT,
                amount,
                creditAccount.getBalance(),
                memo
        ));
    }

    private MoneyAccount getOrCreateUserAccount(UUID userId, String currency) {
        userService.getUser(userId);
        String reference = "USER:" + userId + ":" + currency;
        return accounts.computeIfAbsent(reference, key -> new MoneyAccount(key, userOwnerReference(userId), currency));
    }

    private MoneyAccount getOrCreateInternalAccount(String name, String currency) {
        String reference = "PLATFORM:" + name + ":" + currency;
        return accounts.computeIfAbsent(reference, key -> new MoneyAccount(key, "PLATFORM", currency));
    }

    private String userOwnerReference(UUID userId) {
        return "USER:" + userId;
    }

    private String normalizeCurrency(String currency) {
        String normalized = currency.trim().toUpperCase();
        if (!normalized.matches("[A-Z0-9]{3,5}")) {
            throw new IllegalArgumentException("Currency must use a 3 to 5 character code");
        }
        return normalized;
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String safeMemo(String memo, String fallback) {
        if (memo == null || memo.isBlank()) {
            return fallback;
        }
        return memo.trim();
    }
}
