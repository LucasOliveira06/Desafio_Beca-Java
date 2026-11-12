package br.com.lucas.ms2_transaction.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CurrencyConversionData(
        String currency,
        BigDecimal convertedAmount,
        BigDecimal rate,
        LocalDate rateDate,
        boolean available
) {
}