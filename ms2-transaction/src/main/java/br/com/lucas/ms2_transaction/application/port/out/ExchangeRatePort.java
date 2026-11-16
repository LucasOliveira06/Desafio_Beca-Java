package br.com.lucas.ms2_transaction.application.port.out;

import br.com.lucas.ms2_transaction.application.dto.ExchangeRateData;

import java.time.LocalDate;
import java.util.Optional;

public interface ExchangeRatePort {

    Optional<ExchangeRateData> findRate(
            String currency,
            LocalDate date
    );
}