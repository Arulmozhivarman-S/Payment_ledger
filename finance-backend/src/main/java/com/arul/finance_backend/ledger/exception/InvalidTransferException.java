package com.arul.finance_backend.ledger.exception;

public class InvalidTransferException extends RuntimeException {
    public InvalidTransferException(String message) { super(message); }
}
