package br.com.lucas.ms2_transaction.adapter.controller;

import br.com.lucas.ms2_transaction.adapter.controller.dto.CreateTransactionRequest;
import br.com.lucas.ms2_transaction.adapter.controller.dto.TransactionResponse;
import br.com.lucas.ms2_transaction.adapter.controller.mapper.TransactionControllerMapper;
import br.com.lucas.ms2_transaction.application.dto.CreateTransactionCommand;
import br.com.lucas.ms2_transaction.application.dto.CurrencyConversionData;
import br.com.lucas.ms2_transaction.application.port.in.CreateTransactionUseCase;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import br.com.lucas.ms2_transaction.application.port.in.FindTransactionByIdUseCase;
import br.com.lucas.ms2_transaction.application.port.in.ListTransactionsUseCase;
import br.com.lucas.ms2_transaction.domain.model.TransactionType;
import br.com.lucas.ms2_transaction.application.port.in.ConvertTransactionCurrencyUseCase;
import br.com.lucas.ms2_transaction.adapter.exception.ApiErrorResponse;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

@Tag(
        name = "Transações",
        description = "Criação e consulta das transações do usuário autenticado"
)
@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private final CreateTransactionUseCase createTransactionUseCase;
    private final TransactionControllerMapper transactionControllerMapper;
    private final FindTransactionByIdUseCase findTransactionByIdUseCase;
    private final ListTransactionsUseCase listTransactionsUseCase;
    private final ConvertTransactionCurrencyUseCase convertTransactionCurrencyUseCase;

    public TransactionController(
            CreateTransactionUseCase createTransactionUseCase,
            FindTransactionByIdUseCase findTransactionByIdUseCase,
            ListTransactionsUseCase listTransactionsUseCase,
            ConvertTransactionCurrencyUseCase convertTransactionCurrencyUseCase,
            TransactionControllerMapper transactionControllerMapper
    ) {
        this.createTransactionUseCase = createTransactionUseCase;
        this.findTransactionByIdUseCase = findTransactionByIdUseCase;
        this.listTransactionsUseCase = listTransactionsUseCase;
        this.convertTransactionCurrencyUseCase =
                convertTransactionCurrencyUseCase;
        this.transactionControllerMapper = transactionControllerMapper;
    }

    @Operation(
            summary = "Criar uma transação",
            description = "Cria uma transação para o usuário autenticado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Transação criada com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Dados da transação inválidos",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Serviço de autenticação indisponível"
            )
    })
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

    @Operation(
            summary = "Buscar transação por ID",
            description = "Busca uma transação pertencente ao usuário autenticado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transação encontrada"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Transação não encontrada",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Serviço de autenticação indisponível"
            )
    })
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

        List<CurrencyConversionData> conversions =
                convertTransactionCurrencyUseCase.convert(
                        transaction.getAmount(),
                        transaction.getOccurredAt().toLocalDate()
                );

        TransactionResponse response =
                transactionControllerMapper.toResponse(
                        transaction,
                        conversions
                );

        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Listar transações",
            description = "Lista as transações do usuário autenticado com filtros opcionais"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Transações listadas com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Filtros ou período inválidos",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Serviço de autenticação indisponível"
            )
    })
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
                        .map(transaction -> {
                            List<CurrencyConversionData> conversions =
                                    convertTransactionCurrencyUseCase.convert(
                                            transaction.getAmount(),
                                            transaction.getOccurredAt().toLocalDate()
                                    );

                            return transactionControllerMapper.toResponse(
                                    transaction,
                                    conversions
                            );
                        })
                        .toList();

        return ResponseEntity.ok(response);
    }
}
