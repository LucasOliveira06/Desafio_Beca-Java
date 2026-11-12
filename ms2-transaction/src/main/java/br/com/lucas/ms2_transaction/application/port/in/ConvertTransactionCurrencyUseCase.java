package br.com.lucas.ms2_transaction.application.port.in;

import br.com.lucas.ms2_transaction.application.dto.CurrencyConversionData;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ConvertTransactionCurrencyUseCase {

    List<CurrencyConversionData> convert(
            BigDecimal amount,
            LocalDate date
    );
}