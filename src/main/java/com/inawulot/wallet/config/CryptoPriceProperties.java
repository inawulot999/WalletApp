package com.inawulot.wallet.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wallet.crypto")
public record CryptoPriceProperties(
        String providerUrl,
        long cacheTtlSeconds,
        String defaultVsCurrency,
        int maxPerPage
) {
}
