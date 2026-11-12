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
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionServiceTest {

    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-11-12T15:00:00Z"),
            ZoneId.of("America/Sao_Paulo")
    );

    private FakeTransactionRepository transactionRepository;
    private FakeAccountBalancePort accountBalancePort;
    private TransactionService transactionService;

    @BeforeEach
    void setUp() {
        transactionRepository = new FakeTransactionRepository();
        accountBalancePort = new FakeAccountBalancePort();

        transactionService = new TransactionService(
                transactionRepository,
                accountBalancePort,
                FIXED_CLOCK
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
    void deveAprovarDepositoEAtualizarSaldo() {
        CreateTransactionCommand command = createCommand(
                TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                null
        );

        Transaction transaction = transactionService.create(command);

        assertEquals(TransactionStatus.APPROVED, transaction.getStatus());
        assertEquals(
                new BigDecimal("1100.00"),
                accountBalancePort.updatedBalances.get(1L)
        );
        assertEquals(
                LocalDateTime.of(2026, 11, 12, 12, 0),
                transaction.getOccurredAt()
        );
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
        assertEquals(
                new BigDecimal("900.00"),
                accountBalancePort.updatedBalances.get(1L)
        );
        assertEquals(LocalDate.of(2026, 11, 12), transactionRepository.queriedDate);
    }

    @Test
    void deveDebitarOrigemECreditarDestinoNaTransferencia() {
        CreateTransactionCommand command = createCommand(
                TransactionType.TRANSFER,
                new BigDecimal("100.00"),
                null,
                2L
        );

        Transaction transaction = transactionService.create(command);

        assertEquals(TransactionStatus.APPROVED, transaction.getStatus());
        assertEquals(
                new BigDecimal("900.00"),
                accountBalancePort.updatedBalances.get(1L)
        );
        assertEquals(
                new BigDecimal("400.00"),
                accountBalancePort.updatedBalances.get(2L)
        );
    }

    @Test
    void deveRestaurarSaldoDaOrigemQuandoCreditoDoDestinoFalhar() {
        accountBalancePort.unavailableOnUpdateUserId = 2L;

        CreateTransactionCommand command = createCommand(
                TransactionType.TRANSFER,
                new BigDecimal("100.00"),
                null,
                2L
        );

        Transaction transaction = transactionService.create(command);

        assertEquals(TransactionStatus.REJECTED, transaction.getStatus());
        assertEquals(
                new BigDecimal("1000.00"),
                accountBalancePort.updatedBalances.get(1L)
        );
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
        return createCommand(type, amount, category, null);
    }

    private CreateTransactionCommand createCommand(
            TransactionType type,
            BigDecimal amount,
            String category,
            Long destinationUserId
    ) {
        return new CreateTransactionCommand(
                type,
                amount,
                "Transação de teste",
                category,
                1L,
                destinationUserId
        );
    }

    private static class FakeTransactionRepository implements TransactionRepositoryPort {

        private Transaction savedTransaction;
        private BigDecimal dailyTotal = BigDecimal.ZERO;
        private Transaction transactionToFind;
        private LocalDate queriedDate;

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
            queriedDate = date;
            return dailyTotal;
        }
    }

    private static class FakeAccountBalancePort implements AccountBalancePort {

        private AccountBalanceData accountBalanceData = new AccountBalanceData(
                new BigDecimal("1000.00"),
                new BigDecimal("2000.00"),
                new BigDecimal("500.00")
        );

        private AccountBalanceData destinationAccountBalanceData = new AccountBalanceData(
                new BigDecimal("300.00"),
                new BigDecimal("2000.00"),
                new BigDecimal("500.00")
        );

        private boolean unavailable;
        private Long unavailableOnUpdateUserId;
        private final Map<Long, BigDecimal> updatedBalances = new HashMap<>();

        @Override
        public AccountBalanceData getByUserId(Long userId) {
            if (unavailable) {
                throw new AccountBalanceUnavailableException(
                        "MockAPI indisponível"
                );
            }

            if (userId.equals(2L)) {
                return destinationAccountBalanceData;
            }

            return accountBalanceData;
        }

        @Override
        public void updateBalance(Long userId, BigDecimal balance) {
            if (unavailable || userId.equals(unavailableOnUpdateUserId)) {
                throw new AccountBalanceUnavailableException(
                        "MockAPI indisponível"
                );
            }

            updatedBalances.put(userId, balance);

            if (userId.equals(2L)) {
                destinationAccountBalanceData = new AccountBalanceData(
                        balance,
                        destinationAccountBalanceData.dailyLimit(),
                        destinationAccountBalanceData.transactionLimit()
                );
                return;
            }

            accountBalanceData = new AccountBalanceData(
                    balance,
                    accountBalanceData.dailyLimit(),
                    accountBalanceData.transactionLimit()
            );
        }
    }
}
