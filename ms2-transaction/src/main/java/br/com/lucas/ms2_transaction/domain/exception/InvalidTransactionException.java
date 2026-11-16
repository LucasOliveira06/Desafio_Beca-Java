package br.com.lucas.ms2_transaction.domain.exception;

public class InvalidTransactionException extends IllegalArgumentException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}
