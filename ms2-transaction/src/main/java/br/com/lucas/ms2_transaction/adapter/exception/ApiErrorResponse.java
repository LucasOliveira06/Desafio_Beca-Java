package br.com.lucas.ms2_transaction.adapter.exception;

public record ApiErrorResponse(
        int status,
        String message
) {
}