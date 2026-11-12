package br.com.lucas.ms2_transaction.application.dto;

import br.com.lucas.ms2_transaction.domain.model.TransactionType;

import java.math.BigDecimal;

public record CreateTransactionCommand(
        TransactionType type,
        BigDecimal amount,
        String description,
        String category,
        Long ownerUserId,
        Long destinationUserId
) {
}
