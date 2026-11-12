package br.com.lucas.ms2_transaction.adapter.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record BrasilApiQuoteResponse(
        @JsonProperty("cotacao_venda")
        BigDecimal sellRate,

        @JsonProperty("data_hora_cotacao")
        String quotedAt
) {
}