package com.inawulot.wallet.dto;

import java.time.Instant;
import java.util.List;

public record UsdtPricesResponse(
        List<MarketPriceResponse> pairs,
        Instant updatedAt,
        boolean stale,
        String source
) {
}
