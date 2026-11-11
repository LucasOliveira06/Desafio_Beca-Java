package br.com.lucas.ms2_transaction.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Transaction {

    private final Long id;
    private final TransactionType type;
    private final BigDecimal amount;
    private final LocalDateTime occurredAt;
    private final String description;
    private final String category;
    private final Long ownerUserId;
    private final Long destinationUserId;

    private TransactionStatus status;
    private String rejectionReason;

    public Transaction(
            Long id,
            TransactionType type,
            BigDecimal amount,
            LocalDateTime occurredAt,
            String description,
            String category,
            Long ownerUserId,
            Long destinationUserId
    ) {
        validateData(
                type,
                amount,
                occurredAt,
                description,
                category,
                ownerUserId,
                destinationUserId
        );

        this.id = id;
        this.type = type;
        this.amount = amount;
        this.occurredAt = occurredAt;
        this.description = description;
        this.category = category;
        this.ownerUserId = ownerUserId;
        this.destinationUserId = destinationUserId;
    }

    public void approve() {
        this.status = TransactionStatus.APPROVED;
        this.rejectionReason = null;
    }

    public void reject(String reason) {
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "O motivo da rejeição é obrigatório"
            );
        }

        this.status = TransactionStatus.REJECTED;
        this.rejectionReason = reason;
    }

    private void validateData(
            TransactionType type,
            BigDecimal amount,
            LocalDateTime occurredAt,
            String description,
            String category,
            Long ownerUserId,
            Long destinationUserId
    ) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "O tipo da transação é obrigatório"
            );
        }

        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException(
                    "O valor da transação deve ser maior que zero"
            );
        }

        if (occurredAt == null) {
            throw new IllegalArgumentException(
                    "A data e hora da transação são obrigatórias"
            );
        }

        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException(
                    "A descrição da transação é obrigatória"
            );
        }

        if (ownerUserId == null || ownerUserId <= 0) {
            throw new IllegalArgumentException(
                    "O usuário dono da transação é obrigatório"
            );
        }

        if (type == TransactionType.TRANSFER
                && destinationUserId == null) {
            throw new IllegalArgumentException(
                    "A transferência deve informar o usuário de destino"
            );
        }

        if (type == TransactionType.TRANSFER
                && ownerUserId.equals(destinationUserId)) {
            throw new IllegalArgumentException(
                    "O usuário de destino deve ser diferente do remetente"
            );
        }

        if (type == TransactionType.PURCHASE
                && (category == null || category.isBlank())) {
            throw new IllegalArgumentException(
                    "A compra deve informar uma categoria"
            );
        }
    }

    public Long getId() {
        return id;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public Long getOwnerUserId() {
        return ownerUserId;
    }

    public Long getDestinationUserId() {
        return destinationUserId;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }
}
