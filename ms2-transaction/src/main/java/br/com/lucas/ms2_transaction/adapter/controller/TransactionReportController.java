package br.com.lucas.ms2_transaction.adapter.controller;

import br.com.lucas.ms2_transaction.adapter.exception.ApiErrorResponse;
import br.com.lucas.ms2_transaction.application.port.in.GenerateTransactionReportUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;

@Tag(
        name = "Relatório de transações",
        description = "Exportação das transações do usuário em arquivo CSV"
)
@RestController
@RequestMapping("/transactions/report")
public class TransactionReportController {

    private final GenerateTransactionReportUseCase
            generateTransactionReportUseCase;

    public TransactionReportController(
            GenerateTransactionReportUseCase generateTransactionReportUseCase
    ) {
        this.generateTransactionReportUseCase =
                generateTransactionReportUseCase;
    }

    @Operation(
            summary = "Exportar relatório CSV",
            description = "Gera um arquivo CSV com as transações do usuário autenticado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Relatório CSV gerado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Período informado inválido",
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
    public ResponseEntity<byte[]> generate(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,

            Principal principal
    ) {
        Long ownerUserId = Long.valueOf(principal.getName());

        byte[] report = generateTransactionReportUseCase.generate(
                ownerUserId,
                startDate,
                endDate
        );

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=transactions.csv"
                )
                .body(report);
    }
}
