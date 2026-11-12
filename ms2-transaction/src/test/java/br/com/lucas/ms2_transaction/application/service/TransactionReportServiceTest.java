package br.com.lucas.ms2_transaction.application.service;

import br.com.lucas.ms2_transaction.application.port.in.ListTransactionsUseCase;
import br.com.lucas.ms2_transaction.application.port.out.TransactionCsvExporterPort;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionReportServiceTest {

    private ListTransactionsUseCase listTransactionsUseCase;
    private TransactionCsvExporterPort transactionCsvExporterPort;
    private TransactionReportService transactionReportService;

    @BeforeEach
    void setUp() {
        listTransactionsUseCase =
                mock(ListTransactionsUseCase.class);

        transactionCsvExporterPort =
                mock(TransactionCsvExporterPort.class);

        transactionReportService = new TransactionReportService(
                listTransactionsUseCase,
                transactionCsvExporterPort
        );
    }

    @Test
    void deveGerarRelatorioComTransacoesDoUsuario() {
        Long ownerUserId = 12L;
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        List<Transaction> transactions = List.of();
        byte[] expectedReport = "relatorio".getBytes();

        when(listTransactionsUseCase.findAll(
                ownerUserId,
                null,
                startDate,
                endDate
        )).thenReturn(transactions);

        when(transactionCsvExporterPort.export(transactions))
                .thenReturn(expectedReport);

        byte[] result = transactionReportService.generate(
                ownerUserId,
                startDate,
                endDate
        );

        assertArrayEquals(expectedReport, result);

        verify(listTransactionsUseCase).findAll(
                ownerUserId,
                null,
                startDate,
                endDate
        );

        verify(transactionCsvExporterPort)
                .export(transactions);
    }
}