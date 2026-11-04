package br.com.lucas.ms1_user.adapter.controller;

import br.com.lucas.ms1_user.adapter.controller.dto.UserRequestDTO;
import br.com.lucas.ms1_user.application.port.in.UserUseCase;
import br.com.lucas.ms1_user.domain.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserUseCase userUseCase;

    public UserController(UserUseCase userUseCase) {
        this.userUseCase = userUseCase;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User create(@Valid @RequestBody UserRequestDTO request) {
        User user = new User(null, request.getName(), request.getEmail());
        return userUseCase.create(user);
    }

    @GetMapping
    public List<User> findAll() {
        return userUseCase.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        return userUseCase.findById(id);
    }

    @PutMapping("/{id}")
    public User update(@PathVariable Long id,
                       @Valid @RequestBody UserRequestDTO request) {

        User user = new User(id, request.getName(), request.getEmail());
        return userUseCase.update(id, user);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userUseCase.delete(id);
    }
}
