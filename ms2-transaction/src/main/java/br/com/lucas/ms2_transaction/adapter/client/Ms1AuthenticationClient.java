package br.com.lucas.ms2_transaction.adapter.client;

import br.com.lucas.ms2_transaction.adapter.client.dto.Ms1UserResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Component
public class Ms1AuthenticationClient {

    private final RestClient restClient;

    public Ms1AuthenticationClient(
            @Value("${ms1.base-url}") String ms1BaseUrl
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(ms1BaseUrl)
                .build();
    }

    public Optional<Ms1UserResponse> authenticate(
            String email,
            String password
    ) {
        try {
            List<Ms1UserResponse> users = restClient
                    .get()
                    .uri("/users")
                    .headers(headers -> headers.setBasicAuth(
                            email,
                            password,
                            StandardCharsets.UTF_8
                    ))
                    .retrieve()
                    .body(new ParameterizedTypeReference<
                            List<Ms1UserResponse>
                            >() {
                    });

            if (users == null || users.isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(users.getFirst());

        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == 401
                    || exception.getStatusCode().value() == 403) {
                return Optional.empty();
            }

            throw exception;
        }
    }
}