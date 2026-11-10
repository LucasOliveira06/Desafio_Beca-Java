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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.security.access.AccessDeniedException;

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

    @Test
    void deveRetornar409QuandoHouverViolacaoDeIntegridade() {

        DataIntegrityViolationException exception =
                new DataIntegrityViolationException(
                        "Violação da constraint de e-mail único"
                );

        ResponseEntity<Map<String, String>> response =
                handler.handleDataIntegrity(exception);

        assertEquals(
                HttpStatus.CONFLICT,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Conflito de integridade dos dados",
                response.getBody().get("message")
        );
    }

    @Test
    void deveRetornar413QuandoUploadExcederLimite() {

        MaxUploadSizeExceededException exception =
                new MaxUploadSizeExceededException(1024L * 1024);

        ResponseEntity<Map<String, String>> response =
                handler.handleMaxUploadSize(exception);

        assertEquals(
                HttpStatus.PAYLOAD_TOO_LARGE,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "O arquivo enviado excede o limite permitido de 1 MB",
                response.getBody().get("message")
        );
    }

    @Test
    void deveRetornar500QuandoOcorrerErroInesperado() {

        Exception exception =
                new RuntimeException("Erro interno de conexão com banco");

        ResponseEntity<Map<String, String>> response =
                handler.handleGenericException(exception);

        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Erro interno do servidor",
                response.getBody().get("message")
        );

        assertFalse(
                response.getBody().get("message").contains("banco")
        );
    }

    @Test
    void deveRetornar403QuandoUsuarioNaoTemPermissao() {

        AccessDeniedException exception =
                new AccessDeniedException("Acesso negado");

        ResponseEntity<Map<String, String>> response =
                handler.handleAccessDenied(exception);

        assertEquals(
                HttpStatus.FORBIDDEN,
                response.getStatusCode()
        );

        assertNotNull(response.getBody());

        assertEquals(
                "Você não tem permissão para alterar ou excluir este usuário",
                response.getBody().get("message")
        );
    }

}

