package com.inawulot.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record ConvertRequest(
        @NotBlank String sourceAsset,
        @NotBlank String targetAsset,
        @NotNull @DecimalMin(value = "0.00000001") BigDecimal sourceAmount,
        @NotBlank @Size(min = 4, max = 16) String verificationCode
) { }
