package br.com.lucas.ms2_transaction.adapter.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CurrencyConversionResponse(
        String currency,
        BigDecimal convertedAmount,
        BigDecimal rate,
        LocalDate rateDate,
        boolean available
) {
}