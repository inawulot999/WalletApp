package com.inawulot.wallet.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record UpdatePreferencesRequest(
        @NotBlank @Pattern(regexp = "USD|NGN", message = "Preferred currency must be USD or NGN") String preferredCurrency,
        @NotNull Boolean notificationsEnabled
) { }
