package com.inawulot.wallet.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "crypto_wallet_addresses",
        uniqueConstraints = @UniqueConstraint(name = "uk_wallet_address_user_network", columnNames = {"user_id", "network"})
)
public class CryptoWalletAddress {
    @Id
    private UUID id;
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CryptoNetwork network;
    @Column(nullable = false, unique = true)
    private String address;
    @Column(nullable = false)
    private String encryptedPrivateKeyReference;
    @Column(nullable = false)
    private String seedDerivationPath;
    @Column(nullable = false)
    private Instant createdAt;

    protected CryptoWalletAddress() {
    }

    public CryptoWalletAddress(
            UUID id,
            UUID userId,
            CryptoNetwork network,
            String address,
            String encryptedPrivateKeyReference,
            String seedDerivationPath,
            Instant createdAt
    ) {
        this.id = id;
        this.userId = userId;
        this.network = network;
        this.address = address;
        this.encryptedPrivateKeyReference = encryptedPrivateKeyReference;
        this.seedDerivationPath = seedDerivationPath;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public CryptoNetwork getNetwork() {
        return network;
    }

    public String getAddress() {
        return address;
    }

    public String getEncryptedPrivateKeyReference() {
        return encryptedPrivateKeyReference;
    }

    public String getSeedDerivationPath() {
        return seedDerivationPath;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
