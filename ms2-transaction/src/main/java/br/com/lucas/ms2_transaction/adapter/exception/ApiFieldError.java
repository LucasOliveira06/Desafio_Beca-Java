package br.com.lucas.ms2_transaction.adapter.exception;

public record ApiFieldError(
        String field,
        String message
) {
}