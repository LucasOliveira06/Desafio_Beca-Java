package br.com.lucas.ms2_transaction.application.service;

import br.com.lucas.ms2_transaction.application.dto.CurrencyConversionData;
import br.com.lucas.ms2_transaction.application.port.in.ConvertTransactionCurrencyUseCase;
import br.com.lucas.ms2_transaction.application.port.out.ExchangeRatePort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class CurrencyConversionService
        implements ConvertTransactionCurrencyUseCase {

    private final ExchangeRatePort exchangeRatePort;

    public CurrencyConversionService(
            ExchangeRatePort exchangeRatePort
    ) {
        this.exchangeRatePort = exchangeRatePort;
    }

    @Override
    public List<CurrencyConversionData> convert(
            BigDecimal amount,
            LocalDate date
    ) {
        return List.of(
                convertToCurrency(amount, date, "USD"),
                convertToCurrency(amount, date, "EUR")
        );
    }

    private CurrencyConversionData convertToCurrency(
            BigDecimal amount,
            LocalDate date,
            String currency
    ) {
        return exchangeRatePort
                .findRate(currency, date)
                .filter(exchangeRateData ->
                        exchangeRateData.rate()
                                .compareTo(BigDecimal.ZERO) > 0
                )
                .map(exchangeRateData ->
                        new CurrencyConversionData(
                                currency,
                                amount.divide(
                                        exchangeRateData.rate(),
                                        2,
                                        RoundingMode.HALF_UP
                                ),
                                exchangeRateData.rate(),
                                exchangeRateData.rateDate(),
                                true
                        )
                )
                .orElseGet(() ->
                        new CurrencyConversionData(
                                currency,
                                null,
                                null,
                                null,
                                false
                        )
                );
    }
}