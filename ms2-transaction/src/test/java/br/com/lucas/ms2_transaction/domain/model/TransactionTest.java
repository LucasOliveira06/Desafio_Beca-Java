package br.com.lucas.ms2_transaction.domain.model;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TransactionTest {

    @Test
    void deveAprovarTransacao() {

        Transaction transaction = validDeposit();

        transaction.approve();

        assertEquals(
                TransactionStatus.APPROVED,
                transaction.getStatus()
        );

        assertNull(transaction.getRejectionReason());
    }

    @Test
    void deveRejeitarTransacaoComMotivo() {

        Transaction transaction = validDeposit();

        transaction.reject("Saldo insuficiente");

        assertEquals(
                TransactionStatus.REJECTED,
                transaction.getStatus()
        );

        assertEquals(
                "Saldo insuficiente",
                transaction.getRejectionReason()
        );
    }

    @Test
    void deveRejeitarValorIgualAZero() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction(
                        null,
                        TransactionType.DEPOSIT,
                        BigDecimal.ZERO,
                        LocalDateTime.now(),
                        "Depósito mensal",
                        null,
                        1L,
                        null
                )
        );

        assertEquals(
                "O valor da transação deve ser maior que zero",
                exception.getMessage()
        );
    }

    @Test
    void deveExigirDestinoParaTransferencia() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction(
                        null,
                        TransactionType.TRANSFER,
                        new BigDecimal("100.00"),
                        LocalDateTime.now(),
                        "Transferência para amigo",
                        null,
                        1L,
                        null
                )
        );

        assertEquals(
                "A transferência deve informar o usuário de destino",
                exception.getMessage()
        );
    }

    @Test
    void deveExigirCategoriaParaCompra() {

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new Transaction(
                        null,
                        TransactionType.PURCHASE,
                        new BigDecimal("50.00"),
                        LocalDateTime.now(),
                        "Compra no mercado",
                        null,
                        1L,
                        null
                )
        );

        assertEquals(
                "A compra deve informar uma categoria",
                exception.getMessage()
        );
    }

    private Transaction validDeposit() {
        return new Transaction(
                null,
                TransactionType.DEPOSIT,
                new BigDecimal("100.00"),
                LocalDateTime.now(),
                "Depósito mensal",
                null,
                1L,
                null
        );
    }
}
