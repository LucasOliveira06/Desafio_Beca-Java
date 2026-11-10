package br.com.lucas.ms1_user.adapter.csv;

import br.com.lucas.ms1_user.domain.model.CsvUserRow;
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

        List<CsvUserRow> rows = csvUserAdapter.parse(content);

        assertEquals(2, rows.size());

        assertEquals(2, rows.get(0).lineNumber());
        assertNull(rows.get(0).error());
        assertEquals("Ana Silva", rows.get(0).user().getName());
        assertEquals("ana@exemplo.com", rows.get(0).user().getEmail());
        assertEquals("SenhaAna123", rows.get(0).user().getPassword());

        assertEquals(3, rows.get(1).lineNumber());
        assertNull(rows.get(1).error());
        assertEquals("Pedro Souza", rows.get(1).user().getName());
        assertEquals("pedro@exemplo.com", rows.get(1).user().getEmail());
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
    void deveAceitarCabecalhoComMaiusculas() {

        String content = """
                Name,Email,Password
                Ana Silva,ana@exemplo.com,SenhaAna123
                """;

        List<CsvUserRow> rows = csvUserAdapter.parse(content);

        assertEquals(1, rows.size());
        assertEquals("Ana Silva", rows.get(0).user().getName());
    }

    @Test
    void deveAceitarCsvDoExcelComPontoEVirgula() {

        String content = """
                Name;Email;Password
                Ana Silva;ana@exemplo.com;SenhaAna123
                """;

        List<CsvUserRow> rows = csvUserAdapter.parse(content);

        assertEquals(1, rows.size());
        assertEquals("ana@exemplo.com", rows.get(0).user().getEmail());
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
                "O CSV não contém usuários para importar",
                exception.getMessage()
        );
    }

    @Test
    void deveRegistrarLinhaInvalidaEContinuarLeitura() {

        String content = """
                name,email,password
                Ana Silva,ana@exemplo.com,SenhaAna123

                Pedro Souza,pedro@exemplo.com
                Maria Oliveira,maria@exemplo.com,SenhaMaria123
                """;

        List<CsvUserRow> rows = csvUserAdapter.parse(content);

        assertEquals(3, rows.size());

        // Primeira linha válida
        assertEquals(2, rows.get(0).lineNumber());
        assertNotNull(rows.get(0).user());
        assertNull(rows.get(0).error());

        // Linha inválida: faltou a coluna password
        assertEquals(4, rows.get(1).lineNumber());
        assertNull(rows.get(1).user());
        assertEquals(
                "Formato inválido. Esperadas 3 colunas",
                rows.get(1).error()
        );

        // A leitura continua depois do erro
        assertEquals(5, rows.get(2).lineNumber());
        assertEquals(
                "Maria Oliveira",
                rows.get(2).user().getName()
        );
        assertNull(rows.get(2).error());
    }

    @Test
    void deveAceitarCsvComMilLinhas() {

        String content = "name,email,password\n"
                + "Ana,ana@exemplo.com,Senha123\n".repeat(1000);

        List<CsvUserRow> rows = csvUserAdapter.parse(content);

        assertEquals(1000, rows.size());
    }

    @Test
    void deveRejeitarCsvComMaisDeMilLinhas() {

        String content = "name,email,password\n"
                + "Ana,ana@exemplo.com,Senha123\n".repeat(1001);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> csvUserAdapter.parse(content)
        );

        assertEquals(
                "O CSV deve conter no máximo 1000 linhas de dados",
                exception.getMessage()
        );
    }
}
