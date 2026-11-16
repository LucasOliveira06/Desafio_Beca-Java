package br.com.lucas.ms2_transaction.adapter.controller;

import br.com.lucas.ms2_transaction.adapter.controller.dto.AccountBalanceResponse;
import br.com.lucas.ms2_transaction.application.dto.AccountBalanceData;
import br.com.lucas.ms2_transaction.application.port.in.GetAccountBalanceUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.security.Principal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountBalanceControllerTest {

    private GetAccountBalanceUseCase getAccountBalanceUseCase;
    private AccountBalanceController accountBalanceController;

    @BeforeEach
    void setUp() {
        getAccountBalanceUseCase = mock(GetAccountBalanceUseCase.class);
        accountBalanceController =
                new AccountBalanceController(getAccountBalanceUseCase);
    }

    @Test
    void deveRetornarSaldoDoUsuarioAutenticado() {
        AccountBalanceData accountBalanceData = new AccountBalanceData(
                new BigDecimal("1000.00"),
                new BigDecimal("1500.00"),
                new BigDecimal("500.00")
        );

        when(getAccountBalanceUseCase.getByUserId(12L))
                .thenReturn(accountBalanceData);

        Principal principal = () -> "12";

        ResponseEntity<AccountBalanceResponse> response =
                accountBalanceController.getBalance(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                new BigDecimal("1000.00"),
                response.getBody().balance()
        );
        assertEquals(
                new BigDecimal("1500.00"),
                response.getBody().dailyLimit()
        );
        assertEquals(
                new BigDecimal("500.00"),
                response.getBody().transactionLimit()
        );

        verify(getAccountBalanceUseCase).getByUserId(12L);
    }

}