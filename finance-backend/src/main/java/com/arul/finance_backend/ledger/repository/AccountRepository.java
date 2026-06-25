package com.arul.finance_backend.ledger.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.arul.finance_backend.ledger.model.Account;

import jakarta.persistence.LockModeType;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Loads an account and takes a PESSIMISTIC_WRITE lock (SELECT ... FOR UPDATE).
     * Any other transaction trying to lock the same row blocks until we commit.
     * This serialises concurrent transfers touching the same account, which is
     * what prevents two simultaneous withdrawals from both "seeing" the old
     * balance and overdrawing (the classic double-spend race).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);
}
