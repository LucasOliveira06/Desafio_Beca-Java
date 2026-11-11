package br.com.lucas.ms2_transaction.adapter.controller.mapper;

import br.com.lucas.ms2_transaction.adapter.controller.dto.CreateTransactionRequest;
import br.com.lucas.ms2_transaction.adapter.controller.dto.TransactionResponse;
import br.com.lucas.ms2_transaction.application.dto.CreateTransactionCommand;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import org.springframework.stereotype.Component;

@Component
public class TransactionControllerMapper {

    public CreateTransactionCommand toCommand(
            CreateTransactionRequest request,
            Long ownerUserId
    ) {
        return new CreateTransactionCommand(
                request.type(),
                request.amount(),
                request.occurredAt(),
                request.description(),
                request.category(),
                ownerUserId,
                request.destinationUserId()
        );
    }

    public TransactionResponse toResponse(Transaction transaction) {
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
                transaction.getRejectionReason()
        );
    }
}