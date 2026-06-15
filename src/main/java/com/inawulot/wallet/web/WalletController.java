package com.inawulot.wallet.web;

import com.inawulot.wallet.dto.FundWalletRequest;
import com.inawulot.wallet.dto.LedgerEntryResponse;
import com.inawulot.wallet.dto.WalletBalanceResponse;
import com.inawulot.wallet.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {
    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/{userId}/fund")
    public WalletBalanceResponse fund(@PathVariable UUID userId, @Valid @RequestBody FundWalletRequest request) {
        var account = walletService.fundUserWallet(userId, request.currency(), request.amount(), request.memo());
        return new WalletBalanceResponse(account.getReference(), account.getCurrency(), account.getBalance());
    }

    @GetMapping("/{userId}")
    public List<WalletBalanceResponse> balances(@PathVariable UUID userId) {
        return walletService.getUserWallets(userId).stream()
                .map(account -> new WalletBalanceResponse(account.getReference(), account.getCurrency(), account.getBalance()))
                .toList();
    }

    @GetMapping("/{userId}/ledger")
    public List<LedgerEntryResponse> ledger(@PathVariable UUID userId) {
        return walletService.getUserLedger(userId).stream()
                .map(LedgerEntryResponse::from)
                .toList();
    }
}
