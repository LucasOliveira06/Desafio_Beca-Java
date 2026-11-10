package br.com.lucas.ms1_user.application.service;

import br.com.lucas.ms1_user.application.port.in.UserUseCase;
import br.com.lucas.ms1_user.application.port.out.UserRepositoryPort;
import br.com.lucas.ms1_user.application.port.out.PasswordEncoderPort;
import br.com.lucas.ms1_user.domain.model.User;
import br.com.lucas.ms1_user.domain.exception.UserNotFoundException;
import br.com.lucas.ms1_user.domain.exception.EmailAlreadyExistsException;
import br.com.lucas.ms1_user.application.port.out.CsvUserReaderPort;
import br.com.lucas.ms1_user.application.validation.UserValidator;
import br.com.lucas.ms1_user.application.validation.UserFieldValidationException;
import br.com.lucas.ms1_user.application.dto.CsvImportError;
import br.com.lucas.ms1_user.application.dto.CsvImportResult;
import br.com.lucas.ms1_user.domain.model.CsvUserRow;

import org.springframework.stereotype.Service;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

@Service
public class UserService implements UserUseCase {

    private final UserRepositoryPort repository;
    private final PasswordEncoderPort passwordEncoder;
    private final CsvUserReaderPort csvUserReader;
    private final UserValidator userValidator;

    public UserService(UserRepositoryPort repository,
                       PasswordEncoderPort passwordEncoder,
                       CsvUserReaderPort csvUserReader,
                       UserValidator userValidator) {

        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.csvUserReader = csvUserReader;
        this.userValidator = userValidator;
    }

    @Override
    public User create(User user) {

        String email = user.getEmail().trim().toLowerCase(Locale.ROOT);

        if (repository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }

        user.setEmail(email);

        String passwordHash = passwordEncoder.encode(user.getPassword());
        user.setPassword(passwordHash);

        return repository.save(user);
    }

    @Override
    public List<User> findAll() {
        return repository.findAll();
    }

    @Override
    public User findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException());
    }

    @Override
    public User findByEmail(String email) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        return repository.findByEmail(normalizedEmail)
                .orElseThrow(UserNotFoundException::new);
    }

    @Override
    public User update(Long id, User user) {

        User existingUser = findById(id);

        String email = user.getEmail().trim().toLowerCase(Locale.ROOT);

        if (!existingUser.getEmail().equals(email)
                && repository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException();
        }

        existingUser.setName(user.getName());
        existingUser.setEmail(email);

        String passwordHash = passwordEncoder.encode(user.getPassword());
        existingUser.setPassword(passwordHash);

        return repository.save(existingUser);
    }

    @Override
    public void delete(Long id) {
        findById(id);
        repository.deleteById(id);
    }

    @Override
    public CsvImportResult importCsv(String content) {

        List<CsvUserRow> rows = csvUserReader.parse(content);

        List<CsvImportError> errors = new ArrayList<>();

        int totalImported = 0;

        for (CsvUserRow row : rows) {

            // Linha com formato inválido
            if (row.error() != null) {
                errors.add(new CsvImportError(
                        row.lineNumber(),
                        "row",
                        row.error()
                ));
                continue;
            }

            try {
                // Valida apenas o usuário da linha atual
                userValidator.validate(row.user());

                // Salva o usuário válido
                create(row.user());

                totalImported++;

            } catch (EmailAlreadyExistsException exception) {

                errors.add(new CsvImportError(
                        row.lineNumber(),
                        "email",
                        "E-mail já cadastrado"
                ));

            } catch (UserFieldValidationException exception) {

                errors.add(new CsvImportError(
                        row.lineNumber(),
                        exception.getField(),
                        exception.getMessage()
                ));

            } catch (IllegalArgumentException exception) {

                errors.add(new CsvImportError(
                        row.lineNumber(),
                        "row",
                        exception.getMessage()
                ));

            } catch (DataIntegrityViolationException exception) {

                errors.add(new CsvImportError(
                        row.lineNumber(),
                        "row",
                        "Conflito de dados ao salvar usuário"
                ));
            }
        }

        return new CsvImportResult(
                totalImported,
                errors
        );
    }

}

