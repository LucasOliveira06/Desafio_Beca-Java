package br.com.lucas.ms2_transaction.application.port.in;

import java.time.LocalDate;

public interface GenerateTransactionReportUseCase {

    byte[] generate(
            Long ownerUserId,
            LocalDate startDate,
            LocalDate endDate
    );
}