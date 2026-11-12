package br.com.lucas.ms2_transaction.application.service;

import br.com.lucas.ms2_transaction.application.dto.ExpenseAnalysisData;
import br.com.lucas.ms2_transaction.application.dto.ExpenseCategoryTotalData;
import br.com.lucas.ms2_transaction.application.dto.ExpenseDailyTotalData;
import br.com.lucas.ms2_transaction.application.dto.ExpenseMonthlyTotalData;
import br.com.lucas.ms2_transaction.application.exception.InvalidDateRangeException;
import br.com.lucas.ms2_transaction.application.port.in.AnalyzeExpensesUseCase;
import br.com.lucas.ms2_transaction.application.port.out.TransactionRepositoryPort;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import br.com.lucas.ms2_transaction.domain.model.TransactionStatus;
import br.com.lucas.ms2_transaction.domain.model.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class ExpenseAnalysisService implements AnalyzeExpensesUseCase {

    private static final String NO_CATEGORY = "SEM_CATEGORIA";

    private static final EnumSet<TransactionType> EXPENSE_TYPES =
            EnumSet.of(
                    TransactionType.WITHDRAWAL,
                    TransactionType.TRANSFER,
                    TransactionType.PURCHASE
            );

    private final TransactionRepositoryPort transactionRepositoryPort;
    private final Clock clock;

    public ExpenseAnalysisService(
            TransactionRepositoryPort transactionRepositoryPort,
            Clock clock
    ) {
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.clock = clock;
    }

    @Override
    public ExpenseAnalysisData analyze(
            Long ownerUserId,
            LocalDate startDate,
            LocalDate endDate
    ) {
        LocalDate today = LocalDate.now(clock);

        LocalDate effectiveStartDate;
        LocalDate effectiveEndDate;

        if (startDate == null && endDate == null) {
            effectiveStartDate = today.withDayOfMonth(1);
            effectiveEndDate =
                    today.with(TemporalAdjusters.lastDayOfMonth());
        } else {
            effectiveStartDate = startDate != null
                    ? startDate
                    : endDate.withDayOfMonth(1);

            effectiveEndDate = endDate != null
                    ? endDate
                    : startDate.with(
                    TemporalAdjusters.lastDayOfMonth()
            );
        }

        if (effectiveStartDate.isAfter(effectiveEndDate)) {
            throw new InvalidDateRangeException();
        }

        LocalDateTime start =
                effectiveStartDate.atStartOfDay();

        LocalDateTime end =
                effectiveEndDate.plusDays(1).atStartOfDay();

        List<Transaction> expenses = transactionRepositoryPort
                .findAllByOwnerUserIdAndFilters(
                        ownerUserId,
                        null,
                        start,
                        end
                )
                .stream()
                .filter(transaction ->
                        transaction.getStatus()
                                == TransactionStatus.APPROVED
                )
                .filter(transaction ->
                        EXPENSE_TYPES.contains(transaction.getType())
                )
                .toList();

        Map<String, BigDecimal> categoryTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        this::resolveCategory,
                        TreeMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));

        Map<LocalDate, BigDecimal> dailyTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        transaction ->
                                transaction.getOccurredAt().toLocalDate(),
                        TreeMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));

        Map<YearMonth, BigDecimal> monthlyTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        transaction ->
                                YearMonth.from(transaction.getOccurredAt()),
                        TreeMap::new,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));

        List<ExpenseCategoryTotalData> totalsByCategory =
                categoryTotals.entrySet()
                        .stream()
                        .map(entry ->
                                new ExpenseCategoryTotalData(
                                        entry.getKey(),
                                        entry.getValue()
                                )
                        )
                        .toList();

        List<ExpenseDailyTotalData> totalsByDay =
                dailyTotals.entrySet()
                        .stream()
                        .map(entry ->
                                new ExpenseDailyTotalData(
                                        entry.getKey(),
                                        entry.getValue()
                                )
                        )
                        .toList();

        List<ExpenseMonthlyTotalData> totalsByMonth =
                monthlyTotals.entrySet()
                        .stream()
                        .map(entry ->
                                new ExpenseMonthlyTotalData(
                                        entry.getKey(),
                                        entry.getValue()
                                )
                        )
                        .toList();

        BigDecimal grandTotal = expenses.stream()
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ExpenseAnalysisData(
                effectiveStartDate,
                effectiveEndDate,
                totalsByCategory,
                totalsByDay,
                totalsByMonth,
                grandTotal
        );
    }

    private String resolveCategory(Transaction transaction) {
        if (transaction.getCategory() == null
                || transaction.getCategory().isBlank()) {
            return NO_CATEGORY;
        }

        return transaction.getCategory();
    }
}