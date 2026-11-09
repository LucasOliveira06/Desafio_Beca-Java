package br.com.lucas.ms1_user.adapter.csv;

import br.com.lucas.ms1_user.domain.model.User;
import org.springframework.stereotype.Component;
import br.com.lucas.ms1_user.application.port.out.CsvUserReaderPort;

import java.util.ArrayList;
import java.util.List;

@Component
public class CsvUserAdapter implements CsvUserReaderPort {

    @Override
    public List<User> parse(String content) {

        List<User> users = new ArrayList<>();

        String[] lines = content.stripLeading().split("\\R");

        if (lines.length < 2) {
            throw new IllegalArgumentException(
                    "O CSV deve conter cabeçalho e pelo menos um usuário"
            );
        }

        String header = lines[0].replace("\uFEFF", "").trim();

        
        if (!header.equals("name,email,password")) {
            throw new IllegalArgumentException(
                    "Cabeçalho inválido. Use: name,email,password"
            );
        }

        for (int i = 1; i < lines.length; i++) {

            String line = lines[i].trim();

            if (line.isEmpty()) {
                continue;
            }

            String[] columns = line.split(",", -1);

            if (columns.length != 3) {
                throw new IllegalArgumentException(
                        "Linha " + (i + 1) + ": formato inválido"
                );
            }

            users.add(new User(
                    null,
                    columns[0].trim(),
                    columns[1].trim(),
                    columns[2].trim()
            ));
        }

           if (users.isEmpty()) {
              throw new IllegalArgumentException(
                    "O CSV não contém usuários para importar"
            );
        }

        return users;
    }
}

