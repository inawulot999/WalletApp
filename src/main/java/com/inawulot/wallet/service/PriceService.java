package com.inawulot.wallet.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inawulot.wallet.dto.MarketPriceResponse;
import com.inawulot.wallet.dto.UsdtPricesResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PriceService {
    private static final String BINANCE_24HR_URL = "https://api.binance.com/api/v3/ticker/24hr?symbols=%5B%22BTCUSDT%22,%22ETHUSDT%22,%22USDCUSDT%22%5D";
    private static final Duration STALE_AFTER = Duration.ofSeconds(90);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();
    private final ObjectMapper objectMapper;
    private final AtomicReference<UsdtPricesResponse> cache = new AtomicReference<>(fallbackSnapshot());

    public PriceService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public UsdtPricesResponse getUsdtPrices() {
        UsdtPricesResponse snapshot = cache.get();
        boolean stale = snapshot.updatedAt().plus(STALE_AFTER).isBefore(Instant.now());
        return new UsdtPricesResponse(snapshot.pairs(), snapshot.updatedAt(), stale, snapshot.source());
    }

    @Scheduled(initialDelay = 5000, fixedDelay = 60000)
    public void refreshPrices() {
        try {
            HttpRequest request = HttpRequest.newBuilder(URI.create(BINANCE_24HR_URL))
                    .timeout(Duration.ofSeconds(5))
                    .header("accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                return;
            }
            JsonNode payload = objectMapper.readTree(response.body());
            MarketPriceResponse btc = null;
            MarketPriceResponse eth = null;
            MarketPriceResponse usdtUsd = null;
            for (JsonNode ticker : payload) {
                String symbol = ticker.path("symbol").asText();
                if ("BTCUSDT".equals(symbol)) {
                    btc = market("BTC/USDT", ticker);
                } else if ("ETHUSDT".equals(symbol)) {
                    eth = market("ETH/USDT", ticker);
                } else if ("USDCUSDT".equals(symbol)) {
                    usdtUsd = inverseStableMarket(ticker);
                }
            }
            if (btc != null && eth != null && usdtUsd != null) {
                cache.set(new UsdtPricesResponse(List.of(usdtUsd, btc, eth), Instant.now(), false, "BINANCE_REST"));
            }
        } catch (Exception ignored) {
            // Keep serving the last good snapshot or built-in fallback.
        }
    }

    private MarketPriceResponse market(String pair, JsonNode ticker) {
        return new MarketPriceResponse(
                pair,
                number(ticker, "lastPrice"),
                number(ticker, "priceChangePercent"),
                number(ticker, "highPrice"),
                number(ticker, "lowPrice")
        );
    }

    private MarketPriceResponse inverseStableMarket(JsonNode ticker) {
        BigDecimal usdcUsdt = number(ticker, "lastPrice");
        BigDecimal price = BigDecimal.ONE.divide(usdcUsdt, 8, RoundingMode.HALF_UP);
        BigDecimal high = BigDecimal.ONE.divide(number(ticker, "lowPrice"), 8, RoundingMode.HALF_UP);
        BigDecimal low = BigDecimal.ONE.divide(number(ticker, "highPrice"), 8, RoundingMode.HALF_UP);
        BigDecimal change = number(ticker, "priceChangePercent").negate();
        return new MarketPriceResponse("USDT/USD", price, change, high, low);
    }

    private BigDecimal number(JsonNode node, String field) {
        return new BigDecimal(node.path(field).asText("0")).setScale(8, RoundingMode.HALF_UP);
    }

    private static UsdtPricesResponse fallbackSnapshot() {
        return new UsdtPricesResponse(
                List.of(
                        new MarketPriceResponse("USDT/USD", new BigDecimal("1.00000000"), BigDecimal.ZERO.setScale(8), new BigDecimal("1.00050000"), new BigDecimal("0.99950000")),
                        new MarketPriceResponse("BTC/USDT", new BigDecimal("65000.00000000"), BigDecimal.ZERO.setScale(8), new BigDecimal("66000.00000000"), new BigDecimal("64000.00000000")),
                        new MarketPriceResponse("ETH/USDT", new BigDecimal("3500.00000000"), BigDecimal.ZERO.setScale(8), new BigDecimal("3600.00000000"), new BigDecimal("3400.00000000"))
                ),
                Instant.now(),
                true,
                "BUILT_IN_FALLBACK"
        );
    }
}
