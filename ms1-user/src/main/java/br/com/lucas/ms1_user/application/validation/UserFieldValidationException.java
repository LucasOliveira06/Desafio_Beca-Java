package br.com.lucas.ms1_user.application.validation;

public class UserFieldValidationException extends IllegalArgumentException {

    private final String field;

    public UserFieldValidationException(String field, String message) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
