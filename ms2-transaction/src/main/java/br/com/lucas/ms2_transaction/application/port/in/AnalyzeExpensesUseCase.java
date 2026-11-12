package br.com.lucas.ms2_transaction.application.port.in;

import br.com.lucas.ms2_transaction.application.dto.ExpenseAnalysisData;

import java.time.LocalDate;

public interface AnalyzeExpensesUseCase {

    ExpenseAnalysisData analyze(
            Long ownerUserId,
            LocalDate startDate,
            LocalDate endDate
    );
}