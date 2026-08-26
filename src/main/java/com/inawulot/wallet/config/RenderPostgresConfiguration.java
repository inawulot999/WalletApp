package com.inawulot.wallet.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Configuration
@ConditionalOnProperty(name = "DATABASE_URL")
public class RenderPostgresConfiguration {
    @Bean
    @Primary
    DataSource renderPostgresDataSource(@Value("${DATABASE_URL}") String databaseUrl) {
        URI uri = URI.create(databaseUrl);
        String[] credentials = URLDecoder.decode(uri.getUserInfo(), StandardCharsets.UTF_8).split(":", 2);
        if (credentials.length != 2 || uri.getHost() == null) {
            throw new IllegalStateException("Render DATABASE_URL is malformed");
        }
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        HikariDataSource source = new HikariDataSource();
        source.setJdbcUrl("jdbc:postgresql://%s:%d%s".formatted(uri.getHost(), port, uri.getPath()));
        source.setUsername(credentials[0]);
        source.setPassword(credentials[1]);
        source.setMaximumPoolSize(5);
        source.setMinimumIdle(1);
        return source;
    }
}
