package br.com.lucas.ms2_transaction.application.port.out;

import br.com.lucas.ms2_transaction.application.dto.AccountBalanceData;

public interface AccountBalancePort {

    AccountBalanceData getByUserId(Long userId);
}