package br.com.lucas.ms2_transaction.adapter.controller;

import br.com.lucas.ms2_transaction.adapter.controller.dto.CreateTransactionRequest;
import br.com.lucas.ms2_transaction.adapter.controller.dto.TransactionResponse;
import br.com.lucas.ms2_transaction.adapter.controller.mapper.TransactionControllerMapper;
import br.com.lucas.ms2_transaction.application.dto.CreateTransactionCommand;
import br.com.lucas.ms2_transaction.application.port.in.CreateTransactionUseCase;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import br.com.lucas.ms2_transaction.application.port.in.FindTransactionByIdUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.security.Principal;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final TransactionControllerMapper transactionControllerMapper;
    private final FindTransactionByIdUseCase findTransactionByIdUseCase;

    public TransactionController(
            CreateTransactionUseCase createTransactionUseCase,
            FindTransactionByIdUseCase findTransactionByIdUseCase,
            TransactionControllerMapper transactionControllerMapper
    ) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.findTransactionByIdUseCase = findTransactionByIdUseCase;
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

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> findById(
            @PathVariable Long id,
            Principal principal
    ) {
        Long ownerUserId = Long.valueOf(principal.getName());

        Transaction transaction = findTransactionByIdUseCase.findById(
                id,
                ownerUserId
        );

        TransactionResponse response =
                transactionControllerMapper.toResponse(transaction);

        return ResponseEntity.ok(response);
    }
}