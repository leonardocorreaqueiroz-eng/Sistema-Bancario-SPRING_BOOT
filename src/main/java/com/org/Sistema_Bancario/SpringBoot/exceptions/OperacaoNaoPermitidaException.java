package com.org.Sistema_Bancario.SpringBoot.exceptions;

public class OperacaoNaoPermitidaException extends MovimentacaoException {
    public OperacaoNaoPermitidaException(String message) {
        super(message);
    }
}
