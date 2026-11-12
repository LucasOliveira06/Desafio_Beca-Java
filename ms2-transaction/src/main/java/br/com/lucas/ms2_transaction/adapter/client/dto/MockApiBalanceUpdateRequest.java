package br.com.lucas.ms2_transaction.adapter.client.dto;

import java.math.BigDecimal;

public record MockApiBalanceUpdateRequest(
        Long userId,
        BigDecimal balance,
        BigDecimal dailyLimit,
        BigDecimal transactionLimit
) {
}