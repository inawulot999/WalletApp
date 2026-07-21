package com.inawulot.wallet.repository;

import com.inawulot.wallet.domain.WalletUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WalletUserRepository extends JpaRepository<WalletUser, UUID> {
    Optional<WalletUser> findByEmail(String email);
}
