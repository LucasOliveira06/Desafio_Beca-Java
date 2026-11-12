package br.com.lucas.ms2_transaction.adapter.controller;

import br.com.lucas.ms2_transaction.adapter.controller.dto.AccountBalanceResponse;
import br.com.lucas.ms2_transaction.adapter.exception.ApiErrorResponse;
import br.com.lucas.ms2_transaction.application.dto.AccountBalanceData;
import br.com.lucas.ms2_transaction.application.port.in.GetAccountBalanceUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@Tag(
        name = "Saldo",
        description = "Consulta de saldo e limites do usuário autenticado"
)
@RestController
@RequestMapping("/balance")
public class AccountBalanceController {

    private final GetAccountBalanceUseCase getAccountBalanceUseCase;

    public AccountBalanceController(
            GetAccountBalanceUseCase getAccountBalanceUseCase
    ) {
        this.getAccountBalanceUseCase = getAccountBalanceUseCase;
    }

    @Operation(
            summary = "Consultar saldo e limites",
            description = "Consulta na MockAPI o saldo e os limites do usuário autenticado"
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Saldo e limites consultados com sucesso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Credenciais inválidas"
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Não foi possível consultar a conta externa",
                    content = @Content(
                            schema = @Schema(
                                    implementation = ApiErrorResponse.class
                            )
                    )
            )
    })
    @GetMapping
    public ResponseEntity<AccountBalanceResponse> getBalance(
            Principal principal
    ) {
        Long userId = Long.valueOf(principal.getName());

        AccountBalanceData accountBalanceData =
                getAccountBalanceUseCase.getByUserId(userId);

        AccountBalanceResponse response = new AccountBalanceResponse(
                accountBalanceData.balance(),
                accountBalanceData.dailyLimit(),
                accountBalanceData.transactionLimit()
        );

        return ResponseEntity.ok(response);
    }
}
