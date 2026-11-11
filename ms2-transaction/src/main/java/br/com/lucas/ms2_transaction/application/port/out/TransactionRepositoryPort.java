package br.com.lucas.ms2_transaction.application.port.out;

import br.com.lucas.ms2_transaction.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);

    BigDecimal sumApprovedOutgoingByOwnerUserIdAndDate(Long ownerUserId, LocalDate date);
}