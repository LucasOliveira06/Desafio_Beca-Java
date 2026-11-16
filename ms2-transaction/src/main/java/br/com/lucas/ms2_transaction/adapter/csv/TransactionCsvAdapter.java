package br.com.lucas.ms2_transaction.adapter.csv;

import br.com.lucas.ms2_transaction.application.port.out.TransactionCsvExporterPort;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class TransactionCsvAdapter implements TransactionCsvExporterPort {

    private static final String HEADER =
            "id;type;amount;occurredAt;description;category;"
                    + "ownerUserId;destinationUserId;status;rejectionReason";

    @Override
    public byte[] export(List<Transaction> transactions) {
        StringBuilder csv = new StringBuilder();

        csv.append(HEADER)
                .append(System.lineSeparator());

        for (Transaction transaction : transactions) {
            csv.append(format(transaction.getId()))
                    .append(";")
                    .append(format(transaction.getType()))
                    .append(";")
                    .append(format(transaction.getAmount()))
                    .append(";")
                    .append(format(transaction.getOccurredAt()))
                    .append(";")
                    .append(format(transaction.getDescription()))
                    .append(";")
                    .append(format(transaction.getCategory()))
                    .append(";")
                    .append(format(transaction.getOwnerUserId()))
                    .append(";")
                    .append(format(transaction.getDestinationUserId()))
                    .append(";")
                    .append(format(transaction.getStatus()))
                    .append(";")
                    .append(format(transaction.getRejectionReason()))
                    .append(System.lineSeparator());
        }

        String content = "\uFEFF" + csv;

        return content.getBytes(StandardCharsets.UTF_8);
    }

    private String format(Object value) {
        if (value == null) {
            return "";
        }

        String text = value.toString();

        if (text.contains(";")
                || text.contains("\"")
                || text.contains("\n")
                || text.contains("\r")) {
            return "\"" + text.replace("\"", "\"\"") + "\"";
        }

        return text;
    }
}