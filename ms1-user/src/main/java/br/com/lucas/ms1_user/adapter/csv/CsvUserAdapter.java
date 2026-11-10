package br.com.lucas.ms1_user.adapter.csv;

import br.com.lucas.ms1_user.application.port.out.CsvUserReaderPort;
import br.com.lucas.ms1_user.domain.model.CsvUserRow;
import br.com.lucas.ms1_user.domain.model.User;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Component
public class CsvUserAdapter implements CsvUserReaderPort {
    private static final int MAX_CSV_ROWS = 1000;

    @Override
    public List<CsvUserRow> parse(String content) {

        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "O arquivo CSV está vazio"
            );
        }

        List<CsvUserRow> rows = new ArrayList<>();

        String[] lines = content.split("\\R", -1);


        int headerIndex = 0;

        while (headerIndex < lines.length
                && lines[headerIndex].isBlank()) {
            headerIndex++;
        }

        if (headerIndex >= lines.length) {
            throw new IllegalArgumentException(
                    "O arquivo CSV está vazio"
            );
        }

        String header = lines[headerIndex]
                .replace("\uFEFF", "")
                .trim();

        char separator = detectSeparator(header);
        String[] headerColumns = header.split(
                Pattern.quote(String.valueOf(separator)),
                -1
        );

        if (!hasExpectedHeader(headerColumns)) {
            throw new IllegalArgumentException(
                    "Cabeçalho inválido. Use: name,email,password"
            );
        }

        int rowCount = 0;
        for (int i = headerIndex + 1; i < lines.length; i++) {

            String line = lines[i].trim();

            if (line.isEmpty()) {
                continue;
            }

            rowCount++;

            if (rowCount > MAX_CSV_ROWS) {
                throw new IllegalArgumentException(
                        "O CSV deve conter no máximo 1000 linhas de dados"
                );
            }

            String[] columns = line.split(
                    Pattern.quote(String.valueOf(separator)),
                    -1
            );

            if (columns.length != 3) {

                rows.add(new CsvUserRow(
                        i + 1,
                        null,
                        "Formato inválido. Esperadas 3 colunas"
                ));

                continue;
            }

            User user = new User(
                    null,
                    columns[0].trim(),
                    columns[1].trim(),
                    columns[2].trim()
            );

            rows.add(new CsvUserRow(
                    i + 1,
                    user,
                    null
            ));
        }

        if (rows.isEmpty()) {
            throw new IllegalArgumentException(
                    "O CSV não contém usuários para importar"
            );
        }

        return rows;
    }

    private char detectSeparator(String header) {
        boolean hasComma = header.contains(",");
        boolean hasSemicolon = header.contains(";");

        if (hasComma == hasSemicolon) {
            throw new IllegalArgumentException(
                    "Cabeçalho inválido. Use: name,email,password"
            );
        }

        return hasSemicolon ? ';' : ',';
    }

    private boolean hasExpectedHeader(String[] columns) {
        if (columns.length != 3) {
            return false;
        }

        return normalizeHeader(columns[0]).equals("name")
                && normalizeHeader(columns[1]).equals("email")
                && normalizeHeader(columns[2]).equals("password");
    }

    private String normalizeHeader(String header) {
        return header.trim().toLowerCase(Locale.ROOT);
    }
}
