package com.org.Sistema_Bancario.SpringBoot.dto;

import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferenciaRequest (
        @NotBlank(message = "Defina o destino")
        String destino,
        @NotNull
        @DecimalMin(value = "0.01", message = "O valor deve ser maior que zero")
        BigDecimal valor,
        @NotNull(message = "Defina o tipo")
        TipoMovimentacao tipo) {
}
