package br.com.lucas.ms2_transaction.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExpenseDailyTotalData(
        LocalDate date,
        BigDecimal total
) {
}