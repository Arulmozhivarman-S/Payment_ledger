package com.arul.finance_backend.ledger.exception;

import java.time.Instant;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.arul.finance_backend.ledger")
public class LedgerExceptionHandler {

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, Object>> insufficientFunds(InsufficientFundsException e) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, "INSUFFICIENT_FUNDS", e.getMessage());
    }

    @ExceptionHandler({CurrencyMismatchException.class, InvalidTransferException.class})
    public ResponseEntity<Map<String, Object>> badTransfer(RuntimeException e) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_TRANSFER", e.getMessage());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, Object>> notFound(AccountNotFoundException e) {
        return build(HttpStatus.NOT_FOUND, "ACCOUNT_NOT_FOUND", e.getMessage());
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String code, String msg) {
        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "status", status.value(),
                "error", code,
                "message", msg));
    }
}
