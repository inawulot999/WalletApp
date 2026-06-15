package com.inawulot.wallet.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(CryptoPriceProperties.class)
public class RestClientConfig {
    @Bean
    RestClient.Builder cryptoPriceRestClientBuilder(CryptoPriceProperties properties) {
        return RestClient.builder()
                .baseUrl(properties.providerUrl())
                .defaultHeader("Accept", "application/json");
    }
}
