package br.com.lucas.ms2_transaction.adapter.csv;

import br.com.lucas.ms2_transaction.domain.model.Transaction;
import br.com.lucas.ms2_transaction.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionCsvAdapterTest {

    private TransactionCsvAdapter transactionCsvAdapter;

    @BeforeEach
    void setUp() {
        transactionCsvAdapter = new TransactionCsvAdapter();
    }

    @Test
    void deveExportarTransacoesParaCsv() {
        Transaction transaction = new Transaction(
                1L,
                TransactionType.PURCHASE,
                new BigDecimal("100.00"),
                LocalDateTime.of(2026, 11, 12, 10, 30),
                "Compra; mercado",
                "ALIMENTACAO",
                12L,
                null
        );

        transaction.approve();

        byte[] result = transactionCsvAdapter.export(
                List.of(transaction)
        );

        String csv = new String(
                result,
                StandardCharsets.UTF_8
        );

        assertTrue(csv.contains(
                "id;type;amount;occurredAt;description;category;"
                        + "ownerUserId;destinationUserId;status;"
                        + "rejectionReason"
        ));

        assertTrue(csv.contains(
                "1;PURCHASE;100.00;2026-11-12T10:30;"
                        + "\"Compra; mercado\";ALIMENTACAO;"
                        + "12;;APPROVED;"
        ));
    }
}