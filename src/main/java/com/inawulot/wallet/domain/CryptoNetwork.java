package com.inawulot.wallet.domain;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public enum CryptoNetwork {
    TRC20("TRON", "Tether USD", new BigDecimal("1.00"), Pattern.compile("^T[1-9A-HJ-NP-Za-km-z]{33}$")),
    ERC20("Ethereum", "Tether USD", new BigDecimal("5.00"), Pattern.compile("^0x[a-fA-F0-9]{40}$")),
    BEP20("BNB Smart Chain", "Tether USD", new BigDecimal("0.30"), Pattern.compile("^0x[a-fA-F0-9]{40}$")),
    POLYGON("Polygon", "Tether USD", new BigDecimal("0.10"), Pattern.compile("^0x[a-fA-F0-9]{40}$"));

    private final String displayName;
    private final String assetName;
    private final BigDecimal estimatedFee;
    private final Pattern addressPattern;

    CryptoNetwork(String displayName, String assetName, BigDecimal estimatedFee, Pattern addressPattern) {
        this.displayName = displayName;
        this.assetName = assetName;
        this.estimatedFee = estimatedFee;
        this.addressPattern = addressPattern;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAssetName() {
        return assetName;
    }

    public BigDecimal getEstimatedFee() {
        return estimatedFee;
    }

    public boolean isValidAddress(String address) {
        return address != null && addressPattern.matcher(address.trim()).matches();
    }
}
