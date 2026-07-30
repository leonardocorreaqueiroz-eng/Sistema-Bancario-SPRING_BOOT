package com.org.Sistema_Bancario.SpringBoot.unit.model;

import com.org.Sistema_Bancario.SpringBoot.exceptions.SaldoInsuficienteException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ValorInvalidoException;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class ContaTest {

    private Conta conta;

    @BeforeEach
    void setUp(){
        Cliente cliente =
                new Cliente("Patrício Silva Rodriguete",
                        "12345678909",
                        "192292");
        conta = new Conta(cliente,
                TipoConta.CORRENTE,
                LocalDate.now());
    }

    @Test
    void deveSacarValorQuandoSaldoForSuficiente(){
        conta.depositar(new BigDecimal("300.98"));
        var valor = new BigDecimal("290.36")
                .setScale(2, RoundingMode.HALF_EVEN);
        var resultado = conta.getSaldo()
                .subtract(valor
                        .add(valor
                        .multiply(RegrasDeBanco.TAXA_SAQUE)))
                .setScale(2, RoundingMode.HALF_EVEN);
        conta.sacar(valor);
        assertEquals(resultado,conta.getSaldo());
    }

    @Test
    void deveLancarExcecaoQuandoSacarValorInvalido() {
        assertThrows(ValorInvalidoException.class,
                () -> conta.sacar(new BigDecimal(-80)));
    }
    @Test
    void deveLancarExcecaoQuandoSaldoForInsuficiente() {
        assertThrows(SaldoInsuficienteException.class,
                () -> conta.sacar(new BigDecimal(10000)));
    }

    @Test
    void deveDepositarValorNaConta(){
        var valor = new BigDecimal("390.36");
        conta.depositar(valor);
        assertEquals(0, conta.getSaldo().compareTo(valor));
    }

    @Test
    void deveLancarExcecaoQuandoDepositarValorInvalido() {
        assertThrows(ValorInvalidoException.class,
                () -> conta.depositar(new BigDecimal("-80")));
    }

    @Test
    void deveTransferirValorParaOutraConta() {
        Conta destino = getConta();
        var valor = new BigDecimal("690.36");
        conta.depositar(valor);
        conta.transferir(valor, destino);
        assertEquals(valor, destino.getSaldo());
    }

    @Test
    void deveLancarExcecaoQuandoTransferirValorInvalido() {
        Conta destino = getConta();
        var valor = new BigDecimal("-690.36");
        assertThrows(ValorInvalidoException.class,
                () -> {
                        conta.depositar(valor);
                        conta.transferir(valor, destino);
                });
    }

    @Test
    void deveLancarExcecaoQuandoTransferirSemSaldo() {
        Conta destino = getConta();
        var valor = new BigDecimal("690.36");
        assertThrows(SaldoInsuficienteException.class,
                () -> {
                        conta.depositar(valor);
                        conta.transferir(valor.add(new BigDecimal("40")), destino);
                });
    }

    private static @NonNull Conta getConta() {
        Cliente cliente =
                new Cliente("Patrick Silva Rodrigues",
                        "111.444.777-35",
                        "264792");
        return new Conta(cliente,
                TipoConta.CORRENTE,
                LocalDate.now());
    }
}