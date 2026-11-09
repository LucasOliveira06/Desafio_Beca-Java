package br.com.lucas.ms1_user.adapter.csv;

import br.com.lucas.ms1_user.domain.model.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvUserAdapterTest {

    private final CsvUserAdapter csvUserAdapter =
            new CsvUserAdapter();

    @Test
    void deveLerUsuariosDoCsv() {

        String content = """
                name,email,password
                Ana Silva,ana@exemplo.com,SenhaAna123
                Pedro Souza,pedro@exemplo.com,SenhaPedro123
                """;

        List<User> users = csvUserAdapter.parse(content);

        assertEquals(2, users.size());

        assertEquals("Ana Silva", users.get(0).getName());
        assertEquals("ana@exemplo.com", users.get(0).getEmail());
        assertEquals("SenhaAna123", users.get(0).getPassword());

        assertEquals("Pedro Souza", users.get(1).getName());
        assertEquals("pedro@exemplo.com", users.get(1).getEmail());
    }

    @Test
    void deveRejeitarCabecalhoInvalido() {

        String content = """
            nome,email,senha
            Ana Silva,ana@exemplo.com,SenhaAna123
            """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> csvUserAdapter.parse(content)
        );

        assertEquals(
                "Cabeçalho inválido. Use: name,email,password",
                exception.getMessage()
        );
    }

    @Test
    void deveRejeitarCsvSemUsuarios() {

        String content = """
            name,email,password

            """;

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> csvUserAdapter.parse(content)
        );

        assertEquals(
                "O CSV deve conter cabeçalho e pelo menos um usuário",
                exception.getMessage()
        );
    }

}

