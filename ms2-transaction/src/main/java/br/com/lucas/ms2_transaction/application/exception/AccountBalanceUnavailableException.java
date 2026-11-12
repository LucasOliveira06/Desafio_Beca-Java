package br.com.lucas.ms2_transaction.application.exception;

public class AccountBalanceUnavailableException extends RuntimeException {

    public AccountBalanceUnavailableException(String message) {
        super(message);
    }

    public AccountBalanceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}