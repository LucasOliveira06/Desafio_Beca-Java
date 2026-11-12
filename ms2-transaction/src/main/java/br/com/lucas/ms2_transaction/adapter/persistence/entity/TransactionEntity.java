package br.com.lucas.ms2_transaction.adapter.persistence.entity;

import br.com.lucas.ms2_transaction.domain.model.TransactionStatus;
import br.com.lucas.ms2_transaction.domain.model.TransactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
public class TransactionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    @Column(nullable = false)
    private String description;

    private String category;

    @Column(name = "owner_user_id", nullable = false)
    private Long ownerUserId;

    @Column(name = "destination_user_id")
    private Long destinationUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(name = "rejection_reason")
    private String rejectionReason;

    @Column(
            name = "created_at",
            insertable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    protected TransactionEntity() {
    }

    public TransactionEntity(
            TransactionType type,
            BigDecimal amount,
            LocalDateTime occurredAt,
            String description,
            String category,
            Long ownerUserId,
            Long destinationUserId,
            TransactionStatus status,
            String rejectionReason
    ) {
        this.type = type;
        this.amount = amount;
        this.occurredAt = occurredAt;
        this.description = description;
        this.category = category;
        this.ownerUserId = ownerUserId;
        this.destinationUserId = destinationUserId;
        this.status = status;
        this.rejectionReason = rejectionReason;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}