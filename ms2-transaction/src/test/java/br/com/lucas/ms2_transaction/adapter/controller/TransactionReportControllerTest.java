package br.com.lucas.ms2_transaction.adapter.controller;

import br.com.lucas.ms2_transaction.application.port.in.GenerateTransactionReportUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TransactionReportControllerTest {

    private GenerateTransactionReportUseCase
            generateTransactionReportUseCase;

    private TransactionReportController transactionReportController;

    @BeforeEach
    void setUp() {
        generateTransactionReportUseCase =
                mock(GenerateTransactionReportUseCase.class);

        transactionReportController =
                new TransactionReportController(
                        generateTransactionReportUseCase
                );
    }

    @Test
    void deveRetornarRelatorioCsvDoUsuarioAutenticado() {
        LocalDate startDate = LocalDate.of(2026, 11, 1);
        LocalDate endDate = LocalDate.of(2026, 11, 30);
        Principal principal = () -> "12";

        byte[] report = "id;type"
                .getBytes(StandardCharsets.UTF_8);

        when(generateTransactionReportUseCase.generate(
                12L,
                startDate,
                endDate
        )).thenReturn(report);

        ResponseEntity<byte[]> response =
                transactionReportController.generate(
                        startDate,
                        endDate,
                        principal
                );

        assertEquals(
                HttpStatus.OK,
                response.getStatusCode()
        );

        assertEquals(
                MediaType.parseMediaType("text/csv"),
                response.getHeaders().getContentType()
        );

        assertEquals(
                "attachment; filename=transactions.csv",
                response.getHeaders().getFirst(
                        HttpHeaders.CONTENT_DISPOSITION
                )
        );

        assertArrayEquals(
                report,
                response.getBody()
        );

        verify(generateTransactionReportUseCase).generate(
                12L,
                startDate,
                endDate
        );
    }
}
