package com.inawulot.wallet.repository;

import com.inawulot.wallet.domain.CryptoNetwork;
import com.inawulot.wallet.domain.CryptoWalletAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CryptoWalletAddressRepository extends JpaRepository<CryptoWalletAddress, UUID> {
    Optional<CryptoWalletAddress> findByUserIdAndNetwork(UUID userId, CryptoNetwork network);

    List<CryptoWalletAddress> findByUserIdOrderByNetworkAsc(UUID userId);
}
