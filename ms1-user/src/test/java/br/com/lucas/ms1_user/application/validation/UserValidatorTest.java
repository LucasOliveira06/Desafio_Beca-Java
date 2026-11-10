package br.com.lucas.ms1_user.application.validation;

import br.com.lucas.ms1_user.domain.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserValidatorTest {

    private final UserValidator validator = new UserValidator();

    @Test
    void deveAceitarUsuarioValido() {

        User user = new User(
                null,
                "Ana Silva",
                "ana@exemplo.com",
                "SenhaAna123"
        );

        assertDoesNotThrow(() -> validator.validate(user));
    }

    @Test
    void deveRejeitarNomeVazio() {

        User user = new User(
                null,
                "",
                "ana@exemplo.com",
                "SenhaAna123"
        );

        UserFieldValidationException exception = assertThrows(
                UserFieldValidationException.class,
                () -> validator.validate(user)
        );

        assertEquals(
                "O nome é obrigatório",
                exception.getMessage()
        );
        assertEquals("name", exception.getField());
    }

    @Test
    void deveRejeitarEmailInvalido() {

        User user = new User(
                null,
                "Ana Silva",
                "email-invalido",
                "SenhaAna123"
        );

        UserFieldValidationException exception = assertThrows(
                UserFieldValidationException.class,
                () -> validator.validate(user)
        );

        assertEquals(
                "O email deve ser válido",
                exception.getMessage()
        );
        assertEquals("email", exception.getField());
    }

    @Test
    void deveRejeitarSenhaCurta() {

        User user = new User(
                null,
                "Ana Silva",
                "ana@exemplo.com",
                "123"
        );

        UserFieldValidationException exception = assertThrows(
                UserFieldValidationException.class,
                () -> validator.validate(user)
        );

        assertEquals(
                "A senha deve ter no mínimo 8 caracteres",
                exception.getMessage()
        );
        assertEquals("password", exception.getField());
    }
}

