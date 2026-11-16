package br.com.lucas.ms2_transaction.application.port.in;

import br.com.lucas.ms2_transaction.application.dto.AccountBalanceData;

public interface GetAccountBalanceUseCase {

    AccountBalanceData getByUserId(Long userId);
}