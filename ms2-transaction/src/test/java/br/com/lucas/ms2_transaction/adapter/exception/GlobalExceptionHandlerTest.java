package br.com.lucas.ms2_transaction.adapter.exception;

import br.com.lucas.ms2_transaction.application.exception.TransactionNotFoundException;
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
}