package com.org.Sistema_Bancario.SpringBoot.dto;

import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;

import java.math.BigDecimal;

public record TransferenciaRequest (String destino,
                                    BigDecimal valor,
                                    TipoMovimentacao tipo) {
}
