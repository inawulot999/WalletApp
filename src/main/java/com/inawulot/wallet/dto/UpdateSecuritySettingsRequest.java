package com.inawulot.wallet.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateSecuritySettingsRequest(
        @NotNull Boolean twoFactorAuthenticatorEnabled,
        @NotNull Boolean fingerprintEnabled,
        @NotNull Boolean pinLockEnabled,
        @NotNull Boolean immediateLockEnabled
) {
}
