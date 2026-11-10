package br.com.lucas.ms1_user.application.port.in;

import br.com.lucas.ms1_user.domain.model.User;
import br.com.lucas.ms1_user.application.dto.CsvImportResult;
import java.util.List;

public interface UserUseCase {

    User create(User user);

    List<User> findAll();

    User findById(Long id);

    User update(Long id, User user);

    void delete(Long id);

    CsvImportResult importCsv(String content);
}

