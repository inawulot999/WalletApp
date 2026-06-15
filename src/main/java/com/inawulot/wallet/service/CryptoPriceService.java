package com.inawulot.wallet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.inawulot.wallet.config.CryptoPriceProperties;
import com.inawulot.wallet.dto.CryptoPricePageResponse;
import com.inawulot.wallet.dto.CryptoPriceResponse;
import com.inawulot.wallet.exception.ExternalServiceException;
import com.inawulot.wallet.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CryptoPriceService {
    private static final String PRICE_SOURCE = "COINGECKO_PUBLIC_API";

    private final RestClient.Builder restClientBuilder;
    private final CryptoPriceProperties properties;
    private final Map<String, CachedPage> pageCache = new ConcurrentHashMap<>();

    public CryptoPriceService(RestClient.Builder cryptoPriceRestClientBuilder, CryptoPriceProperties properties) {
        this.restClientBuilder = cryptoPriceRestClientBuilder;
        this.properties = properties;
    }

    public CryptoPricePageResponse getMarketPrices(int page, int perPage, String vsCurrency) {
        int safePage = Math.max(page, 1);
        int safePerPage = Math.min(Math.max(perPage, 1), properties.maxPerPage());
        String normalizedCurrency = normalizeCurrency(vsCurrency);
        String cacheKey = normalizedCurrency + ":" + safePage + ":" + safePerPage;

        CachedPage cached = pageCache.get(cacheKey);
        if (cached != null && cached.isFresh(properties.cacheTtlSeconds())) {
            return cached.response();
        }

        JsonNode payload = fetchMarkets(safePage, safePerPage, normalizedCurrency);
        List<CryptoPriceResponse> prices = new ArrayList<>();
        for (JsonNode node : payload) {
            prices.add(toMarketPrice(node, normalizedCurrency));
        }

        CryptoPricePageResponse response = new CryptoPricePageResponse(
                List.copyOf(prices),
                safePage,
                safePerPage,
                normalizedCurrency,
                Instant.now(),
                PRICE_SOURCE
        );
        pageCache.put(cacheKey, new CachedPage(response));
        return response;
    }

    public CryptoPriceResponse getPrice(String coinId, String vsCurrency) {
        String normalizedCoinId = normalizeCoinId(coinId);
        String normalizedCurrency = normalizeCurrency(vsCurrency);

        JsonNode payload;
        try {
            payload = client().get()
                    .uri("/coins/{coinId}?localization=false&tickers=false&market_data=true&community_data=false&developer_data=false",
                            normalizedCoinId)
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientException exception) {
            throw new ExternalServiceException("Unable to fetch crypto price for " + normalizedCoinId, exception);
        }

        if (payload == null || payload.isMissingNode()) {
            throw new NotFoundException("Crypto asset not found: " + normalizedCoinId);
        }

        JsonNode marketData = payload.path("market_data");
        if (marketData.isMissingNode()) {
            throw new NotFoundException("Crypto asset not found: " + normalizedCoinId);
        }

        JsonNode currentPriceNode = marketData.path("current_price").path(normalizedCurrency);
        if (currentPriceNode.isMissingNode() || currentPriceNode.isNull()) {
            throw new NotFoundException("Price not available for " + normalizedCoinId + " in " + normalizedCurrency.toUpperCase(Locale.ROOT));
        }

        return new CryptoPriceResponse(
                payload.path("id").asText(normalizedCoinId),
                payload.path("symbol").asText().toUpperCase(Locale.ROOT),
                payload.path("name").asText(),
                payload.path("image").path("large").asText(null),
                normalizedCurrency,
                decimal(currentPriceNode),
                decimal(marketData.path("market_cap").path(normalizedCurrency)),
                decimal(marketData.path("total_volume").path(normalizedCurrency)),
                decimal(marketData.path("price_change_percentage_24h")),
                parseInstant(marketData.path("last_updated").asText(null))
        );
    }

    private JsonNode fetchMarkets(int page, int perPage, String vsCurrency) {
        String uri = UriComponentsBuilder.fromPath("/coins/markets")
                .queryParam("vs_currency", vsCurrency)
                .queryParam("order", "market_cap_desc")
                .queryParam("per_page", perPage)
                .queryParam("page", page)
                .queryParam("sparkline", false)
                .queryParam("price_change_percentage", "24h")
                .build()
                .toUriString();

        try {
            JsonNode payload = client().get()
                    .uri(uri)
                    .retrieve()
                    .body(JsonNode.class);
            if (payload == null || !payload.isArray()) {
                throw new ExternalServiceException("Crypto price provider returned an unexpected response");
            }
            return payload;
        } catch (RestClientException exception) {
            throw new ExternalServiceException("Unable to fetch crypto market prices", exception);
        }
    }

    private CryptoPriceResponse toMarketPrice(JsonNode node, String vsCurrency) {
        return new CryptoPriceResponse(
                node.path("id").asText(),
                node.path("symbol").asText().toUpperCase(Locale.ROOT),
                node.path("name").asText(),
                node.path("image").asText(null),
                vsCurrency,
                decimal(node.path("current_price")),
                decimal(node.path("market_cap")),
                decimal(node.path("total_volume")),
                decimal(node.path("price_change_percentage_24h")),
                parseInstant(node.path("last_updated").asText(null))
        );
    }

    private BigDecimal decimal(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        return node.decimalValue();
    }

    private Instant parseInstant(String value) {
        if (value == null || value.isBlank()) {
            return Instant.now();
        }
        return Instant.parse(value);
    }

    private String normalizeCurrency(String vsCurrency) {
        if (vsCurrency == null || vsCurrency.isBlank()) {
            return properties.defaultVsCurrency().toLowerCase(Locale.ROOT);
        }
        return vsCurrency.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeCoinId(String coinId) {
        if (coinId == null || coinId.isBlank()) {
            throw new IllegalArgumentException("coinId is required");
        }
        return coinId.trim().toLowerCase(Locale.ROOT);
    }

    private RestClient client() {
        return restClientBuilder.build();
    }

    private record CachedPage(CryptoPricePageResponse response, Instant cachedAt) {
        CachedPage(CryptoPricePageResponse response) {
            this(response, Instant.now());
        }

        boolean isFresh(long ttlSeconds) {
            return cachedAt.plusSeconds(ttlSeconds).isAfter(Instant.now());
        }
    }
}
