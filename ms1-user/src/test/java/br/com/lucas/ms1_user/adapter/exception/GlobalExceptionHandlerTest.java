package br.com.lucas.ms1_user.adapter.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import br.com.lucas.ms1_user.domain.exception.UserNotFoundException;
import br.com.lucas.ms1_user.domain.exception.EmailAlreadyExistsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler();

    @Test
    void deveRetornar400QuandoCsvForInvalido() {

        IllegalArgumentException exception =
                new IllegalArgumentException("Cabeçalho CSV inválido");

        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidCsv(exception);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Cabeçalho CSV inválido",
                response.getBody().get("message")
        );
    }

    @Test
    void deveRetornar404QuandoUsuarioNaoForEncontrado() {

        UserNotFoundException exception =
                new UserNotFoundException();

        ResponseEntity<Map<String, String>> response =
                handler.handleUserNotFound(exception);

        assertEquals(
                HttpStatus.NOT_FOUND,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Usuário não encontrado",
                response.getBody().get("message")
        );
    }

    @Test
    void deveRetornar409QuandoEmailJaExiste() {

        EmailAlreadyExistsException exception =
                mock(EmailAlreadyExistsException.class);

        when(exception.getMessage())
                .thenReturn("E-mail já cadastrado");

        ResponseEntity<Map<String, String>> response =
                handler.handleEmailAlreadyExists(exception);

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "E-mail já cadastrado",
                response.getBody().get("message")
        );
    }

    @Test
    void deveRetornar400QuandoValidacaoFalhar() {

        BindingResult bindingResult =
                new BeanPropertyBindingResult(
                        new Object(),
                        "user"
                );

        bindingResult.addError(
                new FieldError(
                        "user",
                        "email",
                        "E-mail inválido"
                )
        );

        MethodArgumentNotValidException exception =
                mock(MethodArgumentNotValidException.class);

        when(exception.getBindingResult())
                .thenReturn(bindingResult);

        ResponseEntity<Map<String, String>> response =
                handler.handleValidation(exception);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "E-mail inválido",
                response.getBody().get("email")
        );
    }

}

