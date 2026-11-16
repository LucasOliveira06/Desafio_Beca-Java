package br.com.lucas.ms2_transaction.adapter.controller.dto;

import br.com.lucas.ms2_transaction.domain.model.TransactionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.OptBoolean;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record CreateTransactionRequest(

        @NotNull(message = "O tipo da transação é obrigatório")
        TransactionType type,

        @NotNull(message = "O valor da transação é obrigatório")
        @DecimalMin(
                value = "0.01",
                message = "O valor da transação deve ser maior que zero"
        )
        @Digits(
                integer = 17,
                fraction = 2,
                message = "O valor deve possuir no máximo duas casas decimais"
        )
        BigDecimal amount,

        @NotBlank(message = "A descrição da transação é obrigatória")
        @Size(
                max = 255,
                message = "A descrição deve possuir no máximo 255 caracteres"
        )
        String description,

        @JsonProperty(isRequired = OptBoolean.FALSE)
        @Size(
                max = 100,
                message = "A categoria deve possuir no máximo 100 caracteres"
        )
        String category,

        @JsonProperty(isRequired = OptBoolean.FALSE)
        @Positive(message = "O usuário de destino deve possuir um identificador válido")
        Long destinationUserId
) {
}
