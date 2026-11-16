package br.com.lucas.ms2_transaction.adapter.client;

import br.com.lucas.ms2_transaction.adapter.client.dto.BrasilApiExchangeResponse;
import br.com.lucas.ms2_transaction.adapter.client.dto.BrasilApiQuoteResponse;
import br.com.lucas.ms2_transaction.application.dto.ExchangeRateData;
import br.com.lucas.ms2_transaction.application.port.out.ExchangeRatePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.Optional;

@Component
public class BrasilApiExchangeRateAdapter implements ExchangeRatePort {

    private static final int MAX_FALLBACK_DAYS = 7;

    private final RestClient restClient;

    @Autowired
    public BrasilApiExchangeRateAdapter(
            @Value("${brasil-api.base-url}") String baseUrl,
            ClientHttpRequestFactory requestFactory
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .build();
    }

    BrasilApiExchangeRateAdapter(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<ExchangeRateData> findRate(
            String currency,
            LocalDate date
    ) {
        for (int daysBack = 0; daysBack <= MAX_FALLBACK_DAYS; daysBack++) {
            Optional<ExchangeRateData> exchangeRate = findRateForDate(
                    currency,
                    date.minusDays(daysBack)
            );

            if (exchangeRate.isPresent()) {
                return exchangeRate;
            }
        }

        return Optional.empty();
    }

    private Optional<ExchangeRateData> findRateForDate(
            String currency,
            LocalDate date
    ) {
        try {
            BrasilApiExchangeResponse response = restClient
                    .get()
                    .uri(
                            "/api/cambio/v1/cotacao/{currency}/{date}",
                            currency,
                            date
                    )
                    .retrieve()
                    .body(BrasilApiExchangeResponse.class);

            if (response == null
                    || response.quotes() == null
                    || response.quotes().isEmpty()) {
                return Optional.empty();
            }

            return response.quotes()
                    .stream()
                    .filter(quote ->
                            quote.sellRate() != null
                                    && quote.quotedAt() != null
                    )
                    .max(Comparator.comparing(
                            BrasilApiQuoteResponse::quotedAt
                    ))
                    .map(quote -> new ExchangeRateData(
                            response.currency(),
                            quote.sellRate(),
                            response.rateDate()
                    ));

        } catch (RestClientException exception) {
            return Optional.empty();
        }
    }
}
