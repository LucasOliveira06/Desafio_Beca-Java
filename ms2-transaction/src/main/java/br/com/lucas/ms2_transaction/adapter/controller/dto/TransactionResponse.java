package br.com.lucas.ms2_transaction.adapter.controller.dto;

import br.com.lucas.ms2_transaction.domain.model.TransactionStatus;
import br.com.lucas.ms2_transaction.domain.model.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record TransactionResponse(
        Long id,
        TransactionType type,
        BigDecimal amount,
        LocalDateTime occurredAt,
        String description,
        String category,
        Long ownerUserId,
        Long destinationUserId,
        TransactionStatus status,
        String rejectionReason,
        List<CurrencyConversionResponse> conversions
) {
}