package com.arul.finance_backend.ledger.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arul.finance_backend.ledger.model.LedgerEntry;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {

    List<LedgerEntry> findByTransferId(UUID transferId);

    List<LedgerEntry> findByAccountIdOrderByCreatedAtDesc(Long accountId);
}
