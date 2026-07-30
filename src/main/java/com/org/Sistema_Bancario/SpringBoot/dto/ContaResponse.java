package com.org.Sistema_Bancario.SpringBoot.dto;

import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;

import java.math.BigDecimal;

public record ContaResponse (
        Integer numero,
        BigDecimal saldo,
        TipoConta tipo
) {
}
