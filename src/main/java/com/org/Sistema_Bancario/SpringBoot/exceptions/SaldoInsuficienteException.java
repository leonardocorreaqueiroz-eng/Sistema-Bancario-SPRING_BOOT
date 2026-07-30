package com.org.Sistema_Bancario.SpringBoot.exceptions;

public class SaldoInsuficienteException extends ContaException {
    public SaldoInsuficienteException() {
        super("Saldo insuficiente");
    }
}
