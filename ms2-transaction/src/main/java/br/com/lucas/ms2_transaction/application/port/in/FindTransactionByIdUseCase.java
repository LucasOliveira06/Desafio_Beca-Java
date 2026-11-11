package br.com.lucas.ms2_transaction.application.port.in;

import br.com.lucas.ms2_transaction.domain.model.Transaction;

public interface FindTransactionByIdUseCase {

    Transaction findById(
            Long id,
            Long ownerUserId
    );
}