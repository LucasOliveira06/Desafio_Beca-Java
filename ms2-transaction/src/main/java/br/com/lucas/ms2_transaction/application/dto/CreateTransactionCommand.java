package br.com.lucas.ms2_transaction.application.dto;

import br.com.lucas.ms2_transaction.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CreateTransactionCommand(
        TransactionType type,
        BigDecimal amount,
        LocalDateTime occurredAt,
        String description,
        String category,
        Long ownerUserId,
        Long destinationUserId
) {
}
