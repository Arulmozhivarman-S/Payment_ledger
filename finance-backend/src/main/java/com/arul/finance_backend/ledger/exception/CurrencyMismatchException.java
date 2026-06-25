package com.arul.finance_backend.ledger.exception;

public class CurrencyMismatchException extends RuntimeException {
    public CurrencyMismatchException(String message) { super(message); }
}
