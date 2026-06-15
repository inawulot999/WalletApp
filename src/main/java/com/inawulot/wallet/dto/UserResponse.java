package com.inawulot.wallet.dto;

import com.inawulot.wallet.domain.KycStatus;
import com.inawulot.wallet.domain.WalletUser;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        Instant createdAt,
        String fullName,
        String email,
        String phoneNumber,
        String country,
        KycStatus kycStatus,
        String profileImageUrl,
        boolean twoFactorAuthenticatorEnabled,
        boolean fingerprintEnabled,
        boolean pinLockEnabled,
        boolean immediateLockEnabled
) {
    public static UserResponse from(WalletUser user) {
        return new UserResponse(
                user.getId(),
                user.getCreatedAt(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                user.getCountry(),
                user.getKycStatus(),
                user.getProfileImageUrl(),
                user.isTwoFactorAuthenticatorEnabled(),
                user.isFingerprintEnabled(),
                user.isPinLockEnabled(),
                user.isImmediateLockEnabled()
        );
    }
}
