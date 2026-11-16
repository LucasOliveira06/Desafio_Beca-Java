package br.com.lucas.ms2_transaction.adapter.controller.dto;

import java.math.BigDecimal;

public record ExpenseCategoryTotalResponse(
        String category,
        BigDecimal total
) {
}