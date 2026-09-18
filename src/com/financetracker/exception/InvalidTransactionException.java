package com.financetracker.exception;

/**
 * Thrown when a transaction fails basic validation rules
 * (e.g. non-positive amount, null category, future-dated recurring setup, etc).
 */
public class InvalidTransactionException extends Exception {

    public InvalidTransactionException(String message) {
        super(message);
    }

    public InvalidTransactionException(String message, Throwable cause) {
        super(message, cause);
    }
}