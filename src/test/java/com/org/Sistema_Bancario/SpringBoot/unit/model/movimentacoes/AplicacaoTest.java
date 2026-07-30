package com.org.Sistema_Bancario.SpringBoot.unit.model.movimentacoes;

import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.DirecaoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.StatusAplicacao;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
@ExtendWith(MockitoExtension.class)
class AplicacaoTest {

    private Aplicacao aplicacao;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente(
                "Patrício Silva Rodriguete",
                "52998224725",
                "192292");

        Conta conta = new Conta(
                cliente,
                TipoConta.INVESTIMENTO,
                LocalDate.now());

        LocalDate hoje = LocalDate.now();
        aplicacao = new Aplicacao(
                conta,
                new BigDecimal("3000.00"),
                hoje.minusDays(7),
                hoje.minusDays(1),
                new BigDecimal("0.00042"),
                TipoMovimentacao.APLICACAO,
                DirecaoMovimentacao.SAIDA
        );
    }

    @Test
    void aplicarRendimentosDeveAdicionarUltimaCapitalizacaoESomarRendimentos() {
        aplicacao.aplicarRendimento(
               LocalDate.now(), new BigDecimal("100.86"));
        assertAll(
                () -> assertEquals(LocalDate.now(),aplicacao.getUltimaCapitalizacao()),
                () -> assertEquals(new BigDecimal("3100.86"),aplicacao.getValorAtual())
        );
    }

    @Test
    void resgatarDeveRetornarZeroComAplicacaoResgatada() {
        var resgate = aplicacao.resgatar(new BigDecimal("3000.00"));
        assertAll(
                () -> assertEquals(StatusAplicacao.RESGATADA,aplicacao.getStatus()),
                () -> assertEquals(BigDecimal.ZERO,resgate)
        );
    }
    @Test
    void resgatarDeveRetornarZero() {
        var resgate = aplicacao.resgatar(new BigDecimal("2000.00"));
        assertAll(
                () -> assertEquals(StatusAplicacao.ATIVO,aplicacao.getStatus()),
                () -> assertEquals(BigDecimal.ZERO,resgate)
        );
    }
    @Test
    void resgatarDeveRetornarRestante(){
        var resgate = aplicacao.resgatar(new BigDecimal("4000.00"));
        assertAll(
                () -> assertEquals(BigDecimal.ZERO,aplicacao.getValorAtual()),
                () -> assertEquals(StatusAplicacao.RESGATADA,aplicacao.getStatus()),
                () -> assertEquals(new BigDecimal("1000.00"),resgate)
        );
    }
}