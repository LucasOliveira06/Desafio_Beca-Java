package br.com.lucas.ms2_transaction.adapter.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;
import java.util.List;

public record BrasilApiExchangeResponse(
        @JsonProperty("cotacoes")
        List<BrasilApiQuoteResponse> quotes,

        @JsonProperty("moeda")
        String currency,

        @JsonProperty("data")
        LocalDate rateDate
) {
}