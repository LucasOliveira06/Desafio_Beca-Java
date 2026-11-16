package br.com.lucas.ms2_transaction.adapter.persistence.repository;

import br.com.lucas.ms2_transaction.adapter.persistence.entity.TransactionEntity;
import br.com.lucas.ms2_transaction.domain.model.TransactionStatus;
import br.com.lucas.ms2_transaction.domain.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;
import java.util.List;

public interface SpringDataTransactionRepository
        extends JpaRepository<TransactionEntity, Long> {
    Optional<TransactionEntity> findByIdAndOwnerUserId(
            Long id,
            Long ownerUserId
    );

    @Query("""
        SELECT transaction
        FROM TransactionEntity transaction
        WHERE transaction.ownerUserId = :ownerUserId
          AND (:type IS NULL OR transaction.type = :type)
          AND transaction.occurredAt >= :start
          AND transaction.occurredAt < :end
        ORDER BY transaction.occurredAt DESC
        """)
    List<TransactionEntity> findAllByOwnerUserIdAndFilters(
            @Param("ownerUserId") Long ownerUserId,
            @Param("type") TransactionType type,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("""
            SELECT SUM(transaction.amount)
            FROM TransactionEntity transaction
            WHERE transaction.ownerUserId = :ownerUserId
              AND transaction.status = :status
              AND transaction.type IN :types
              AND transaction.occurredAt >= :start
              AND transaction.occurredAt < :end
            """)
    BigDecimal sumAmountByOwnerUserIdAndDate(
            @Param("ownerUserId") Long ownerUserId,
            @Param("status") TransactionStatus status,
            @Param("types") Collection<TransactionType> types,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );
}