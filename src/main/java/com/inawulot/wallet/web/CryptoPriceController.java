package com.inawulot.wallet.web;

import com.inawulot.wallet.dto.CryptoPricePageResponse;
import com.inawulot.wallet.dto.CryptoPriceResponse;
import com.inawulot.wallet.service.CryptoPriceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crypto")
public class CryptoPriceController {
    private final CryptoPriceService cryptoPriceService;

    public CryptoPriceController(CryptoPriceService cryptoPriceService) {
        this.cryptoPriceService = cryptoPriceService;
    }

    @GetMapping("/prices")
    public CryptoPricePageResponse listPrices(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "100") int perPage,
            @RequestParam(required = false) String vsCurrency
    ) {
        return cryptoPriceService.getMarketPrices(page, perPage, vsCurrency);
    }

    @GetMapping("/prices/{coinId}")
    public CryptoPriceResponse getPrice(
            @PathVariable String coinId,
            @RequestParam(required = false) String vsCurrency
    ) {
        return cryptoPriceService.getPrice(coinId, vsCurrency);
    }
}
