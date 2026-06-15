package com.inawulot.wallet.web;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClient;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CryptoPriceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RestClient.Builder cryptoPriceRestClientBuilder;

    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        server = MockRestServiceServer.bindTo(cryptoPriceRestClientBuilder).build();
    }

    @Test
    void listsTrackedCryptoPrices() throws Exception {
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/coins/markets")))
                .andRespond(withSuccess("""
                        [
                          {
                            "id": "bitcoin",
                            "symbol": "btc",
                            "name": "Bitcoin",
                            "image": "https://assets.coingecko.com/coins/images/1/large/bitcoin.png",
                            "current_price": 65000.12,
                            "market_cap": 1200000000000,
                            "total_volume": 25000000000,
                            "price_change_percentage_24h": 1.25,
                            "last_updated": "2026-06-15T10:00:00.000Z"
                          }
                        ]
                        """, MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/crypto/prices"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.perPage").value(100))
                .andExpect(jsonPath("$.vsCurrency").value("usd"))
                .andExpect(jsonPath("$.source").value("COINGECKO_PUBLIC_API"))
                .andExpect(jsonPath("$.prices[0].symbol").value("BTC"))
                .andExpect(jsonPath("$.prices[0].currentPrice").value(65000.12));

        server.verify();
    }

    @Test
    void returnsSingleCryptoPrice() throws Exception {
        server.expect(requestTo(org.hamcrest.Matchers.containsString("/coins/bitcoin?")))
                .andRespond(withSuccess("""
                        {
                          "id": "bitcoin",
                          "symbol": "btc",
                          "name": "Bitcoin",
                          "image": {
                            "large": "https://assets.coingecko.com/coins/images/1/large/bitcoin.png"
                          },
                          "market_data": {
                            "current_price": {
                              "usd": 65000.12
                            },
                            "market_cap": {
                              "usd": 1200000000000
                            },
                            "total_volume": {
                              "usd": 25000000000
                            },
                            "price_change_percentage_24h": 1.25,
                            "last_updated": "2026-06-15T10:00:00.000Z"
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/crypto/prices/bitcoin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("bitcoin"))
                .andExpect(jsonPath("$.symbol").value("BTC"))
                .andExpect(jsonPath("$.currentPrice").value(65000.12));

        server.verify();
    }
}
