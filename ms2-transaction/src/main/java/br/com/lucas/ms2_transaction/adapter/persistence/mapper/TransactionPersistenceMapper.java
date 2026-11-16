package br.com.lucas.ms2_transaction.adapter.persistence.mapper;

import br.com.lucas.ms2_transaction.adapter.persistence.entity.TransactionEntity;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionPersistenceMapper {

    public TransactionEntity toEntity(Transaction transaction) {
        return new TransactionEntity(
                transaction.getType(),
                transaction.getAmount(),
                transaction.getOccurredAt(),
                transaction.getDescription(),
                transaction.getCategory(),
                transaction.getOwnerUserId(),
                transaction.getDestinationUserId(),
                transaction.getStatus(),
                transaction.getRejectionReason()
        );
    }

    public Transaction toDomain(TransactionEntity entity) {
        Transaction transaction = new Transaction(
                entity.getId(),
                entity.getType(),
                entity.getAmount(),
                entity.getOccurredAt(),
                entity.getDescription(),
                entity.getCategory(),
                entity.getOwnerUserId(),
                entity.getDestinationUserId()
        );

        if (entity.getStatus().name().equals("APPROVED")) {
            transaction.approve();
        } else {
            transaction.reject(entity.getRejectionReason());
        }

        return transaction;
    }
}