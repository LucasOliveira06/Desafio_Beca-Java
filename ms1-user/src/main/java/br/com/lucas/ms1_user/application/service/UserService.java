package br.com.lucas.ms1_user.application.service;

import br.com.lucas.ms1_user.application.port.in.UserUseCase;
import br.com.lucas.ms1_user.application.port.out.UserRepositoryPort;
import br.com.lucas.ms1_user.application.port.out.PasswordEncoderPort;
import br.com.lucas.ms1_user.domain.model.User;
import br.com.lucas.ms1_user.domain.exception.UserNotFoundException;
import br.com.lucas.ms1_user.domain.exception.EmailAlreadyExistsException;
import br.com.lucas.ms1_user.application.port.out.CsvUserReaderPort;
import org.springframework.transaction.annotation.Transactional;
import br.com.lucas.ms1_user.application.validation.UserValidator;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;

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
    @Transactional
    public List<User> importCsv(String content) {

        List<User> users = csvUserReader.parse(content);

        // Valida todos os usuários antes de salvar
        for (User user : users) {
            userValidator.validate(user);
        }

        List<User> importedUsers = new java.util.ArrayList<>();

        for (User user : users) {
            importedUsers.add(create(user));
        }

        return importedUsers;
    }

}

