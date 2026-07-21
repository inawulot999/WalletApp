package com.inawulot.wallet.service;

import com.inawulot.wallet.domain.LedgerEntry;
import com.inawulot.wallet.domain.LedgerSide;
import com.inawulot.wallet.domain.MoneyAccount;
import com.inawulot.wallet.exception.InsufficientFundsException;
import com.inawulot.wallet.repository.LedgerEntryRepository;
import com.inawulot.wallet.repository.MoneyAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {
    private final UserService userService;
    private final MoneyAccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final InputSanitizer inputSanitizer;

    public WalletService(
            UserService userService,
            MoneyAccountRepository accountRepository,
            LedgerEntryRepository ledgerEntryRepository,
            InputSanitizer inputSanitizer
    ) {
        this.userService = userService;
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.inputSanitizer = inputSanitizer;
    }

    @Transactional
    public synchronized MoneyAccount fundUserWallet(UUID userId, String currency, BigDecimal amount, String memo) {
        userService.requireVerified(userId);
        String normalizedCurrency = normalizeCurrency(currency);
        MoneyAccount fundingAccount = getOrCreateInternalAccount("FUNDING", normalizedCurrency);
        MoneyAccount userAccount = getOrCreateUserAccount(userId, normalizedCurrency);
        post(UUID.randomUUID(), fundingAccount, userAccount, normalizeAmount(amount), safeMemo(memo, "Simulated funding"));
        return userAccount;
    }

    @Transactional
    public synchronized void debitUserToSettlement(UUID transactionId, UUID userId, String currency, BigDecimal amount, String memo) {
        userService.requireVerified(userId);
        String normalizedCurrency = normalizeCurrency(currency);
        MoneyAccount userAccount = getOrCreateUserAccount(userId, normalizedCurrency);
        MoneyAccount settlementAccount = getOrCreateInternalAccount("PAYOUT_SETTLEMENT", normalizedCurrency);
        post(transactionId, userAccount, settlementAccount, normalizeAmount(amount), memo);
    }

    @Transactional
    public synchronized MoneyAccount debitUserAsset(UUID transactionId, UUID userId, String asset, BigDecimal totalDeduction, String memo) {
        userService.requireVerified(userId);
        String normalizedAsset = normalizeCurrency(asset);
        MoneyAccount userAccount = getOrCreateUserAccount(userId, normalizedAsset);
        MoneyAccount networkSettlementAccount = getOrCreateInternalAccount("NETWORK_SETTLEMENT", normalizedAsset);
        post(transactionId, userAccount, networkSettlementAccount, normalizeAmount(totalDeduction), memo);
        return userAccount;
    }

    public List<MoneyAccount> getUserWallets(UUID userId) {
        userService.getUser(userId);
        return accountRepository.findByOwnerReferenceOrderByCurrencyAsc(userOwnerReference(userId));
    }

    public MoneyAccount getOrCreateUserWallet(UUID userId, String currency) {
        userService.getUser(userId);
        return getOrCreateUserAccount(userId, normalizeCurrency(currency));
    }

    public BigDecimal getBalance(UUID userId, String currency) {
        MoneyAccount account = getOrCreateUserAccount(userId, normalizeCurrency(currency));
        return account.getBalance();
    }

    public List<LedgerEntry> getUserLedger(UUID userId) {
        userService.getUser(userId);
        List<String> accountReferences = accountRepository.findByOwnerReferenceOrderByCurrencyAsc(userOwnerReference(userId))
                .stream()
                .map(MoneyAccount::getReference)
                .toList();
        if (accountReferences.isEmpty()) {
            return List.of();
        }
        return ledgerEntryRepository.findByAccountReferenceInOrderByCreatedAtDesc(accountReferences);
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
        accountRepository.save(debitAccount);
        ledgerEntryRepository.save(new LedgerEntry(
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
        accountRepository.save(creditAccount);
        ledgerEntryRepository.save(new LedgerEntry(
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
        return accountRepository.findById(reference)
                .orElseGet(() -> accountRepository.save(new MoneyAccount(reference, userOwnerReference(userId), currency)));
    }

    private MoneyAccount getOrCreateInternalAccount(String name, String currency) {
        String reference = "PLATFORM:" + name + ":" + currency;
        return accountRepository.findById(reference)
                .orElseGet(() -> accountRepository.save(new MoneyAccount(reference, "PLATFORM", currency)));
    }

    private String userOwnerReference(UUID userId) {
        return "USER:" + userId;
    }

    private String normalizeCurrency(String currency) {
        return inputSanitizer.currency(currency);
    }

    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount.setScale(8, RoundingMode.HALF_UP);
    }

    private String safeMemo(String memo, String fallback) {
        if (memo == null || memo.isBlank()) {
            return fallback;
        }
        return memo.trim();
    }
}
