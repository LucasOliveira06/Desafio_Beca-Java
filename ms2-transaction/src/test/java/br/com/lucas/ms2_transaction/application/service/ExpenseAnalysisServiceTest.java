package br.com.lucas.ms2_transaction.application.service;

import br.com.lucas.ms2_transaction.application.dto.ExpenseAnalysisData;
import br.com.lucas.ms2_transaction.application.exception.InvalidDateRangeException;
import br.com.lucas.ms2_transaction.application.port.out.TransactionRepositoryPort;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import br.com.lucas.ms2_transaction.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExpenseAnalysisServiceTest {

    private TransactionRepositoryPort transactionRepositoryPort;
    private ExpenseAnalysisService expenseAnalysisService;

    @BeforeEach
    void setUp() {
        transactionRepositoryPort =
                mock(TransactionRepositoryPort.class);

        Clock clock = Clock.fixed(
                Instant.parse("2026-11-12T12:00:00Z"),
                ZoneId.of("America/Sao_Paulo")
        );

        expenseAnalysisService = new ExpenseAnalysisService(
                transactionRepositoryPort,
                clock
        );
    }

    @Test
    void deveSomarSomenteDespesasAprovadas() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        Transaction firstPurchase = createTransaction(
                1L,
                TransactionType.PURCHASE,
                "100.00",
                LocalDateTime.of(2026, 11, 10, 10, 0),
                "ALIMENTACAO"
        );
        firstPurchase.approve();

        Transaction secondPurchase = createTransaction(
                2L,
                TransactionType.PURCHASE,
                "50.00",
                LocalDateTime.of(2026, 11, 11, 10, 0),
                "ALIMENTACAO"
        );
        secondPurchase.approve();

        Transaction withdrawal = createTransaction(
                3L,
                TransactionType.WITHDRAWAL,
                "30.00",
                LocalDateTime.of(2026, 11, 11, 15, 0),
                null
        );
        withdrawal.approve();

        Transaction deposit = createTransaction(
                4L,
                TransactionType.DEPOSIT,
                "1000.00",
                LocalDateTime.of(2026, 11, 11, 16, 0),
                null
        );
        deposit.approve();

        Transaction rejectedPurchase = createTransaction(
                5L,
                TransactionType.PURCHASE,
                "200.00",
                LocalDateTime.of(2026, 11, 11, 17, 0),
                "LAZER"
        );
        rejectedPurchase.reject("Saldo insuficiente");

        when(transactionRepositoryPort
                .findAllByOwnerUserIdAndFilters(
                        12L,
                        null,
                        LocalDateTime.of(2026, 11, 1, 0, 0),
                        LocalDateTime.of(2026, 12, 1, 0, 0)
                ))
                .thenReturn(List.of(
                        firstPurchase,
                        secondPurchase,
                        withdrawal,
                        deposit,
                        rejectedPurchase
                ));

        ExpenseAnalysisData result =
                expenseAnalysisService.analyze(
                        12L,
                        startDate,
                        endDate
                );

        assertEquals(
                new BigDecimal("180.00"),
                result.grandTotal()
        );

        assertEquals(2, result.totalsByCategory().size());
        assertEquals(
                "ALIMENTACAO",
                result.totalsByCategory().getFirst().category()
        );
        assertEquals(
                new BigDecimal("150.00"),
                result.totalsByCategory().getFirst().total()
        );
        assertEquals(
                "SEM_CATEGORIA",
                result.totalsByCategory().get(1).category()
        );
        assertEquals(2, result.totalsByDay().size());
        assertEquals(1, result.totalsByMonth().size());
        assertEquals(
                new BigDecimal("180.00"),
                result.totalsByMonth().getFirst().total()
        );
    }

    @Test
    void deveUsarMesAtualQuandoPeriodoNaoForInformado() {
        when(transactionRepositoryPort
                .findAllByOwnerUserIdAndFilters(
                        12L,
                        null,
                        LocalDateTime.of(2026, 11, 1, 0, 0),
                        LocalDateTime.of(2026, 12, 1, 0, 0)
                ))
                .thenReturn(List.of());

        ExpenseAnalysisData result =
                expenseAnalysisService.analyze(
                        12L,
                        null,
                        null
                );

        assertEquals(
                LocalDate.of(2026, 11, 1),
                result.startDate()
        );
        assertEquals(
                LocalDate.of(2026, 11, 30),
                result.endDate()
        );
        assertEquals(BigDecimal.ZERO, result.grandTotal());

        verify(transactionRepositoryPort)
                .findAllByOwnerUserIdAndFilters(
                        12L,
                        null,
                        LocalDateTime.of(2026, 11, 1, 0, 0),
                        LocalDateTime.of(2026, 12, 1, 0, 0)
                );
    }

    @Test
    void deveRejeitarPeriodoInvalido() {
        assertThrows(
                InvalidDateRangeException.class,
                () -> expenseAnalysisService.analyze(
                        12L,
                        LocalDate.of(2026, 11, 30),
                        LocalDate.of(2026, 11, 1)
                )
        );
    }

    private Transaction createTransaction(
            Long id,
            TransactionType type,
            String amount,
            LocalDateTime occurredAt,
            String category
    ) {
        return new Transaction(
                id,
                type,
                new BigDecimal(amount),
                occurredAt,
                "Transação de teste",
                category,
                12L,
                type == TransactionType.TRANSFER ? 20L : null
        );
    }
}