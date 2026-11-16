package br.com.lucas.ms2_transaction.application.port.in;

import br.com.lucas.ms2_transaction.domain.model.Transaction;
import br.com.lucas.ms2_transaction.domain.model.TransactionType;

import java.time.LocalDate;
import java.util.List;

public interface ListTransactionsUseCase {

    List<Transaction> findAll(
            Long ownerUserId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    );
}