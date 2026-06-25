package com.arul.finance_backend.ledger.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.arul.finance_backend.ledger.model.Transfer;

@Repository
public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    /** Fast path for idempotent retries. */
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);
}
