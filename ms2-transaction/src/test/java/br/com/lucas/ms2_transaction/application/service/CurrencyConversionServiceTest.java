package br.com.lucas.ms2_transaction.application.service;

import br.com.lucas.ms2_transaction.application.dto.CurrencyConversionData;
import br.com.lucas.ms2_transaction.application.dto.ExchangeRateData;
import br.com.lucas.ms2_transaction.application.port.out.ExchangeRatePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrencyConversionServiceTest {

    private ExchangeRatePort exchangeRatePort;
    private CurrencyConversionService currencyConversionService;

    @BeforeEach
    void setUp() {
        exchangeRatePort = mock(ExchangeRatePort.class);
        currencyConversionService =
                new CurrencyConversionService(exchangeRatePort);
    }

    @Test
    void deveConverterValorParaDolarEEuro() {
        LocalDate date = LocalDate.of(2026, 11, 11);

        when(exchangeRatePort.findRate("USD", date))
                .thenReturn(Optional.of(
                        new ExchangeRateData(
                                "USD",
                                new BigDecimal("5.00"),
                                date
                        )
                ));

        when(exchangeRatePort.findRate("EUR", date))
                .thenReturn(Optional.of(
                        new ExchangeRateData(
                                "EUR",
                                new BigDecimal("10.00"),
                                date
                        )
                ));

        List<CurrencyConversionData> result =
                currencyConversionService.convert(
                        new BigDecimal("100.00"),
                        date
                );

        assertEquals(2, result.size());

        assertEquals("USD", result.getFirst().currency());
        assertEquals(
                new BigDecimal("20.00"),
                result.getFirst().convertedAmount()
        );
        assertTrue(result.getFirst().available());

        assertEquals("EUR", result.get(1).currency());
        assertEquals(
                new BigDecimal("10.00"),
                result.get(1).convertedAmount()
        );
        assertTrue(result.get(1).available());
    }

    @Test
    void deveInformarConversaoIndisponivelQuandoApiFalhar() {
        LocalDate date = LocalDate.of(2026, 11, 11);

        when(exchangeRatePort.findRate("USD", date))
                .thenReturn(Optional.empty());

        when(exchangeRatePort.findRate("EUR", date))
                .thenReturn(Optional.empty());

        List<CurrencyConversionData> result =
                currencyConversionService.convert(
                        new BigDecimal("100.00"),
                        date
                );

        assertEquals(2, result.size());

        assertFalse(result.getFirst().available());
        assertNull(result.getFirst().convertedAmount());
        assertNull(result.getFirst().rate());
        assertNull(result.getFirst().rateDate());

        assertFalse(result.get(1).available());
        assertNull(result.get(1).convertedAmount());
    }
}