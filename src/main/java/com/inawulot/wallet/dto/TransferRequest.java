package com.inawulot.wallet.dto;

import com.inawulot.wallet.domain.TransferType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(
        @NotNull UUID sourceUserId,
        @NotBlank String sourceCurrency,
        @NotBlank String targetCurrency,
        @NotNull @DecimalMin(value = "0.01") BigDecimal sourceAmount,
        @NotNull TransferType transferType,
        @NotBlank String recipientName,
        @NotBlank String destinationCountry,
        @NotBlank String destinationReference
) {
}
