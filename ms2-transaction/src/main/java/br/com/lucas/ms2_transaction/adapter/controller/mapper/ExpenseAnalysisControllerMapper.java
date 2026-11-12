package br.com.lucas.ms2_transaction.adapter.controller.mapper;

import br.com.lucas.ms2_transaction.adapter.controller.dto.ExpenseAnalysisResponse;
import br.com.lucas.ms2_transaction.adapter.controller.dto.ExpenseCategoryTotalResponse;
import br.com.lucas.ms2_transaction.adapter.controller.dto.ExpenseDailyTotalResponse;
import br.com.lucas.ms2_transaction.adapter.controller.dto.ExpenseMonthlyTotalResponse;
import br.com.lucas.ms2_transaction.application.dto.ExpenseAnalysisData;
import org.springframework.stereotype.Component;

@Component
public class ExpenseAnalysisControllerMapper {

    public ExpenseAnalysisResponse toResponse(
            ExpenseAnalysisData analysisData
    ) {
        return new ExpenseAnalysisResponse(
                analysisData.startDate(),
                analysisData.endDate(),
                analysisData.totalsByCategory()
                        .stream()
                        .map(category ->
                                new ExpenseCategoryTotalResponse(
                                        category.category(),
                                        category.total()
                                )
                        )
                        .toList(),
                analysisData.totalsByDay()
                        .stream()
                        .map(day ->
                                new ExpenseDailyTotalResponse(
                                        day.date(),
                                        day.total()
                                )
                        )
                        .toList(),
                analysisData.totalsByMonth()
                        .stream()
                        .map(month ->
                                new ExpenseMonthlyTotalResponse(
                                        month.month(),
                                        month.total()
                                )
                        )
                        .toList(),
                analysisData.grandTotal()
        );
    }
}