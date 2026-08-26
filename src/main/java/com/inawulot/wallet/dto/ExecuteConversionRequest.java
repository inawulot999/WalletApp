package com.inawulot.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ExecuteConversionRequest(
        @NotBlank String fromAsset,
        @NotBlank String toAsset,
        @NotNull @DecimalMin(value = "0.00000001") BigDecimal amount,
        @NotBlank @Size(min = 4, max = 16) String pinCode
) { }
