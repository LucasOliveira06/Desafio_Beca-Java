package br.com.lucas.ms2_transaction.application.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ExpenseMonthlyTotalData(
        YearMonth month,
        BigDecimal total
) {
}