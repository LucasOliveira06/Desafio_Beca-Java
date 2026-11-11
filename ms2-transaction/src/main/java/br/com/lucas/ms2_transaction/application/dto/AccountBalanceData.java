package br.com.lucas.ms2_transaction.application.dto;

import java.math.BigDecimal;

public record AccountBalanceData(
        BigDecimal balance,
        BigDecimal dailyLimit,
        BigDecimal transactionLimit
) {
}
