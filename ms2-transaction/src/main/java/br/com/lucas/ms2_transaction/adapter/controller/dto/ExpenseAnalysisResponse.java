package br.com.lucas.ms2_transaction.adapter.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseAnalysisResponse(
        LocalDate startDate,
        LocalDate endDate,
        List<ExpenseCategoryTotalResponse> totalsByCategory,
        List<ExpenseDailyTotalResponse> totalsByDay,
        List<ExpenseMonthlyTotalResponse> totalsByMonth,
        BigDecimal grandTotal
) {
}