package br.com.lucas.ms2_transaction.adapter.controller;

import br.com.lucas.ms2_transaction.adapter.controller.dto.CreateTransactionRequest;
import br.com.lucas.ms2_transaction.adapter.controller.dto.TransactionResponse;
import br.com.lucas.ms2_transaction.adapter.controller.mapper.TransactionControllerMapper;
import br.com.lucas.ms2_transaction.application.dto.CreateTransactionCommand;
import br.com.lucas.ms2_transaction.application.port.in.CreateTransactionUseCase;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import br.com.lucas.ms2_transaction.application.port.in.FindTransactionByIdUseCase;
import br.com.lucas.ms2_transaction.application.port.in.ListTransactionsUseCase;
import br.com.lucas.ms2_transaction.domain.model.TransactionType;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final TransactionControllerMapper transactionControllerMapper;
    private final FindTransactionByIdUseCase findTransactionByIdUseCase;
    private final ListTransactionsUseCase listTransactionsUseCase;

    public TransactionController(
            CreateTransactionUseCase createTransactionUseCase,
            FindTransactionByIdUseCase findTransactionByIdUseCase,
            ListTransactionsUseCase listTransactionsUseCase,
            TransactionControllerMapper transactionControllerMapper
    ) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.findTransactionByIdUseCase = findTransactionByIdUseCase;
        this.listTransactionsUseCase = listTransactionsUseCase;
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

    @GetMapping
    public ResponseEntity<List<TransactionResponse>> findAll(
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            Principal principal
    ) {
        Long ownerUserId = Long.valueOf(principal.getName());

        List<TransactionResponse> response =
                listTransactionsUseCase
                        .findAll(
                                ownerUserId,
                                type,
                                startDate,
                                endDate
                        )
                        .stream()
                        .map(transactionControllerMapper::toResponse)
                        .toList();

        return ResponseEntity.ok(response);
    }
}