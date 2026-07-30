package com.org.Sistema_Bancario.SpringBoot.exceptions;

public class ExtratoVazioException extends MovimentacaoException {
    public ExtratoVazioException() {
        super("Extrato vazio");
    }
}
