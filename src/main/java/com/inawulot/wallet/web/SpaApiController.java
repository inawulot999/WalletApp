package com.inawulot.wallet.web;

import com.inawulot.wallet.domain.TransferStatus;
import com.inawulot.wallet.domain.TransferType;
import com.inawulot.wallet.dto.ConvertRequest;
import com.inawulot.wallet.dto.ExecuteConversionRequest;
import com.inawulot.wallet.dto.QuoteRequest;
import com.inawulot.wallet.dto.TransferResponse;
import com.inawulot.wallet.dto.UpdatePreferencesRequest;
import com.inawulot.wallet.dto.TwoFactorConfirmRequest;
import com.inawulot.wallet.security.CurrentUser;
import com.inawulot.wallet.service.PriceService;
import com.inawulot.wallet.service.QuoteService;
import com.inawulot.wallet.service.RateLimitService;
import com.inawulot.wallet.service.TransferService;
import com.inawulot.wallet.service.UserService;
import com.inawulot.wallet.service.WalletService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SpaApiController {
    private static final BigDecimal USD_TO_NGN = new BigDecimal("1600.00");
    private static final Map<String, BigDecimal> USD_VALUES = Map.of("BTC", new BigDecimal("65000"), "ETH", new BigDecimal("3540"), "SOL", new BigDecimal("142.8"), "USDT", BigDecimal.ONE, "USD", BigDecimal.ONE, "NGN", BigDecimal.ONE.divide(USD_TO_NGN, 12, RoundingMode.HALF_UP));
    private final CurrentUser currentUser;
    private final UserService users;
    private final WalletService wallets;
    private final TransferService transfers;
    private final QuoteService quotes;
    private final PriceService prices;
    private final RateLimitService limits;

    public SpaApiController(CurrentUser currentUser, UserService users, WalletService wallets, TransferService transfers, QuoteService quotes, PriceService prices, RateLimitService limits) {
        this.currentUser = currentUser; this.users = users; this.wallets = wallets; this.transfers = transfers; this.quotes = quotes; this.prices = prices; this.limits = limits;
    }

    @GetMapping("/user/profile")
    public Map<String, Object> profile(Authentication authentication) {
        var user = users.getUser(currentUser.id(authentication));
        int securityScore = (user.isTwoFactorAuthenticatorEnabled() ? 40 : 0) + (user.isFingerprintEnabled() ? 30 : 0) + (user.isPinLockEnabled() ? 30 : 0);
        return Map.of("id", user.getId(), "email", user.getEmail(), "fullName", user.getFullName(), "kycStatus", user.getKycStatus(), "securityScore", securityScore, "preferredCurrency", user.getPreferredCurrency(), "notificationsEnabled", user.isNotificationsEnabled(), "twoFactorEnabled", user.isTwoFactorAuthenticatorEnabled());
    }

    @PatchMapping("/user/settings")
    public Map<String, Object> settings(@Valid @RequestBody UpdatePreferencesRequest request, Authentication authentication) {
        var user = users.updatePreferences(currentUser.id(authentication), request.preferredCurrency(), request.notificationsEnabled());
        return Map.of("preferredCurrency", user.getPreferredCurrency(), "notificationsEnabled", user.isNotificationsEnabled());
    }

    @PostMapping("/user/2fa/setup")
    public Map<String, String> setupTwoFactor(Authentication authentication) {
        var user = users.getUser(currentUser.id(authentication));
        String secret = users.beginTwoFactorEnrollment(user.getId());
        String label = java.net.URLEncoder.encode("WalletApp:" + user.getEmail(), java.nio.charset.StandardCharsets.UTF_8);
        return Map.of("secret", secret, "uri", "otpauth://totp/" + label + "?secret=" + secret + "&issuer=WalletApp&algorithm=SHA1&digits=6&period=30");
    }

    @PostMapping("/user/2fa/confirm")
    public Map<String, String> confirmTwoFactor(@Valid @RequestBody TwoFactorConfirmRequest request, Authentication authentication) {
        users.confirmTwoFactorEnrollment(currentUser.id(authentication), request.code());
        return Map.of("message", "Authenticator two-factor authentication is enabled.");
    }

    @GetMapping("/wallet/balances")
    public Map<String, Object> balances(Authentication authentication) {
        UUID userId = currentUser.id(authentication);
        wallets.getOrCreateUserWallet(userId, "USDT");
        List<Map<String, Object>> items = wallets.getUserWallets(userId).stream().map(account -> Map.<String, Object>of("asset", account.getCurrency(), "balance", account.getBalance(), "usdValue", account.getBalance().multiply(USD_VALUES.getOrDefault(account.getCurrency(), BigDecimal.ZERO)))).toList();
        BigDecimal totalUsd = items.stream().map(item -> (BigDecimal) item.get("usdValue")).reduce(BigDecimal.ZERO, BigDecimal::add);
        return Map.of("totalUsd", totalUsd, "totalNgn", totalUsd.multiply(USD_TO_NGN), "balances", items);
    }

    @GetMapping("/wallet/transactions")
    public Map<String, Object> transactions(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size, Authentication authentication) {
        var result = transfers.getHistory(currentUser.id(authentication), null, page, size);
        return Map.of("transactions", result.getContent().stream().map(TransferResponse::from).toList(), "page", result.getNumber(), "size", result.getSize(), "totalElements", result.getTotalElements(), "totalPages", result.getTotalPages());
    }

    @GetMapping("/markets/live")
    public Object markets() { return prices.getUsdtPrices(); }

    @GetMapping("/markets/rates")
    public Object rate(@RequestParam String from, @RequestParam String to) { return quotes.quote(new QuoteRequest(from, to, BigDecimal.ONE, TransferType.EXCHANGE_WALLET)); }

    @PostMapping("/convert/execute")
    public TransferResponse execute(@Valid @RequestBody ExecuteConversionRequest request, Authentication authentication, HttpServletRequest http) {
        UUID userId = currentUser.id(authentication);
        limits.check(http.getRemoteAddr() + ":convert:" + userId);
        return TransferResponse.from(transfers.convert(userId, new ConvertRequest(request.fromAsset(), request.toAsset(), request.amount(), request.pinCode())));
    }
}
