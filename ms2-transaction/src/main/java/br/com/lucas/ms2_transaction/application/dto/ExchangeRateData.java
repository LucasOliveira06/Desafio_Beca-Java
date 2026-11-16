package br.com.lucas.ms2_transaction.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ExchangeRateData(
        String currency,
        BigDecimal rate,
        LocalDate rateDate
) {
}