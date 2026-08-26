package com.inawulot.wallet.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "phone_login_otps")
public class PhoneLoginOtp {
    @Id
    private String phoneNumber;
    private String codeHash;
    private Instant expiresAt;
    private int failedAttempts;

    protected PhoneLoginOtp() {
    }

    public PhoneLoginOtp(String phoneNumber, String codeHash, Instant expiresAt) {
        this.phoneNumber = phoneNumber;
        this.codeHash = codeHash;
        this.expiresAt = expiresAt;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getCodeHash() {
        return codeHash;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public int getFailedAttempts() {
        return failedAttempts;
    }

    public void recordFailedAttempt() {
        failedAttempts++;
    }
}
