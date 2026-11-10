package br.com.lucas.ms1_user.application.validation;

import br.com.lucas.ms1_user.domain.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    public void validate(User user) {

        if (user.getName() == null || user.getName().isBlank()) {
            throw new UserFieldValidationException(
                    "name",
                    "O nome é obrigatório"
            );
        }

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new UserFieldValidationException(
                    "email",
                    "O email é obrigatório"
            );
        }

        if (!user.getEmail().trim().matches(
                "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new UserFieldValidationException(
                    "email",
                    "O email deve ser válido"
            );
        }

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new UserFieldValidationException(
                    "password",
                    "A senha é obrigatória"
            );
        }

        if (user.getPassword().length() < 8) {
            throw new UserFieldValidationException(
                    "password",
                    "A senha deve ter no mínimo 8 caracteres"
            );
        }
    }
}

