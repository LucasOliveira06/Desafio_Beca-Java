package br.com.lucas.ms2_transaction.adapter.controller.mapper;

import br.com.lucas.ms2_transaction.adapter.controller.dto.CreateTransactionRequest;
import br.com.lucas.ms2_transaction.adapter.controller.dto.CurrencyConversionResponse;
import br.com.lucas.ms2_transaction.adapter.controller.dto.TransactionResponse;
import br.com.lucas.ms2_transaction.application.dto.CreateTransactionCommand;
import br.com.lucas.ms2_transaction.application.dto.CurrencyConversionData;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TransactionControllerMapper {

    public CreateTransactionCommand toCommand(
            CreateTransactionRequest request,
            Long ownerUserId
    ) {
        return new CreateTransactionCommand(
                request.type(),
                request.amount(),
                request.description(),
                request.category(),
                ownerUserId,
                request.destinationUserId()
        );
    }

    public TransactionResponse toResponse(Transaction transaction) {
        return toResponse(transaction, List.of());
    }

    public TransactionResponse toResponse(
            Transaction transaction,
            List<CurrencyConversionData> conversions
    ) {
        List<CurrencyConversionResponse> conversionResponses =
                conversions.stream()
                        .map(conversion ->
                                new CurrencyConversionResponse(
                                        conversion.currency(),
                                        conversion.convertedAmount(),
                                        conversion.rate(),
                                        conversion.rateDate(),
                                        conversion.available()
                                )
                        )
                        .toList();

        return new TransactionResponse(
                transaction.getId(),
                transaction.getType(),
                transaction.getAmount(),
                transaction.getOccurredAt(),
                transaction.getDescription(),
                transaction.getCategory(),
                transaction.getOwnerUserId(),
                transaction.getDestinationUserId(),
                transaction.getStatus(),
                transaction.getRejectionReason(),
                conversionResponses
        );
    }
}