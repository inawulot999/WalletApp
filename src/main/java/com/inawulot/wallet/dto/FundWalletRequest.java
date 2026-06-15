package com.inawulot.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record FundWalletRequest(
        @NotBlank String currency,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        String memo
) {
}
