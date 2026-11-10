package br.com.lucas.ms1_user.application.dto;

import java.util.List;

public record CsvImportResult(
        int totalImported,
        List<CsvImportError> errors
) {
}

