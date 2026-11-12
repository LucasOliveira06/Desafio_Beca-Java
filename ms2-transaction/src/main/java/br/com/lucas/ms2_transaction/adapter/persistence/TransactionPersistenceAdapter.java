package br.com.lucas.ms2_transaction.adapter.persistence;

import br.com.lucas.ms2_transaction.adapter.persistence.entity.TransactionEntity;
import br.com.lucas.ms2_transaction.adapter.persistence.mapper.TransactionPersistenceMapper;
import br.com.lucas.ms2_transaction.adapter.persistence.repository.SpringDataTransactionRepository;
import br.com.lucas.ms2_transaction.application.port.out.TransactionRepositoryPort;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import br.com.lucas.ms2_transaction.domain.model.TransactionStatus;
import br.com.lucas.ms2_transaction.domain.model.TransactionType;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class TransactionPersistenceAdapter implements TransactionRepositoryPort {

    private final SpringDataTransactionRepository springDataTransactionRepository;
    private final TransactionPersistenceMapper transactionPersistenceMapper;

    public TransactionPersistenceAdapter(
            SpringDataTransactionRepository springDataTransactionRepository,
            TransactionPersistenceMapper transactionPersistenceMapper
    ) {
        this.springDataTransactionRepository = springDataTransactionRepository;
        this.transactionPersistenceMapper = transactionPersistenceMapper;
    }

    @Override
    public Transaction save(Transaction transaction) {
        TransactionEntity entity =
                transactionPersistenceMapper.toEntity(transaction);

        TransactionEntity savedEntity =
                springDataTransactionRepository.save(entity);

        return transactionPersistenceMapper.toDomain(savedEntity);
    }
    @Override
    public Optional<Transaction> findByIdAndOwnerUserId(
            Long id,
            Long ownerUserId
    ) {
        return springDataTransactionRepository
                .findByIdAndOwnerUserId(id, ownerUserId)
                .map(transactionPersistenceMapper::toDomain);
    }

    @Override
    public List<Transaction> findAllByOwnerUserIdAndFilters(
            Long ownerUserId,
            TransactionType type,
            LocalDateTime start,
            LocalDateTime end
    ) {
        return springDataTransactionRepository
                .findAllByOwnerUserIdAndFilters(
                        ownerUserId,
                        type,
                        start,
                        end
                )
                .stream()
                .map(transactionPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public BigDecimal sumApprovedOutgoingByOwnerUserIdAndDate(
            Long ownerUserId,
            LocalDate date
    ) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();

        BigDecimal total =
                springDataTransactionRepository.sumAmountByOwnerUserIdAndDate(
                        ownerUserId,
                        TransactionStatus.APPROVED,
                        List.of(
                                TransactionType.WITHDRAWAL,
                                TransactionType.TRANSFER,
                                TransactionType.PURCHASE
                        ),
                        start,
                        end
                );

        return total == null ? BigDecimal.ZERO : total;
    }
}