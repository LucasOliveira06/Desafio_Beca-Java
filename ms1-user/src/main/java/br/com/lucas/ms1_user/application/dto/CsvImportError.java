package br.com.lucas.ms1_user.application.dto;

public record CsvImportError(
        int line,
        String field,
        String reason
) {
}
