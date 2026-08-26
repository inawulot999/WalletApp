package com.inawulot.wallet.web;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {
    @GetMapping("/")
    public Map<String, Object> status() {
        return Map.of(
                "service", "Nexus Wallet API",
                "status", "online",
                "timestamp", Instant.now().toString(),
                "api", "/api/v1");
    }
}
