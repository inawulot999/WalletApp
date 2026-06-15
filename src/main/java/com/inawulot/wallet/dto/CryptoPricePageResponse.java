package com.inawulot.wallet.dto;

import java.time.Instant;
import java.util.List;

public record CryptoPricePageResponse(
        List<CryptoPriceResponse> prices,
        int page,
        int perPage,
        String vsCurrency,
        Instant fetchedAt,
        String source
) {
}
