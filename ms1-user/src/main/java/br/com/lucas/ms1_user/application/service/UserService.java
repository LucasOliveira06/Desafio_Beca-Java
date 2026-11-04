package br.com.lucas.ms1_user.application.service;

import br.com.lucas.ms1_user.application.port.in.UserUseCase;
import br.com.lucas.ms1_user.application.port.out.UserRepositoryPort;
import br.com.lucas.ms1_user.domain.model.User;
import org.springframework.stereotype.Service;
import br.com.lucas.ms1_user.domain.exception.UserNotFoundException;

import java.util.List;

@Service
public class UserService implements UserUseCase {

    private final UserRepositoryPort repository;

    public UserService(UserRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public User create(User user) {
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

        existingUser.setName(user.getName());
        existingUser.setEmail(user.getEmail());

        return repository.save(existingUser);
    }

    @Override
    public void delete(Long id) {
        findById(id);
        repository.deleteById(id);
    }
}
