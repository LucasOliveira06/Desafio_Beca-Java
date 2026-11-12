package br.com.lucas.ms2_transaction.application.service;

import br.com.lucas.ms2_transaction.application.dto.AccountBalanceData;
import br.com.lucas.ms2_transaction.application.port.in.GetAccountBalanceUseCase;
import br.com.lucas.ms2_transaction.application.port.out.AccountBalancePort;
import org.springframework.stereotype.Service;

@Service
public class AccountBalanceService implements GetAccountBalanceUseCase {

    private final AccountBalancePort accountBalancePort;

    public AccountBalanceService(AccountBalancePort accountBalancePort) {
        this.accountBalancePort = accountBalancePort;
    }

    @Override
    public AccountBalanceData getByUserId(Long userId) {
        return accountBalancePort.getByUserId(userId);
    }
}