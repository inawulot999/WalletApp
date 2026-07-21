package com.inawulot.wallet.dto;

import com.inawulot.wallet.domain.MoneyAccount;

import java.math.BigDecimal;

public record AssetBalanceResponse(
        String accountReference,
        String asset,
        BigDecimal balance
) {
    public static AssetBalanceResponse from(MoneyAccount account) {
        return new AssetBalanceResponse(account.getReference(), account.getCurrency(), account.getBalance());
    }
}
