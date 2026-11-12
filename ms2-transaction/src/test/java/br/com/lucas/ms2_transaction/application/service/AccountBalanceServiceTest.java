package br.com.lucas.ms2_transaction.application.service;

import br.com.lucas.ms2_transaction.application.dto.AccountBalanceData;
import br.com.lucas.ms2_transaction.application.port.out.AccountBalancePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountBalanceServiceTest {

    private AccountBalancePort accountBalancePort;
    private AccountBalanceService accountBalanceService;

    @BeforeEach
    void setUp() {
        accountBalancePort = mock(AccountBalancePort.class);
        accountBalanceService = new AccountBalanceService(accountBalancePort);
    }

    @Test
    void deveConsultarSaldoDoUsuario() {
        AccountBalanceData expected = new AccountBalanceData(
                new BigDecimal("1000.00"),
                new BigDecimal("1500.00"),
                new BigDecimal("500.00")
        );

        when(accountBalancePort.getByUserId(12L))
                .thenReturn(expected);

        AccountBalanceData result =
                accountBalanceService.getByUserId(12L);

        assertEquals(expected, result);
        verify(accountBalancePort).getByUserId(12L);
    }
}