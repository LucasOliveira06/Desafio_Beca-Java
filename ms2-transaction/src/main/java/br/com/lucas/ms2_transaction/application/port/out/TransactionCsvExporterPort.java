package br.com.lucas.ms2_transaction.application.port.out;

import br.com.lucas.ms2_transaction.domain.model.Transaction;

import java.util.List;

public interface TransactionCsvExporterPort {

    byte[] export(List<Transaction> transactions);
}