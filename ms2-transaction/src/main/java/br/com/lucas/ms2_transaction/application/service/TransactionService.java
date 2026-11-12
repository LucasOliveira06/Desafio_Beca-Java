package br.com.lucas.ms2_transaction.application.service;

import br.com.lucas.ms2_transaction.application.dto.AccountBalanceData;
import br.com.lucas.ms2_transaction.application.dto.CreateTransactionCommand;
import br.com.lucas.ms2_transaction.application.exception.AccountBalanceUnavailableException;
import br.com.lucas.ms2_transaction.application.port.in.CreateTransactionUseCase;
import br.com.lucas.ms2_transaction.application.port.out.AccountBalancePort;
import br.com.lucas.ms2_transaction.application.port.out.TransactionRepositoryPort;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import br.com.lucas.ms2_transaction.domain.model.TransactionType;
import br.com.lucas.ms2_transaction.application.exception.TransactionNotFoundException;
import br.com.lucas.ms2_transaction.application.port.in.FindTransactionByIdUseCase;
import br.com.lucas.ms2_transaction.application.exception.InvalidDateRangeException;
import br.com.lucas.ms2_transaction.application.port.in.ListTransactionsUseCase;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

@Service
public class TransactionService implements
        CreateTransactionUseCase,
        FindTransactionByIdUseCase,
        ListTransactionsUseCase {

    private final TransactionRepositoryPort transactionRepositoryPort;
    private final AccountBalancePort accountBalancePort;
    private final Clock clock;

    public TransactionService(
            TransactionRepositoryPort transactionRepositoryPort,
            AccountBalancePort accountBalancePort,
            Clock clock
    ) {
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.accountBalancePort = accountBalancePort;
        this.clock = clock;
    }

    @Override
    public synchronized Transaction create(CreateTransactionCommand command) {
        LocalDateTime occurredAt = LocalDateTime.now(clock);

        Transaction transaction = new Transaction(
                null,
                command.type(),
                command.amount(),
                occurredAt,
                command.description(),
                command.category(),
                command.ownerUserId(),
                command.destinationUserId()
        );

        try {
            AccountBalanceData accountBalanceData =
                    accountBalancePort.getByUserId(transaction.getOwnerUserId());

            if (transaction.getType() == TransactionType.DEPOSIT) {
                accountBalancePort.updateBalance(
                        transaction.getOwnerUserId(),
                        accountBalanceData.balance().add(transaction.getAmount())
                );

                return approveAndSave(
                        transaction,
                        accountBalanceData,
                        null
                );
            }

            if (accountBalanceData.balance().compareTo(transaction.getAmount()) < 0) {
                return rejectAndSave(transaction, "Saldo insuficiente para realizar a transação.");
            }

            if (accountBalanceData.transactionLimit().compareTo(transaction.getAmount()) < 0) {
                return rejectAndSave(transaction, "O valor da transação ultrapassa o limite por operação.");
            }

            BigDecimal dailyTotal =
                    transactionRepositoryPort.sumApprovedOutgoingByOwnerUserIdAndDate(
                            transaction.getOwnerUserId(),
                            occurredAt.toLocalDate()
                    );

            if (dailyTotal == null) {
                dailyTotal = BigDecimal.ZERO;
            }

            if (dailyTotal.add(transaction.getAmount())
                    .compareTo(accountBalanceData.dailyLimit()) > 0) {
                return rejectAndSave(transaction, "O valor da transação ultrapassa o limite diário.");
            }

            AccountBalanceData destinationAccount = null;

            if (transaction.getType() == TransactionType.TRANSFER) {
                destinationAccount = accountBalancePort.getByUserId(
                        transaction.getDestinationUserId()
                );
            }

            BigDecimal originalOwnerBalance = accountBalanceData.balance();

            accountBalancePort.updateBalance(
                    transaction.getOwnerUserId(),
                    originalOwnerBalance.subtract(transaction.getAmount())
            );

            if (destinationAccount != null) {
                try {
                    accountBalancePort.updateBalance(
                            transaction.getDestinationUserId(),
                            destinationAccount.balance().add(transaction.getAmount())
                    );
                } catch (AccountBalanceUnavailableException exception) {
                    restoreBalance(
                            transaction.getOwnerUserId(),
                            originalOwnerBalance,
                            exception
                    );
                    throw exception;
                }
            }

            return approveAndSave(
                    transaction,
                    accountBalanceData,
                    destinationAccount
            );

        } catch (AccountBalanceUnavailableException exception) {
            return rejectAndSave(
                    transaction,
                    "Não foi possível consultar o saldo e os limites do usuário."
            );
        }
    }

    @Override
    public Transaction findById(
            Long id,
            Long ownerUserId
    ) {
        return transactionRepositoryPort
                .findByIdAndOwnerUserId(id, ownerUserId)
                .orElseThrow(TransactionNotFoundException::new);
    }

    private Transaction rejectAndSave(Transaction transaction, String reason) {
        transaction.reject(reason);
        return transactionRepositoryPort.save(transaction);
    }

    private Transaction approveAndSave(
            Transaction transaction,
            AccountBalanceData ownerAccount,
            AccountBalanceData destinationAccount
    ) {
        transaction.approve();

        try {
            return transactionRepositoryPort.save(transaction);
        } catch (RuntimeException exception) {
            restoreBalance(
                    transaction.getOwnerUserId(),
                    ownerAccount.balance(),
                    exception
            );

            if (destinationAccount != null) {
                restoreBalance(
                        transaction.getDestinationUserId(),
                        destinationAccount.balance(),
                        exception
                );
            }

            throw exception;
        }
    }

    private void restoreBalance(
            Long userId,
            BigDecimal originalBalance,
            RuntimeException originalException
    ) {
        try {
            accountBalancePort.updateBalance(userId, originalBalance);
        } catch (AccountBalanceUnavailableException rollbackException) {
            originalException.addSuppressed(rollbackException);
        }
    }

    @Override
    public List<Transaction> findAll(
            Long ownerUserId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate
    ) {
        LocalDate effectiveStartDate =
                startDate != null
                        ? startDate
                        : LocalDate.of(1970, 1, 1);

        LocalDate effectiveEndDate =
                endDate != null
                        ? endDate
                        : LocalDate.now(clock);

        if (effectiveStartDate.isAfter(effectiveEndDate)) {
            throw new InvalidDateRangeException();
        }

        LocalDateTime start =
                effectiveStartDate.atStartOfDay();

        LocalDateTime end =
                effectiveEndDate
                        .plusDays(1)
                        .atStartOfDay();

        return transactionRepositoryPort
                .findAllByOwnerUserIdAndFilters(
                        ownerUserId,
                        type,
                        start,
                        end
                );
    }
}
