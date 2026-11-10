package br.com.lucas.ms1_user.domain.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException() {
        super("Email já cadastrado");
    }
}

