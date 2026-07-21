package com.inawulot.wallet.repository;

import com.inawulot.wallet.domain.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {
    List<LedgerEntry> findByAccountReferenceInOrderByCreatedAtDesc(Collection<String> accountReferences);
}
