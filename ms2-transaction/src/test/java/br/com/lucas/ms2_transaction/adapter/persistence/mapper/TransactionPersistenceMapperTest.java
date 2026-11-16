package br.com.lucas.ms2_transaction.adapter.persistence.mapper;

import br.com.lucas.ms2_transaction.adapter.persistence.entity.TransactionEntity;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import br.com.lucas.ms2_transaction.domain.model.TransactionStatus;
import br.com.lucas.ms2_transaction.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class TransactionPersistenceMapperTest {

    private TransactionPersistenceMapper mapper;
    private LocalDateTime occurredAt;

    @BeforeEach
    void setUp() {
        mapper = new TransactionPersistenceMapper();
        occurredAt = LocalDateTime.of(2026, 11, 12, 10, 30);
    }

    @Test
    void deveConverterDominioParaEntidade() {
        Transaction transaction = new Transaction(
                1L,
                TransactionType.PURCHASE,
                new BigDecimal("100.00"),
                occurredAt,
                "Compra de mercado",
                "ALIMENTACAO",
                12L,
                null
        );

        transaction.approve();

        TransactionEntity entity = mapper.toEntity(transaction);

        assertEquals(TransactionType.PURCHASE, entity.getType());
        assertEquals(new BigDecimal("100.00"), entity.getAmount());
        assertEquals(occurredAt, entity.getOccurredAt());
        assertEquals("Compra de mercado", entity.getDescription());
        assertEquals("ALIMENTACAO", entity.getCategory());
        assertEquals(12L, entity.getOwnerUserId());
        assertNull(entity.getDestinationUserId());
        assertEquals(TransactionStatus.APPROVED, entity.getStatus());
        assertNull(entity.getRejectionReason());
    }

    @Test
    void deveConverterEntidadeAprovadaParaDominio() {
        TransactionEntity entity = new TransactionEntity(
                TransactionType.DEPOSIT,
                new BigDecimal("50.00"),
                occurredAt,
                "Depósito",
                null,
                12L,
                null,
                TransactionStatus.APPROVED,
                null
        );

        Transaction transaction = mapper.toDomain(entity);

        assertEquals(TransactionType.DEPOSIT, transaction.getType());
        assertEquals(new BigDecimal("50.00"), transaction.getAmount());
        assertEquals(occurredAt, transaction.getOccurredAt());
        assertEquals("Depósito", transaction.getDescription());
        assertEquals(12L, transaction.getOwnerUserId());
        assertEquals(TransactionStatus.APPROVED, transaction.getStatus());
        assertNull(transaction.getRejectionReason());
    }

    @Test
    void deveConverterEntidadeRejeitadaParaDominio() {
        TransactionEntity entity = new TransactionEntity(
                TransactionType.PURCHASE,
                new BigDecimal("200.00"),
                occurredAt,
                "Compra rejeitada",
                "ALIMENTACAO",
                12L,
                null,
                TransactionStatus.REJECTED,
                "Saldo insuficiente"
        );

        Transaction transaction = mapper.toDomain(entity);

        assertEquals(TransactionStatus.REJECTED, transaction.getStatus());
        assertEquals(
                "Saldo insuficiente",
                transaction.getRejectionReason()
        );
    }
}