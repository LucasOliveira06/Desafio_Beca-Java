package br.com.lucas.ms2_transaction.adapter.client;

import br.com.lucas.ms2_transaction.adapter.client.dto.MockApiAccountResponse;
import br.com.lucas.ms2_transaction.adapter.client.dto.MockApiBalanceUpdateRequest;
import br.com.lucas.ms2_transaction.application.dto.AccountBalanceData;
import br.com.lucas.ms2_transaction.application.exception.AccountBalanceUnavailableException;
import br.com.lucas.ms2_transaction.application.port.out.AccountBalancePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;

@Component
public class MockApiAccountBalanceAdapter implements AccountBalancePort {

    private final RestClient restClient;

    public MockApiAccountBalanceAdapter(
            @Value("${mock-api.account-url}") String accountUrl,
            ClientHttpRequestFactory requestFactory
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(accountUrl)
                .requestFactory(requestFactory)
                .build();
    }

    @Override
    public AccountBalanceData getByUserId(Long userId) {
        MockApiAccountResponse account = findAccountByUserId(userId);

        return new AccountBalanceData(
                account.balance(),
                account.dailyLimit(),
                account.transactionLimit()
        );
    }

    @Override
    public void updateBalance(Long userId, BigDecimal balance) {
        MockApiAccountResponse account = findAccountByUserId(userId);

        try {
            restClient.patch()
                    .uri("/{id}", account.id())
                    .body(new MockApiBalanceUpdateRequest(balance))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new AccountBalanceUnavailableException(
                    "Não foi possível atualizar o saldo na MockAPI.",
                    exception
            );
        }
    }

    private MockApiAccountResponse findAccountByUserId(Long userId) {
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

            return accounts.getFirst();
        } catch (RestClientException exception) {
            throw new AccountBalanceUnavailableException(
                    "Não foi possível consultar a MockAPI.",
                    exception
            );
        }
    }
}
