package br.com.lucas.ms1_user.domain.model;

public record CsvUserRow(
        int lineNumber,
        User user,
        String error
) {
}
