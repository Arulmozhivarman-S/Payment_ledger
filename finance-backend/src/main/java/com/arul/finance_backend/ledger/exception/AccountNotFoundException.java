package com.arul.finance_backend.ledger.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) { super(message); }
}
