package com.org.Sistema_Bancario.SpringBoot.model;

import java.math.BigDecimal;

public final class RegrasDeBanco {
    public static final BigDecimal TAXA_SAQUE = new BigDecimal("0.02");
    public static final BigDecimal TAXA_INVESTIMENTO = new BigDecimal("0.00042");
    public static final BigDecimal LIMITE_DOC = new BigDecimal("4999");
}
