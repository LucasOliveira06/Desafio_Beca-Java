package br.com.lucas.ms2_transaction.adapter.client;

import br.com.lucas.ms2_transaction.adapter.client.dto.MockApiAccountResponse;
import br.com.lucas.ms2_transaction.application.dto.AccountBalanceData;
import br.com.lucas.ms2_transaction.application.exception.AccountBalanceUnavailableException;
import br.com.lucas.ms2_transaction.application.port.out.AccountBalancePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class MockApiAccountBalanceAdapter implements AccountBalancePort {

    private final RestClient restClient;

    public MockApiAccountBalanceAdapter(
            @Value("${mock-api.account-url}") String accountUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(accountUrl)
                .build();
    }

    @Override
    public AccountBalanceData getByUserId(Long userId) {
        try {
            List<MockApiAccountResponse> accounts = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .queryParam("userId", userId)
                            .build())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });

            if (accounts == null || accounts.isEmpty()) {
                throw new AccountBalanceUnavailableException(
                        "Conta não encontrada na MockAPI."
                );
            }

            MockApiAccountResponse account = accounts.getFirst();

            return new AccountBalanceData(
                    account.balance(),
                    account.dailyLimit(),
                    account.transactionLimit()
            );

        } catch (RestClientException exception) {
            throw new AccountBalanceUnavailableException(
                    "Não foi possível consultar a MockAPI.",
                    exception
            );
        }
    }
}