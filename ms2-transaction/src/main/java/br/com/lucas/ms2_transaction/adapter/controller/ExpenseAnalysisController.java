package br.com.lucas.ms2_transaction.adapter.controller;

import br.com.lucas.ms2_transaction.adapter.controller.dto.ExpenseAnalysisResponse;
import br.com.lucas.ms2_transaction.adapter.controller.mapper.ExpenseAnalysisControllerMapper;
import br.com.lucas.ms2_transaction.adapter.exception.ApiErrorResponse;
import br.com.lucas.ms2_transaction.application.dto.ExpenseAnalysisData;
import br.com.lucas.ms2_transaction.application.port.in.AnalyzeExpensesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.time.LocalDate;

@Tag(
        name = "Análise de despesas",
        description = "Totais de despesas por categoria, dia e mês"
)
@RestController
@RequestMapping("/expenses")
public class ExpenseAnalysisController {

    private final AnalyzeExpensesUseCase analyzeExpensesUseCase;
    private final ExpenseAnalysisControllerMapper mapper;

    public ExpenseAnalysisController(
            AnalyzeExpensesUseCase analyzeExpensesUseCase,
            ExpenseAnalysisControllerMapper mapper
    ) {
        this.analyzeExpensesUseCase = analyzeExpensesUseCase;
        this.mapper = mapper;
    }

    @Operation(
            summary = "Analisar despesas",
            description = "Calcula as despesas do usuário autenticado dentro do período informado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Análise calculada com sucesso"
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
    @GetMapping("/analysis")
    public ResponseEntity<ExpenseAnalysisResponse> analyze(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate,
            Principal principal
    ) {
        Long ownerUserId = Long.valueOf(principal.getName());

        ExpenseAnalysisData analysisData =
                analyzeExpensesUseCase.analyze(
                        ownerUserId,
                        startDate,
                        endDate
                );

        return ResponseEntity.ok(
                mapper.toResponse(analysisData)
        );
    }
}
