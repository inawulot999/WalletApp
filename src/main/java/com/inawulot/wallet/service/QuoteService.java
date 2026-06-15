package com.inawulot.wallet.service;

import com.inawulot.wallet.domain.TransferType;
import com.inawulot.wallet.dto.QuoteRequest;
import com.inawulot.wallet.dto.QuoteResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

@Service
public class QuoteService {
    private static final String SIMULATED_RATE_SOURCE = "SIMULATED_RATE_NOT_FOR_REAL_TRANSACTIONS";

    private final Map<String, BigDecimal> demoRates = Map.of(
            "NGN:USDT", new BigDecimal("0.00060"),
            "NGN:USD", new BigDecimal("0.00060"),
            "NGN:GBP", new BigDecimal("0.00048"),
            "NGN:GHS", new BigDecimal("0.00900"),
            "USDT:NGN", new BigDecimal("1600.00"),
            "USDT:USD", new BigDecimal("1.00"),
            "USD:NGN", new BigDecimal("1600.00"),
            "USD:USDT", new BigDecimal("1.00"),
            "GBP:NGN", new BigDecimal("2050.00"),
            "GHS:NGN", new BigDecimal("105.00")
    );

    public QuoteResponse quote(QuoteRequest request) {
        String sourceCurrency = normalizeCurrency(request.sourceCurrency());
        String targetCurrency = normalizeCurrency(request.targetCurrency());
        BigDecimal sourceAmount = money(request.sourceAmount());
        BigDecimal feeAmount = calculateFee(sourceCurrency, sourceAmount, request.transferType());
        BigDecimal exchangeRate = rate(sourceCurrency, targetCurrency);
        BigDecimal estimatedTargetAmount = sourceAmount.subtract(feeAmount)
                .multiply(exchangeRate)
                .setScale(2, RoundingMode.HALF_UP);

        return new QuoteResponse(
                sourceCurrency,
                targetCurrency,
                sourceAmount,
                feeAmount,
                exchangeRate,
                estimatedTargetAmount,
                request.transferType(),
                SIMULATED_RATE_SOURCE
        );
    }

    private BigDecimal calculateFee(String sourceCurrency, BigDecimal sourceAmount, TransferType transferType) {
        BigDecimal percentage = switch (transferType) {
            case EXCHANGE_WALLET -> new BigDecimal("0.0200");
            case DIOR_WALLET_USER -> new BigDecimal("0.0050");
            case CROSS_BORDER -> new BigDecimal("0.0150");
        };
        BigDecimal percentageFee = sourceAmount.multiply(percentage);
        BigDecimal minimumFee = switch (sourceCurrency) {
            case "NGN" -> new BigDecimal("500.00");
            case "USDT" -> new BigDecimal("1.00");
            case "USD" -> new BigDecimal("2.00");
            case "GBP" -> new BigDecimal("2.00");
            case "GHS" -> new BigDecimal("20.00");
            default -> new BigDecimal("1.00");
        };
        return percentageFee.max(minimumFee).setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal rate(String sourceCurrency, String targetCurrency) {
        if (sourceCurrency.equals(targetCurrency)) {
            return BigDecimal.ONE.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal rate = demoRates.get(sourceCurrency + ":" + targetCurrency);
        if (rate == null) {
            throw new IllegalArgumentException("No simulated FX rate configured for " + sourceCurrency + " to " + targetCurrency);
        }
        return rate;
    }

    private String normalizeCurrency(String currency) {
        String normalized = currency.trim().toUpperCase();
        if (!normalized.matches("[A-Z0-9]{3,5}")) {
            throw new IllegalArgumentException("Currency must use a 3 to 5 character code");
        }
        return normalized;
    }

    private BigDecimal money(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
