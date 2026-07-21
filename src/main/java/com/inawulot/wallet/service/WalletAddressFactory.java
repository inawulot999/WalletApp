package com.inawulot.wallet.service;

import com.inawulot.wallet.domain.CryptoNetwork;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class WalletAddressFactory {
    private static final char[] BASE58 = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz".toCharArray();

    private final HashingService hashingService;

    public WalletAddressFactory(HashingService hashingService) {
        this.hashingService = hashingService;
    }

    public String addressFor(UUID userId, CryptoNetwork network) {
        String hash = hashingService.sha256("wallet-address:" + userId + ":" + network);
        if (network == CryptoNetwork.TRC20) {
            StringBuilder address = new StringBuilder("T");
            for (int i = 0; address.length() < 34; i += 2) {
                int value = Integer.parseInt(hash.substring(i % (hash.length() - 1), i % (hash.length() - 1) + 2), 16);
                address.append(BASE58[value % BASE58.length]);
            }
            return address.toString();
        }
        return "0x" + hash.substring(0, 40);
    }

    public String encryptedPrivateKeyReference(UUID userId, CryptoNetwork network) {
        return "vault:v1:" + hashingService.sha256("private-key:" + userId + ":" + network).substring(0, 32);
    }

    public String seedDerivationPath(UUID userId, CryptoNetwork network) {
        int accountIndex = Math.abs(hashingService.sha256(userId + ":" + network).hashCode() % 10000);
        return "m/44'/" + slip44(network) + "'/0'/0/" + accountIndex;
    }

    private int slip44(CryptoNetwork network) {
        return switch (network) {
            case TRC20 -> 195;
            case ERC20, BEP20, POLYGON -> 60;
        };
    }
}
