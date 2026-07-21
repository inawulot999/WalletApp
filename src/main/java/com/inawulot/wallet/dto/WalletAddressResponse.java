package com.inawulot.wallet.dto;

import com.inawulot.wallet.domain.CryptoNetwork;
import com.inawulot.wallet.domain.CryptoWalletAddress;

public record WalletAddressResponse(
        CryptoNetwork network,
        String networkName,
        String address
) {
    public static WalletAddressResponse from(CryptoWalletAddress address) {
        return new WalletAddressResponse(
                address.getNetwork(),
                address.getNetwork().getDisplayName(),
                address.getAddress()
        );
    }
}
