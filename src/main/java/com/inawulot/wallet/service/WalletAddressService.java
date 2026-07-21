package com.inawulot.wallet.service;

import com.inawulot.wallet.domain.CryptoNetwork;
import com.inawulot.wallet.domain.CryptoWalletAddress;
import com.inawulot.wallet.repository.CryptoWalletAddressRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class WalletAddressService {
    private final UserService userService;
    private final CryptoWalletAddressRepository walletAddressRepository;
    private final WalletAddressFactory walletAddressFactory;

    public WalletAddressService(
            UserService userService,
            CryptoWalletAddressRepository walletAddressRepository,
            WalletAddressFactory walletAddressFactory
    ) {
        this.userService = userService;
        this.walletAddressRepository = walletAddressRepository;
        this.walletAddressFactory = walletAddressFactory;
    }

    @Transactional
    public List<CryptoWalletAddress> ensureUserAddresses(UUID userId) {
        userService.getUser(userId);
        Arrays.stream(CryptoNetwork.values()).forEach(network -> getOrCreateAddress(userId, network));
        return walletAddressRepository.findByUserIdOrderByNetworkAsc(userId);
    }

    @Transactional
    public CryptoWalletAddress getOrCreateAddress(UUID userId, CryptoNetwork network) {
        userService.getUser(userId);
        return walletAddressRepository.findByUserIdAndNetwork(userId, network)
                .orElseGet(() -> walletAddressRepository.save(new CryptoWalletAddress(
                        UUID.randomUUID(),
                        userId,
                        network,
                        walletAddressFactory.addressFor(userId, network),
                        walletAddressFactory.encryptedPrivateKeyReference(userId, network),
                        walletAddressFactory.seedDerivationPath(userId, network),
                        Instant.now()
                )));
    }
}
