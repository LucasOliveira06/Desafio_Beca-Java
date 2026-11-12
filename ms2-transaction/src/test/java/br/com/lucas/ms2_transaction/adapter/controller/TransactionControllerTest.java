package br.com.lucas.ms2_transaction.adapter.controller;

import br.com.lucas.ms2_transaction.adapter.controller.dto.CreateTransactionRequest;
import br.com.lucas.ms2_transaction.adapter.controller.dto.TransactionResponse;
import br.com.lucas.ms2_transaction.adapter.controller.mapper.TransactionControllerMapper;
import br.com.lucas.ms2_transaction.application.dto.CreateTransactionCommand;
import br.com.lucas.ms2_transaction.application.port.in.CreateTransactionUseCase;
import br.com.lucas.ms2_transaction.domain.model.Transaction;
import br.com.lucas.ms2_transaction.domain.model.TransactionStatus;
import br.com.lucas.ms2_transaction.domain.model.TransactionType;
import br.com.lucas.ms2_transaction.application.port.in.FindTransactionByIdUseCase;
import br.com.lucas.ms2_transaction.application.port.in.ListTransactionsUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private CreateTransactionUseCase createTransactionUseCase;

    @Mock
    private FindTransactionByIdUseCase findTransactionByIdUseCase;

    @Mock
    private ListTransactionsUseCase listTransactionsUseCase;

    private TransactionController transactionController;

    @BeforeEach
    void setUp() {
        TransactionControllerMapper mapper =
                new TransactionControllerMapper();

        transactionController = new TransactionController(
                createTransactionUseCase,
                findTransactionByIdUseCase,
                listTransactionsUseCase,
                mapper
        );
    }

    @Test
    void deveCriarTransacaoParaUsuarioAutenticado() {
        LocalDateTime occurredAt =
                LocalDateTime.of(2026, 11, 11, 10, 30);

        CreateTransactionRequest request =
                new CreateTransactionRequest(
                        TransactionType.DEPOSIT,
                        new BigDecimal("100.00"),
                        "Depósito mensal",
                        null,
                        null
                );

        Transaction transaction = new Transaction(
                1L,
                TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                occurredAt,
                "Depósito mensal",
                null,
                1L,
                null
        );

        transaction.approve();

        when(createTransactionUseCase.create(any(
                CreateTransactionCommand.class
        ))).thenReturn(transaction);

        Principal principal = () -> "1";

        ResponseEntity<TransactionResponse> response =
                transactionController.create(request, principal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().ownerUserId());
        assertEquals(
                TransactionStatus.APPROVED,
                response.getBody().status()
        );

        ArgumentCaptor<CreateTransactionCommand> commandCaptor =
                ArgumentCaptor.forClass(CreateTransactionCommand.class);

        verify(createTransactionUseCase).create(commandCaptor.capture());

        assertEquals(
                1L,
                commandCaptor.getValue().ownerUserId()
        );
    }

    @Test
    void deveBuscarTransacaoDoUsuarioAutenticadoPorId() {
        LocalDateTime occurredAt =
                LocalDateTime.of(2026, 11, 11, 15, 45);

        Transaction transaction = new Transaction(
                7L,
                TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                occurredAt,
                "Depósito de teste",
                null,
                12L,
                null
        );

        transaction.approve();

        when(findTransactionByIdUseCase.findById(7L, 12L))
                .thenReturn(transaction);

        Principal principal = () -> "12";

        ResponseEntity<TransactionResponse> response =
                transactionController.findById(7L, principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(7L, response.getBody().id());
        assertEquals(12L, response.getBody().ownerUserId());
        assertEquals(
                TransactionStatus.APPROVED,
                response.getBody().status()
        );

        verify(findTransactionByIdUseCase)
                .findById(7L, 12L);
    }

    @Test
    void deveListarTransacoesDoUsuarioAutenticadoComFiltros() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);

        Transaction transaction = new Transaction(
                8L,
                TransactionType.DEPOSIT,
                new BigDecimal("150.00"),
                LocalDateTime.of(2026, 11, 12, 10, 0),
                "Depósito de teste",
                null,
                12L,
                null
        );

        transaction.approve();

        when(listTransactionsUseCase.findAll(
                12L,
                TransactionType.DEPOSIT,
                startDate,
                endDate
        )).thenReturn(List.of(transaction));

        Principal principal = () -> "12";

        ResponseEntity<List<TransactionResponse>> response =
                transactionController.findAll(
                        TransactionType.DEPOSIT,
                        startDate,
                        endDate,
                        principal
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(8L, response.getBody().getFirst().id());
        assertEquals(12L, response.getBody().getFirst().ownerUserId());

        verify(listTransactionsUseCase).findAll(
                12L,
                TransactionType.DEPOSIT,
                startDate,
                endDate
        );
    }
}
