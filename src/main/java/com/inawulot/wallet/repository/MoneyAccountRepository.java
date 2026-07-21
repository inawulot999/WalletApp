package com.inawulot.wallet.repository;

import com.inawulot.wallet.domain.MoneyAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface MoneyAccountRepository extends JpaRepository<MoneyAccount, String> {
    List<MoneyAccount> findByOwnerReferenceOrderByCurrencyAsc(String ownerReference);

    List<MoneyAccount> findByReferenceIn(Collection<String> references);
}
