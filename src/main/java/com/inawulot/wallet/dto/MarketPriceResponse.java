package com.inawulot.wallet.dto;

import java.math.BigDecimal;

public record MarketPriceResponse(
        String pair,
        BigDecimal price,
        BigDecimal changePercent24h,
        BigDecimal high24h,
        BigDecimal low24h
) {
}
