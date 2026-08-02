package com.org.Sistema_Bancario.SpringBoot.unit.util;

import com.org.Sistema_Bancario.SpringBoot.util.CPFValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CPFValidatorTest {
    @InjectMocks
    private CPFValidator cpfValidator;
    @Mock
    private ConstraintValidatorContext context;

    @Test
    void validarCpfDeveLancarCpfInvalidoExceptionPeloTamanho() {
        assertFalse(CPFValidator.validarCpf("123"));
    }
    @Test
    void validarCpfDeveLancarCpfInvalidoExceptionPorConterApenasNumerosRepetidos() {
        assertFalse(CPFValidator.validarCpf("11111111111"));
    }
    @Test
    void validarCpfDeveLancarCpfInvalidoExceptionPorNaoSerValido() {
        assertFalse(CPFValidator.validarCpf("529.982.247-25"));
    }
    @Test
    void validarCpfDeveRealizarValidacaoNaoLancandoCpfException() {
        assertTrue(CPFValidator.validarCpf("12345678909"));

    }

    @Test
    void deveRetornarTrueQuandoCpfForValido() {

        assertTrue(cpfValidator.isValid("12345678909", null));
    }

    @Test
    void deveRetornarFalseQuandoCpfForInvalido() {
        assertFalse(cpfValidator.isValid("52998224725", null));
    }

    @Test
    void deveRetornarFalseQuandoCpfForNull() {
        assertFalse(cpfValidator.isValid(null, null));
    }
}