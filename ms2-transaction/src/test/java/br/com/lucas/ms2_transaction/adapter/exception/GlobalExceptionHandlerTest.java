package br.com.lucas.ms2_transaction.adapter.exception;

import br.com.lucas.ms2_transaction.application.exception.TransactionNotFoundException;
import br.com.lucas.ms2_transaction.domain.exception.InvalidTransactionException;
import br.com.lucas.ms2_transaction.application.exception.InvalidDateRangeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        globalExceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void deveRetornarNotFoundQuandoTransacaoNaoForEncontrada() {
        TransactionNotFoundException exception =
                new TransactionNotFoundException();

        ResponseEntity<ApiErrorResponse> response =
                globalExceptionHandler.handleTransactionNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().status());
        assertEquals(
                "Transação não encontrada",
                response.getBody().message()
        );
    }

    @Test
    void deveRetornarBadRequestQuandoTransacaoForInvalida() {
        InvalidTransactionException exception =
                new InvalidTransactionException("Transação inválida");

        ResponseEntity<ApiErrorResponse> response =
                globalExceptionHandler.handleInvalidTransaction(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals("Transação inválida", response.getBody().message());
    }

    @Test
    void deveRetornarBadRequestQuandoPeriodoForInvalido() {
        InvalidDateRangeException exception =
                new InvalidDateRangeException();

        ResponseEntity<ApiErrorResponse> response =
                globalExceptionHandler.handleInvalidDateRange(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(400, response.getBody().status());
        assertEquals(
                "A data inicial não pode ser posterior à data final",
                response.getBody().message()
        );
    }
}
