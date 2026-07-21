package com.inawulot.wallet.web;

import com.inawulot.wallet.dto.AssetBalanceResponse;
import com.inawulot.wallet.dto.SendWalletRequest;
import com.inawulot.wallet.dto.SendWalletResponse;
import com.inawulot.wallet.dto.V1WalletBalanceResponse;
import com.inawulot.wallet.dto.WalletAddressResponse;
import com.inawulot.wallet.service.RateLimitService;
import com.inawulot.wallet.service.TransferService;
import com.inawulot.wallet.service.WalletAddressService;
import com.inawulot.wallet.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallet")
public class V1WalletController {
    private final WalletService walletService;
    private final WalletAddressService walletAddressService;
    private final TransferService transferService;
    private final RateLimitService rateLimitService;

    public V1WalletController(
            WalletService walletService,
            WalletAddressService walletAddressService,
            TransferService transferService,
            RateLimitService rateLimitService
    ) {
        this.walletService = walletService;
        this.walletAddressService = walletAddressService;
        this.transferService = transferService;
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/balance")
    public V1WalletBalanceResponse balances(@RequestParam UUID userId) {
        walletService.getOrCreateUserWallet(userId, "USDT");
        return new V1WalletBalanceResponse(
                userId,
                walletService.getUserWallets(userId).stream()
                        .map(AssetBalanceResponse::from)
                        .toList(),
                walletAddressService.ensureUserAddresses(userId).stream()
                        .map(WalletAddressResponse::from)
                        .toList()
        );
    }

    @PostMapping("/send")
    public SendWalletResponse send(
            @Valid @RequestBody SendWalletRequest request,
            HttpServletRequest servletRequest
    ) {
        rateLimitService.check(servletRequest.getRemoteAddr() + ":wallet-send:" + request.userId());
        return SendWalletResponse.from(transferService.sendWalletTransfer(request));
    }
}
