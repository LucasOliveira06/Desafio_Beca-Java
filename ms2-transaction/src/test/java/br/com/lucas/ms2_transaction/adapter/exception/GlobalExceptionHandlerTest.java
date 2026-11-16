package br.com.lucas.ms2_transaction.adapter.exception;

import br.com.lucas.ms2_transaction.application.exception.TransactionNotFoundException;
import br.com.lucas.ms2_transaction.domain.exception.InvalidTransactionException;
import br.com.lucas.ms2_transaction.application.exception.InvalidDateRangeException;
import br.com.lucas.ms2_transaction.application.exception.AccountBalanceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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

    @Test
    void deveRetornarServiceUnavailableQuandoSaldoNaoEstiverDisponivel() {
        AccountBalanceUnavailableException exception =
                new AccountBalanceUnavailableException(
                        "Não foi possível consultar o saldo na MockAPI."
                );

        ResponseEntity<ApiErrorResponse> response =
                globalExceptionHandler.handleAccountBalanceUnavailable(exception);

        assertEquals(
                HttpStatus.SERVICE_UNAVAILABLE,
                response.getStatusCode()
        );
        assertNotNull(response.getBody());
        assertEquals(503, response.getBody().status());
        assertEquals(
                "Não foi possível consultar o saldo na MockAPI.",
                response.getBody().message()
        );
    }

    @Test
    void deveRetornarCampoEMensagemQuandoBeanValidationFalhar() {
        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError = new FieldError(
                "createTransactionRequest",
                "amount",
                "O valor da transação deve ser maior que zero"
        );

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors())
                .thenReturn(List.of(fieldError));

        ResponseEntity<Object> response =
                globalExceptionHandler.handleMethodArgumentNotValid(
                        exception,
                        new HttpHeaders(),
                        HttpStatus.BAD_REQUEST,
                        mock(WebRequest.class)
                );

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());

        ApiErrorResponse body = (ApiErrorResponse) response.getBody();

        assertEquals(400, body.status());
        assertEquals("Dados inválidos", body.message());
        assertEquals(1, body.errors().size());
        assertEquals("amount", body.errors().getFirst().field());
        assertEquals(
                "O valor da transação deve ser maior que zero",
                body.errors().getFirst().message()
        );
    }
}
