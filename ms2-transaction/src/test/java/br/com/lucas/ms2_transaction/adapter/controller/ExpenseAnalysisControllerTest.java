package br.com.lucas.ms2_transaction.adapter.controller;

import br.com.lucas.ms2_transaction.adapter.controller.dto.ExpenseAnalysisResponse;
import br.com.lucas.ms2_transaction.adapter.controller.mapper.ExpenseAnalysisControllerMapper;
import br.com.lucas.ms2_transaction.application.dto.ExpenseAnalysisData;
import br.com.lucas.ms2_transaction.application.dto.ExpenseCategoryTotalData;
import br.com.lucas.ms2_transaction.application.dto.ExpenseDailyTotalData;
import br.com.lucas.ms2_transaction.application.dto.ExpenseMonthlyTotalData;
import br.com.lucas.ms2_transaction.application.port.in.AnalyzeExpensesUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ExpenseAnalysisControllerTest {

    private AnalyzeExpensesUseCase analyzeExpensesUseCase;
    private ExpenseAnalysisController expenseAnalysisController;

    @BeforeEach
    void setUp() {
        analyzeExpensesUseCase =
                mock(AnalyzeExpensesUseCase.class);

        expenseAnalysisController = new ExpenseAnalysisController(
                analyzeExpensesUseCase,
                new ExpenseAnalysisControllerMapper()
        );
    }

    @Test
    void deveAnalisarDespesasDoUsuarioAutenticado() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        ExpenseAnalysisData analysisData =
                new ExpenseAnalysisData(
                        startDate,
                        endDate,
                        List.of(
                                new ExpenseCategoryTotalData(
                                        "ALIMENTACAO",
                                        new BigDecimal("150.00")
                                )
                        ),
                        List.of(
                                new ExpenseDailyTotalData(
                                        LocalDate.of(2026, 11, 10),
                                        new BigDecimal("150.00")
                                )
                        ),
                        List.of(
                                new ExpenseMonthlyTotalData(
                                        YearMonth.of(2026, 11),
                                        new BigDecimal("150.00")
                                )
                        ),
                        new BigDecimal("150.00")
                );

        when(analyzeExpensesUseCase.analyze(
                12L,
                startDate,
                endDate
        )).thenReturn(analysisData);

        Principal principal = () -> "12";

        ResponseEntity<ExpenseAnalysisResponse> response =
                expenseAnalysisController.analyze(
                        startDate,
                        endDate,
                        principal
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(
                new BigDecimal("150.00"),
                response.getBody().grandTotal()
        );
        assertEquals(
                "ALIMENTACAO",
                response.getBody()
                        .totalsByCategory()
                        .getFirst()
                        .category()
        );

        verify(analyzeExpensesUseCase).analyze(
                12L,
                startDate,
                endDate
        );
    }
}