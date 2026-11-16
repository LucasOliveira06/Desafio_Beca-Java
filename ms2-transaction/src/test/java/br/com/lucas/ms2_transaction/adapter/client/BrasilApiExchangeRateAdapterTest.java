package br.com.lucas.ms2_transaction.adapter.client;

import br.com.lucas.ms2_transaction.application.dto.ExchangeRateData;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withBadRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class BrasilApiExchangeRateAdapterTest {

    @Test
    void deveUsarUltimoDiaDisponivelQuandoDataSolicitadaNaoTemCotacao() {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://brasilapi.com.br");
        MockRestServiceServer server = MockRestServiceServer
                .bindTo(builder)
                .build();
        BrasilApiExchangeRateAdapter adapter =
                new BrasilApiExchangeRateAdapter(builder.build());

        server.expect(once(), requestTo(
                        "https://brasilapi.com.br/api/cambio/v1/cotacao/USD/2026-11-12"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest());

        server.expect(once(), requestTo(
                        "https://brasilapi.com.br/api/cambio/v1/cotacao/USD/2026-11-11"
                ))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(
                        """
                                {
                                  "cotacoes": [
                                    {
                                      "cotacao_venda": 5.5600,
                                      "data_hora_cotacao": "2026-11-11 10:04:12.543029"
                                    }
                                  ],
                                  "moeda": "USD",
                                  "data": "2026-11-11"
                                }
                                """,
                        MediaType.APPLICATION_JSON
                ));

        Optional<ExchangeRateData> result = adapter.findRate(
                "USD",
                LocalDate.of(2026, 11, 12)
        );

        assertTrue(result.isPresent());
        assertEquals("USD", result.get().currency());
        assertEquals(new BigDecimal("5.5600"), result.get().rate());
        assertEquals(
                LocalDate.of(2026, 11, 11),
                result.get().rateDate()
        );
        server.verify();
    }
}
