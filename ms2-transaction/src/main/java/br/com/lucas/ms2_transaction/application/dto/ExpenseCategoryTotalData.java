package br.com.lucas.ms2_transaction.application.dto;

import java.math.BigDecimal;

public record ExpenseCategoryTotalData(
        String category,
        BigDecimal total
) {
}