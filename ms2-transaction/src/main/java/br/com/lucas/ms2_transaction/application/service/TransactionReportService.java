package br.com.lucas.ms2_transaction.application.service;

import br.com.lucas.ms2_transaction.application.port.in.GenerateTransactionReportUseCase;
import br.com.lucas.ms2_transaction.application.port.in.ListTransactionsUseCase;
import br.com.lucas.ms2_transaction.application.port.out.TransactionCsvExporterPort;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionReportService
        implements GenerateTransactionReportUseCase {

    private final ListTransactionsUseCase listTransactionsUseCase;
    private final TransactionCsvExporterPort transactionCsvExporterPort;

    public TransactionReportService(
            ListTransactionsUseCase listTransactionsUseCase,
            TransactionCsvExporterPort transactionCsvExporterPort
    ) {
        this.listTransactionsUseCase = listTransactionsUseCase;
        this.transactionCsvExporterPort = transactionCsvExporterPort;
    }

    @Override
    public byte[] generate(
            Long ownerUserId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        List<Transaction> transactions =
                listTransactionsUseCase.findAll(
                        ownerUserId,
                        null,
                        startDate,
                        endDate
                );

        return transactionCsvExporterPort.export(transactions);
    }
}