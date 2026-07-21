package com.inawulot.wallet.web;

import com.inawulot.wallet.dto.UsdtPricesResponse;
import com.inawulot.wallet.service.PriceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/prices")
public class PriceController {
    private final PriceService priceService;

    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    @GetMapping("/usdt")
    public UsdtPricesResponse usdtPrices() {
        return priceService.getUsdtPrices();
    }
}
