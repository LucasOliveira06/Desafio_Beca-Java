package br.com.lucas.ms2_transaction.adapter.exception;

import java.util.List;

public record ApiErrorResponse(
        int status,
        String message,
        List<ApiFieldError> errors
) {

    public ApiErrorResponse(int status, String message) {
        this(status, message, List.of());
    }
}