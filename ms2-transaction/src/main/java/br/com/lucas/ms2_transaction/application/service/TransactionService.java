package br.com.lucas.ms2_transaction.application.service;

import br.com.lucas.ms2_transaction.application.dto.AccountBalanceData;
import br.com.lucas.ms2_transaction.application.dto.CreateTransactionCommand;
import br.com.lucas.ms2_transaction.application.exception.AccountBalanceUnavailableException;
import br.com.lucas.ms2_transaction.application.port.in.CreateTransactionUseCase;
import br.com.lucas.ms2_transaction.application.port.out.AccountBalancePort;
import br.com.lucas.ms2_transaction.application.port.out.TransactionRepositoryPort;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import br.com.lucas.ms2_transaction.domain.model.TransactionType;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionService implements CreateTransactionUseCase {

    private final TransactionRepositoryPort transactionRepositoryPort;
    private final AccountBalancePort accountBalancePort;

    public TransactionService(
            TransactionRepositoryPort transactionRepositoryPort,
            AccountBalancePort accountBalancePort
    ) {
        this.transactionRepositoryPort = transactionRepositoryPort;
        this.accountBalancePort = accountBalancePort;
    }

    @Override
    public Transaction create(CreateTransactionCommand command) {
        Transaction transaction = new Transaction(
                null,
                command.type(),
                command.amount(),
                command.occurredAt(),
                command.description(),
                command.category(),
                command.ownerUserId(),
                command.destinationUserId()
        );

        if (transaction.getType() == TransactionType.DEPOSIT) {
            transaction.approve();
            return transactionRepositoryPort.save(transaction);
        }

        try {
            AccountBalanceData accountBalanceData =
                    accountBalancePort.getByUserId(transaction.getOwnerUserId());

            if (accountBalanceData.balance().compareTo(transaction.getAmount()) < 0) {
                return rejectAndSave(transaction, "Saldo insuficiente para realizar a transação.");
            }

            if (accountBalanceData.transactionLimit().compareTo(transaction.getAmount()) < 0) {
                return rejectAndSave(transaction, "O valor da transação ultrapassa o limite por operação.");
            }

            BigDecimal dailyTotal =
                    transactionRepositoryPort.sumApprovedOutgoingByOwnerUserIdAndDate(
                            transaction.getOwnerUserId(),
                            transaction.getOccurredAt().toLocalDate()
                    );

            if (dailyTotal == null) {
                dailyTotal = BigDecimal.ZERO;
            }

            if (dailyTotal.add(transaction.getAmount())
                    .compareTo(accountBalanceData.dailyLimit()) > 0) {
                return rejectAndSave(transaction, "O valor da transação ultrapassa o limite diário.");
            }

            transaction.approve();
            return transactionRepositoryPort.save(transaction);

        } catch (AccountBalanceUnavailableException exception) {
            return rejectAndSave(
                    transaction,
                    "Não foi possível consultar o saldo e os limites do usuário."
            );
        }
    }

    private Transaction rejectAndSave(Transaction transaction, String reason) {
        transaction.reject(reason);
        return transactionRepositoryPort.save(transaction);
    }
}