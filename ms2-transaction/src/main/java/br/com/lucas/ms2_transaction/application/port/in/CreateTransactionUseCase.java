package br.com.lucas.ms2_transaction.application.port.in;

import br.com.lucas.ms2_transaction.application.dto.CreateTransactionCommand;
import br.com.lucas.ms2_transaction.domain.model.Transaction;

public interface CreateTransactionUseCase {

    Transaction create(CreateTransactionCommand command);
}