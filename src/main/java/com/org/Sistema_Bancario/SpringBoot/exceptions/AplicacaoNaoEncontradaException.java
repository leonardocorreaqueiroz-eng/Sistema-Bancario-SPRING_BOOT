package com.org.Sistema_Bancario.SpringBoot.exceptions;

public class AplicacaoNaoEncontradaException extends MovimentacaoException {
    public AplicacaoNaoEncontradaException() {
        super("Aplicações não encontradas!");
    }
    public AplicacaoNaoEncontradaException(String message) {
        super(message);
    }
}
