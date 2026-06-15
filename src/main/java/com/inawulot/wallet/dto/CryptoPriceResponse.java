package com.inawulot.wallet.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record CryptoPriceResponse(
        String id,
        String symbol,
        String name,
        String imageUrl,
        String vsCurrency,
        BigDecimal currentPrice,
        BigDecimal marketCap,
        BigDecimal totalVolume,
        BigDecimal priceChangePercentage24h,
        Instant lastUpdated
) {
}
