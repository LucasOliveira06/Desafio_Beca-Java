package br.com.lucas.ms2_transaction.adapter.client.dto;

import java.math.BigDecimal;

public record MockApiAccountResponse(
        String id,
        Long userId,
        BigDecimal balance,
        BigDecimal dailyLimit,
        BigDecimal transactionLimit
) {
}
