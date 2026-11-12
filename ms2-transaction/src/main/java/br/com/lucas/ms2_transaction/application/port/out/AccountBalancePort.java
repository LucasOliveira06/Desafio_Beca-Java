package br.com.lucas.ms2_transaction.application.port.out;

import br.com.lucas.ms2_transaction.application.dto.AccountBalanceData;

import java.math.BigDecimal;

public interface AccountBalancePort {

    AccountBalanceData getByUserId(Long userId);

    void updateBalance(Long userId, BigDecimal balance);
}
