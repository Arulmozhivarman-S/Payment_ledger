package com.arul.finance_backend;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.arul.finance_backend.ledger.dto.TransferRequest;
import com.arul.finance_backend.ledger.model.Account;
import com.arul.finance_backend.ledger.repository.AccountRepository;
import com.arul.finance_backend.ledger.service.AccountService;
import com.arul.finance_backend.ledger.service.TransferService;

/**
 * Proof tests for the two headline guarantees.
 *
 * Requires a running database (MySQL via docker compose, or add Testcontainers).
 * These tests fail if you ever remove the row lock or the idempotency constraint.
 */
@SpringBootTest
class TransferServiceConcurrencyTest {

    @Autowired TransferService transferService;
    @Autowired AccountService accountService;
    @Autowired AccountRepository accountRepository;

    /** 20 threads try to spend a balance that only covers ONE transfer. Exactly one wins. */
    @Test
    void concurrentWithdrawals_neverOverdraw() throws InterruptedException {
        Account from = accountService.open(1L, "USD", 100_00); // $100.00
        Account to   = accountService.open(2L, "USD", 0);

        int threads = 20;
        long amount = 100_00; // each transfer drains the whole balance
        var pool = Executors.newFixedThreadPool(threads);
        var ready = new CountDownLatch(threads);
        var go = new CountDownLatch(1);
        var success = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    go.await();
                    // Distinct keys => these are genuinely different attempts,
                    // so only the lock + funds check can save us.
                    transferService.transfer(UUID.randomUUID().toString(),
                            new TransferRequest(from.getId(), to.getId(), amount, "USD"));
                    success.incrementAndGet();
                } catch (Exception ignored) {
                    // insufficient funds for the losers
                }
            });
        }

        ready.await();
        go.countDown();
        pool.shutdown();
        pool.awaitTermination(30, java.util.concurrent.TimeUnit.SECONDS);

        Account src = accountRepository.findById(from.getId()).orElseThrow();
        assertThat(success.get()).isEqualTo(1);          // only one debit happened
        assertThat(src.getBalanceMinor()).isZero();      // balance never went negative
    }

    /** Same idempotency key submitted twice moves money once. */
    @Test
    void sameIdempotencyKey_movesMoneyOnce() {
        Account from = accountService.open(3L, "USD", 50_00);
        Account to   = accountService.open(4L, "USD", 0);
        String key = "order-12345";
        var req = new TransferRequest(from.getId(), to.getId(), 20_00, "USD");

        var first  = transferService.transfer(key, req);
        var second = transferService.transfer(key, req);

        assertThat(second.getId()).isEqualTo(first.getId());       // same transfer returned
        Account dest = accountRepository.findById(to.getId()).orElseThrow();
        assertThat(dest.getBalanceMinor()).isEqualTo(20_00);       // credited once, not twice
    }
}
