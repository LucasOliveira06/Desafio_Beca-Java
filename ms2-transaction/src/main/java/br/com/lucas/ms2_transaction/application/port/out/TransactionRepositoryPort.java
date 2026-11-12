package br.com.lucas.ms2_transaction.application.port.out;

import br.com.lucas.ms2_transaction.domain.model.Transaction;
import br.com.lucas.ms2_transaction.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TransactionRepositoryPort {

    Transaction save(Transaction transaction);

    Optional<Transaction> findByIdAndOwnerUserId(
            Long id,
            Long ownerUserId
    );

    List<Transaction> findAllByOwnerUserIdAndFilters(
            Long ownerUserId,
            TransactionType type,
            LocalDateTime start,
            LocalDateTime end
    );

    BigDecimal sumApprovedOutgoingByOwnerUserIdAndDate(
            Long ownerUserId,
            LocalDate date
    );
}