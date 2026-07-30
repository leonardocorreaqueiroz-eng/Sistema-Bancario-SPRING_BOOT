package com.org.Sistema_Bancario.SpringBoot.exceptions;

public class AplicacaoException extends RuntimeException {
    public AplicacaoException(String message) {
        super(message);
    }
    public AplicacaoException(){
        super("O valor excedeu o total das aplicações!");
    }
}
