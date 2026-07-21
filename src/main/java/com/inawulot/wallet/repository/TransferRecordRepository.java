package com.inawulot.wallet.repository;

import com.inawulot.wallet.domain.TransferRecord;
import com.inawulot.wallet.domain.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransferRecordRepository extends JpaRepository<TransferRecord, UUID> {
    Page<TransferRecord> findBySourceUserIdOrderByCreatedAtDesc(UUID sourceUserId, Pageable pageable);

    Page<TransferRecord> findBySourceUserIdAndStatusOrderByCreatedAtDesc(UUID sourceUserId, TransferStatus status, Pageable pageable);

    List<TransferRecord> findTop25ByStatusOrderByCreatedAtAsc(TransferStatus status);
}
