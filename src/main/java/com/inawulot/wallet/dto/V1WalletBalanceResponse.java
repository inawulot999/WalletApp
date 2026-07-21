package com.inawulot.wallet.dto;

import java.util.List;
import java.util.UUID;

public record V1WalletBalanceResponse(
        UUID userId,
        List<AssetBalanceResponse> balances,
        List<WalletAddressResponse> addresses
) {
}
