package com.org.Sistema_Bancario.SpringBoot.unit.service;

import com.org.Sistema_Bancario.SpringBoot.exceptions.AplicacaoNaoEncontradaException;
import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.DirecaoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.StatusAplicacao;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Aplicacao;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Rendimentos;
import com.org.Sistema_Bancario.SpringBoot.repository.AplicacaoRepository;
import com.org.Sistema_Bancario.SpringBoot.repository.TransferenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RendimentoServiceTest {
    @InjectMocks
    private RendimentoService rendimentoService;
    @Mock
    private AplicacaoRepository aplicacaoRepository;
    @Mock
    private TransferenciaRepository transferenciaRepository;
    private Conta contaInvestimento;
    private List<Aplicacao> aplicacoes;
    private BigDecimal valorAtual;

    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente(
                "Larissa Souza Silva",
                "11144477735",
                "204689");
        contaInvestimento = new Conta(
                cliente,
                TipoConta.INVESTIMENTO,
                LocalDate.now());
        LocalDate hoje = LocalDate.now();
        valorAtual = new BigDecimal("3000.00");
        aplicacoes = List.of(

                new Aplicacao(
                        contaInvestimento,
                        valorAtual,
                        hoje.minusDays(7),
                        hoje.minusDays(1),
                        new BigDecimal("0.00042"),
                        TipoMovimentacao.APLICACAO,
                        DirecaoMovimentacao.SAIDA
                ),
                new Aplicacao(
                        contaInvestimento,
                        valorAtual,
                        hoje.minusDays(17),
                        hoje.minusDays(2),
                        new BigDecimal("0.00042"),
                        TipoMovimentacao.APLICACAO,
                        DirecaoMovimentacao.SAIDA
                ),
                new Aplicacao(
                        contaInvestimento,
                        valorAtual,
                        hoje,
                        hoje,
                        new BigDecimal("0.00042"),
                        TipoMovimentacao.APLICACAO,
                        DirecaoMovimentacao.SAIDA
                )
        );
    }

    @Test
    void aplicarRendimentosDeveLancarAplicacaoNaoEncontradaException() {
        when(aplicacaoRepository.listarAplicacoes(contaInvestimento, StatusAplicacao.ATIVO))
                .thenReturn(Collections.emptyList());
        assertThrows(AplicacaoNaoEncontradaException.class,
                () -> rendimentoService.aplicarRendimentos(contaInvestimento));
    }

    @Test
    void aplicarRendimentosDeveRealizarRendimentoEmCadaAplicacao() {
        when(aplicacaoRepository.listarAplicacoes(contaInvestimento, StatusAplicacao.ATIVO))
                .thenReturn(aplicacoes);
        var resultadoAplicacao1 = valorAtual.add(
                valorAtual.multiply(aplicacoes.getFirst().getTaxaDiaria())
                        .multiply(BigDecimal
                                .valueOf(ChronoUnit.DAYS.between(
                                        aplicacoes.getFirst().getUltimaCapitalizacao(),
                                        LocalDate.now()))));
        var resultadoAplicacao2 = valorAtual.add(
                valorAtual.multiply(aplicacoes.get(1).getTaxaDiaria())
                        .multiply(BigDecimal
                                .valueOf(ChronoUnit.DAYS.between(
                                        aplicacoes.get(1).getUltimaCapitalizacao(),
                                        LocalDate.now()))));
        var resultadoAplicacao3 = valorAtual.add(
                valorAtual.multiply(aplicacoes.get(2).getTaxaDiaria())
                        .multiply(BigDecimal
                                .valueOf(ChronoUnit.DAYS.between(
                                        aplicacoes.get(2).getUltimaCapitalizacao(),
                                        LocalDate.now()))));
        rendimentoService.aplicarRendimentos(contaInvestimento);
        assertAll(
                () -> assertEquals(resultadoAplicacao1,aplicacoes.getFirst().getValorAtual()),
                () -> assertEquals(resultadoAplicacao2,aplicacoes.get(1).getValorAtual()),
                () -> assertEquals(resultadoAplicacao3.setScale(2, RoundingMode.UNNECESSARY),aplicacoes.get(2).getValorAtual())
        );
        verify(transferenciaRepository, times(2)).save(any(Rendimentos.class));
    }
}