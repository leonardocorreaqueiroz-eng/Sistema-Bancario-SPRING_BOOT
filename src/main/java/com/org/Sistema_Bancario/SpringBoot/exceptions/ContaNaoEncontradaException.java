package com.org.Sistema_Bancario.SpringBoot.exceptions;

public class ContaNaoEncontradaException extends ContaException {
    public ContaNaoEncontradaException() {
        super("Conta não encontrada!");
    }
}
