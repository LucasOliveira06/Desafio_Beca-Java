
package br.com.lucas.ms1_user.application.port.out;

import br.com.lucas.ms1_user.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepositoryPort {

    User save(User user);

    List<User> findAll();

    Optional<User> findById(Long id);

    void deleteById(Long id);
}

