package com.inawulot.wallet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "wallet_users")
public class WalletUser {
    @Id
    private UUID id;
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String phoneNumber;
    @Column(nullable = false, length = 2)
    private String country;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private KycStatus kycStatus;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletStatus walletStatus;
    @Column(nullable = false)
    private String passwordHash;
    @Column(nullable = false)
    private String transactionPinHash;
    private String profileImageUrl;
    private boolean twoFactorAuthenticatorEnabled;
    private boolean fingerprintEnabled;
    private boolean pinLockEnabled;
    private boolean immediateLockEnabled;
    private String twoFactorSecret;
    private String bvn;
    private String nin;
    @Column(length = 512)
    private String residentialAddress;

    protected WalletUser() {
    }

    public WalletUser(UUID id, Instant createdAt, String fullName, String email, String phoneNumber, String country) {
        this(id, createdAt, fullName, email, phoneNumber, country, "DEMO_PASSWORDLESS_HASH", "DEMO_PIN_HASH");
    }

    public WalletUser(
            UUID id,
            Instant createdAt,
            String fullName,
            String email,
            String phoneNumber,
            String country,
            String passwordHash,
            String transactionPinHash
    ) {
        this.id = id;
        this.createdAt = createdAt;
        this.fullName = fullName;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.country = country;
        this.kycStatus = KycStatus.PENDING;
        this.walletStatus = WalletStatus.ACTIVE;
        this.passwordHash = passwordHash;
        this.transactionPinHash = transactionPinHash;
        this.profileImageUrl = "";
        this.twoFactorAuthenticatorEnabled = true;
        this.fingerprintEnabled = true;
        this.pinLockEnabled = true;
        this.immediateLockEnabled = true;
        this.twoFactorSecret = "DEMO-OTP-123456";
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

    public WalletStatus getWalletStatus() {
        return walletStatus;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getTransactionPinHash() {
        return transactionPinHash;
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

    public String getTwoFactorSecret() {
        return twoFactorSecret;
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

    public void updateTransactionPinHash(String transactionPinHash) {
        this.transactionPinHash = transactionPinHash;
    }

    public void approveKyc() {
        this.kycStatus = KycStatus.VERIFIED;
    }

    public void lockWallet() {
        this.walletStatus = WalletStatus.LOCKED;
    }

    public void activateWallet() {
        this.walletStatus = WalletStatus.ACTIVE;
    }
}
