package br.com.lucas.ms2_transaction.application.exception;

public class InvalidDateRangeException extends RuntimeException {

    public InvalidDateRangeException() {
        super("A data inicial não pode ser posterior à data final");
    }
}