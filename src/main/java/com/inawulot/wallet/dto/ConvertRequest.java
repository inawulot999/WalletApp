package com.inawulot.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record ConvertRequest(
        @NotBlank String sourceAsset,
        @NotBlank String targetAsset,
        @NotNull @DecimalMin(value = "0.01") BigDecimal sourceAmount
) { }
