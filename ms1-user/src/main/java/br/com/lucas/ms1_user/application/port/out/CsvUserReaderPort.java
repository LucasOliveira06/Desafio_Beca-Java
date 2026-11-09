package br.com.lucas.ms1_user.application.port.out;

import br.com.lucas.ms1_user.domain.model.User;
import java.util.List;

public interface CsvUserReaderPort {

    List<User> parse(String content);

}

