package br.com.lucas.ms2_transaction.adapter.controller.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public record ExpenseMonthlyTotalResponse(
        YearMonth month,
        BigDecimal total
) {
}