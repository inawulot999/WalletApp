package com.inawulot.wallet.dto;

import com.inawulot.wallet.domain.CryptoNetwork;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record SendWalletRequest(
        @NotNull UUID userId,
        @NotBlank String asset,
        @NotNull CryptoNetwork network,
        @NotBlank @Size(max = 96) String recipientAddress,
        @NotNull @DecimalMin(value = "0.00000001") BigDecimal amount,
        @NotBlank @Size(min = 4, max = 16) String verificationCode,
        @Size(max = 160) String recipientLabel,
        @Size(max = 512) String memo
) {
}
