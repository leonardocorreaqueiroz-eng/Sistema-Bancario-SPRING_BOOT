package com.org.Sistema_Bancario.SpringBoot.exceptions;

import java.math.BigDecimal;

public class ValorInvalidoException extends AplicacaoException {
    public ValorInvalidoException(BigDecimal valor) {
        super("Valor inválido" + valor);
    }
}
