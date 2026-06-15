package com.inawulot.wallet.dto;

import com.inawulot.wallet.domain.TransferType;

import java.math.BigDecimal;

public record QuoteResponse(
        String sourceCurrency,
        String targetCurrency,
        BigDecimal sourceAmount,
        BigDecimal feeAmount,
        BigDecimal exchangeRate,
        BigDecimal estimatedTargetAmount,
        TransferType transferType,
        String rateSource
) {
}
