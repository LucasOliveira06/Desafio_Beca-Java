package br.com.lucas.ms2_transaction.adapter.exception;

import br.com.lucas.ms2_transaction.application.exception.TransactionNotFoundException;
import br.com.lucas.ms2_transaction.domain.exception.InvalidTransactionException;
import br.com.lucas.ms2_transaction.application.exception.InvalidDateRangeException;
import br.com.lucas.ms2_transaction.application.exception.AccountBalanceUnavailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler
        extends ResponseEntityExceptionHandler {

    @ExceptionHandler(TransactionNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleTransactionNotFound(
            TransactionNotFoundException exception
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(InvalidTransactionException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidTransaction(
            InvalidTransactionException exception
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidDateRange(
            InvalidDateRangeException exception
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(AccountBalanceUnavailableException.class)
    public ResponseEntity<ApiErrorResponse> handleAccountBalanceUnavailable(
            AccountBalanceUnavailableException exception
    ) {
        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                exception.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(response);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request
    ) {
        List<ApiFieldError> errors = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> new ApiFieldError(
                        fieldError.getField(),
                        fieldError.getDefaultMessage()
                ))
                .toList();

        ApiErrorResponse response = new ApiErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Dados inválidos",
                errors
        );

        return handleExceptionInternal(
                exception,
                response,
                headers,
                HttpStatus.BAD_REQUEST,
                request
        );
    }
}
