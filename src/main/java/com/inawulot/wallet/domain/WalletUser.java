package com.inawulot.wallet.domain;

import java.time.Instant;
import java.util.UUID;

public class WalletUser {
    private final UUID id;
    private final Instant createdAt;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String country;
    private KycStatus kycStatus;
    private String profileImageUrl;
    private boolean twoFactorAuthenticatorEnabled;
    private boolean fingerprintEnabled;
    private boolean pinLockEnabled;
    private boolean immediateLockEnabled;
    private String bvn;
    private String nin;
    private String residentialAddress;

    public WalletUser(UUID id, Instant createdAt, String fullName, String email, String phoneNumber, String country) {
        this.id = id;
        this.createdAt = createdAt;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.country = country;
        this.kycStatus = KycStatus.PENDING;
        this.profileImageUrl = "";
        this.twoFactorAuthenticatorEnabled = true;
        this.fingerprintEnabled = true;
        this.pinLockEnabled = true;
        this.immediateLockEnabled = true;
    }

    public UUID getId() {
        return id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCountry() {
        return country;
    }

    public KycStatus getKycStatus() {
        return kycStatus;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public boolean isTwoFactorAuthenticatorEnabled() {
        return twoFactorAuthenticatorEnabled;
    }

    public boolean isFingerprintEnabled() {
        return fingerprintEnabled;
    }

    public boolean isPinLockEnabled() {
        return pinLockEnabled;
    }

    public boolean isImmediateLockEnabled() {
        return immediateLockEnabled;
    }

    public String getBvn() {
        return bvn;
    }

    public String getNin() {
        return nin;
    }

    public String getResidentialAddress() {
        return residentialAddress;
    }

    public void submitKyc(String bvn, String nin, String residentialAddress) {
        this.bvn = bvn;
        this.nin = nin;
        this.residentialAddress = residentialAddress;
        this.kycStatus = KycStatus.SUBMITTED;
    }

    public void updateProfile(String fullName, String email, String phoneNumber, String country, String residentialAddress) {
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.country = country;
        this.residentialAddress = residentialAddress;
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public void updateSecuritySettings(
            boolean twoFactorAuthenticatorEnabled,
            boolean fingerprintEnabled,
            boolean pinLockEnabled,
            boolean immediateLockEnabled
    ) {
        this.twoFactorAuthenticatorEnabled = twoFactorAuthenticatorEnabled;
        this.fingerprintEnabled = fingerprintEnabled;
        this.pinLockEnabled = pinLockEnabled;
        this.immediateLockEnabled = immediateLockEnabled;
    }

    public void approveKyc() {
        this.kycStatus = KycStatus.VERIFIED;
    }
}
