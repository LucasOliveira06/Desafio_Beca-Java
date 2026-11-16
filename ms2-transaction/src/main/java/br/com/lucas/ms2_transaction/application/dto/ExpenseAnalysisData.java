package br.com.lucas.ms2_transaction.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseAnalysisData(
        LocalDate startDate,
        LocalDate endDate,
        List<ExpenseCategoryTotalData> totalsByCategory,
        List<ExpenseDailyTotalData> totalsByDay,
        List<ExpenseMonthlyTotalData> totalsByMonth,
        BigDecimal grandTotal
) {
}