package br.com.lucas.ms2_transaction.application.port.out;

import br.com.lucas.ms2_transaction.domain.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);

    Optional<Transaction> findByIdAndOwnerUserId(
            Long id,
            Long ownerUserId
    );

    BigDecimal sumApprovedOutgoingByOwnerUserIdAndDate(
            Long ownerUserId,
            LocalDate date
    );
}