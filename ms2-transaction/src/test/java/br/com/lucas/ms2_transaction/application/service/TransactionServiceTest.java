package br.com.lucas.ms2_transaction.application.service;

import br.com.lucas.ms2_transaction.application.dto.AccountBalanceData;
import br.com.lucas.ms2_transaction.application.dto.CreateTransactionCommand;
import br.com.lucas.ms2_transaction.application.exception.AccountBalanceUnavailableException;
import br.com.lucas.ms2_transaction.application.port.out.AccountBalancePort;
import br.com.lucas.ms2_transaction.application.port.out.TransactionRepositoryPort;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import br.com.lucas.ms2_transaction.domain.model.TransactionStatus;
import br.com.lucas.ms2_transaction.domain.model.TransactionType;
import br.com.lucas.ms2_transaction.application.exception.TransactionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionServiceTest {

    private FakeTransactionRepository transactionRepository;
    private FakeAccountBalancePort accountBalancePort;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionRepository = new FakeTransactionRepository();
        accountBalancePort = new FakeAccountBalancePort();

        transactionService = new TransactionService(
                transactionRepository,
                accountBalancePort
        );
    }

    @Test
    void deveBuscarTransacaoQuandoPertencerAoUsuario() {
        Transaction transaction = new Transaction(
                10L,
                TransactionType.DEPOSIT,
                new BigDecimal("200.00"),
                LocalDateTime.of(2026, 11, 11, 16, 0),
                "Depósito de teste",
                null,
                1L,
                null
        );

        transaction.approve();
        transactionRepository.transactionToFind = transaction;

        Transaction result = transactionService.findById(10L, 1L);

        assertEquals(10L, result.getId());
        assertEquals(1L, result.getOwnerUserId());
        assertEquals(TransactionStatus.APPROVED, result.getStatus());
    }

    @Test
    void deveLancarExcecaoQuandoTransacaoNaoPertencerAoUsuario() {
        Transaction transaction = new Transaction(
                10L,
                TransactionType.DEPOSIT,
                new BigDecimal("200.00"),
                LocalDateTime.of(2026, 11, 11, 16, 0),
                "Depósito de teste",
                null,
                1L,
                null
        );

        transaction.approve();
        transactionRepository.transactionToFind = transaction;

        assertThrows(
                TransactionNotFoundException.class,
                () -> transactionService.findById(10L, 2L)
        );
    }

    @Test
    void deveAprovarDepositoSemConsultarSaldo() {
        CreateTransactionCommand command = createCommand(
                TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                null
        );

        Transaction transaction = transactionService.create(command);

        assertEquals(TransactionStatus.APPROVED, transaction.getStatus());
        assertFalse(accountBalancePort.wasCalled);
    }

    @Test
    void deveAprovarSaqueComSaldoELimitesDisponiveis() {
        CreateTransactionCommand command = createCommand(
                TransactionType.WITHDRAWAL,
                new BigDecimal("100.00"),
                null
        );

        Transaction transaction = transactionService.create(command);

        assertEquals(TransactionStatus.APPROVED, transaction.getStatus());
    }

    @Test
    void deveRejeitarQuandoSaldoForInsuficiente() {
        accountBalancePort.accountBalanceData = new AccountBalanceData(
                new BigDecimal("50.00"),
                new BigDecimal("1000.00"),
                new BigDecimal("500.00")
        );

        CreateTransactionCommand command = createCommand(
                TransactionType.WITHDRAWAL,
                new BigDecimal("100.00"),
                null
        );

        Transaction transaction = transactionService.create(command);

        assertEquals(TransactionStatus.REJECTED, transaction.getStatus());
        assertEquals(
                "Saldo insuficiente para realizar a transação.",
                transaction.getRejectionReason()
        );
    }

    @Test
    void deveRejeitarQuandoMockApiEstiverIndisponivel() {
        accountBalancePort.unavailable = true;

        CreateTransactionCommand command = createCommand(
                TransactionType.PURCHASE,
                new BigDecimal("100.00"),
                "Alimentação"
        );

        Transaction transaction = transactionService.create(command);

        assertEquals(TransactionStatus.REJECTED, transaction.getStatus());
        assertEquals(
                "Não foi possível consultar o saldo e os limites do usuário.",
                transaction.getRejectionReason()
        );
    }

    private CreateTransactionCommand createCommand(
            TransactionType type,
            BigDecimal amount,
            String category
    ) {
        return new CreateTransactionCommand(
                type,
                amount,
                LocalDateTime.now(),
                "Transação de teste",
                category,
                1L,
                null
        );
    }

    private static class FakeTransactionRepository implements TransactionRepositoryPort {

        private Transaction savedTransaction;
        private BigDecimal dailyTotal = BigDecimal.ZERO;
        private Transaction transactionToFind;

        @Override
        public Transaction save(Transaction transaction) {
            savedTransaction = transaction;
            return transaction;
        }

        @Override
        public Optional<Transaction> findByIdAndOwnerUserId(
                Long id,
                Long ownerUserId
        ) {
            if (transactionToFind == null) {
                return Optional.empty();
            }

            boolean sameTransaction =
                    transactionToFind.getId().equals(id);

            boolean sameOwner =
                    transactionToFind.getOwnerUserId().equals(ownerUserId);

            if (sameTransaction && sameOwner) {
                return Optional.of(transactionToFind);
            }

            return Optional.empty();
        }

        @Override
        public BigDecimal sumApprovedOutgoingByOwnerUserIdAndDate(
                Long ownerUserId,
                LocalDate date
        ) {
            return dailyTotal;
        }
    }

    private static class FakeAccountBalancePort implements AccountBalancePort {

        private AccountBalanceData accountBalanceData = new AccountBalanceData(
                new BigDecimal("1000.00"),
                new BigDecimal("2000.00"),
                new BigDecimal("500.00")
        );

        private boolean unavailable;
        private boolean wasCalled;

        @Override
        public AccountBalanceData getByUserId(Long userId) {
            wasCalled = true;

            if (unavailable) {
                throw new AccountBalanceUnavailableException(
                        "MockAPI indisponível"
                );
            }

            return accountBalanceData;
        }
    }
}