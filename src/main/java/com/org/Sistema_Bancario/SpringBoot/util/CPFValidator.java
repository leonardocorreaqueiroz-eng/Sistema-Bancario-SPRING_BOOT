package com.org.Sistema_Bancario.SpringBoot.util;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CPFValidator implements ConstraintValidator<CPF, String> {

    public static boolean validarCpf(String cpf) {
        String valor = cpf.replaceAll("\\D", "");

        if (valor.length() != 11){
            return false;
        }
        if (valor.matches("^(\\d)\\1{10}$"))
            return false;

        int base = Integer.parseInt(valor.substring(0, 9));

        int d1 = calculoCpf(base);
        int d2 = calculoCpf(base * 10 + d1);

        return valor.substring(9).equals("" + d1 + d2);
    }

    private static int calculoCpf(int num) {
        int soma = 0;

        for (int i = 2; i < 11; i++) {
            soma += (num % 10) * i;
            num /= 10;
        }

        int resto = soma % 11;
        return (resto > 1) ? (11 - resto) : 0;
    }

    @Override
    public boolean isValid(String cpf, ConstraintValidatorContext constraintValidatorContext) {
        if (cpf == null)
            return false;

        return validarCpf(cpf);
    }
}