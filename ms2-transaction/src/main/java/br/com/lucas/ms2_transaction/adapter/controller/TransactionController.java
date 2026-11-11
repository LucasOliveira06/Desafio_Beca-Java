package br.com.lucas.ms2_transaction.adapter.controller;

import br.com.lucas.ms2_transaction.adapter.controller.dto.CreateTransactionRequest;
import br.com.lucas.ms2_transaction.adapter.controller.dto.TransactionResponse;
import br.com.lucas.ms2_transaction.adapter.controller.mapper.TransactionControllerMapper;
import br.com.lucas.ms2_transaction.application.dto.CreateTransactionCommand;
import br.com.lucas.ms2_transaction.application.port.in.CreateTransactionUseCase;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final TransactionControllerMapper transactionControllerMapper;

    public TransactionController(
            CreateTransactionUseCase createTransactionUseCase,
            TransactionControllerMapper transactionControllerMapper
    ) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.transactionControllerMapper = transactionControllerMapper;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody CreateTransactionRequest request,
            Principal principal
    ) {
        Long ownerUserId = Long.valueOf(principal.getName());

        CreateTransactionCommand command =
                transactionControllerMapper.toCommand(request, ownerUserId);

        Transaction transaction = createTransactionUseCase.create(command);

        TransactionResponse response =
                transactionControllerMapper.toResponse(transaction);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}