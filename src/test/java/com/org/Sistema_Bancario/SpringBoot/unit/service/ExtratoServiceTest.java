package com.org.Sistema_Bancario.SpringBoot.unit.service;

import com.org.Sistema_Bancario.SpringBoot.dto.MovimentacaoResponse;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ExtratoVazioException;
import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.DirecaoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Aplicacao;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Deposito;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Movimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Rendimentos;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Saque;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Transferencia;
import com.org.Sistema_Bancario.SpringBoot.repository.ExtratoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class ExtratoServiceTest {

    @InjectMocks
    private ExtratoService extratoService;
    @Mock
    private ExtratoRepository extratoRepository;

    private Cliente cliente;
    private List<Movimentacao> movimentacoes;

    @BeforeEach
    void setUp() {
        cliente = new Cliente(
                "Patrício Silva Rodriguete",
                "12345678909",
                "192292");

        Conta contaCorrente = new Conta(
                cliente,
                TipoConta.CORRENTE,
                LocalDate.now());

        Conta contaInvestimento = new Conta(
                cliente,
                TipoConta.INVESTIMENTO,
                LocalDate.now());

        LocalDate hoje = LocalDate.now();
        LocalTime agora = LocalTime.now();

        movimentacoes = List.of(

                new Deposito(
                        contaCorrente,
                        new BigDecimal("1500.00"),
                        hoje.minusDays(10),
                        agora.minusHours(5),
                        TipoMovimentacao.DEPOSITO,
                        DirecaoMovimentacao.ENTRADA
                ),

                new Saque(
                        contaCorrente,
                        new BigDecimal("250.00"),
                        hoje.minusDays(9),
                        agora.minusHours(4),
                        TipoMovimentacao.SAQUE,
                        DirecaoMovimentacao.SAIDA
                ),

                new Deposito(
                        contaInvestimento,
                        new BigDecimal("5000.00"),
                        hoje.minusDays(8),
                        agora.minusHours(3),
                        TipoMovimentacao.DEPOSITO,
                        DirecaoMovimentacao.ENTRADA
                ),

                new Aplicacao(
                        contaInvestimento,
                        new BigDecimal("3000.00"),
                        hoje.minusDays(7),
                        hoje.minusDays(1),
                        new BigDecimal("0.00042"),
                        TipoMovimentacao.APLICACAO,
                        DirecaoMovimentacao.SAIDA
                ),

                new Rendimentos(
                        contaInvestimento,
                        hoje.minusDays(1),
                        agora.minusHours(2),
                        30,
                        new BigDecimal("0.00042"),
                        new BigDecimal("3000.00"),
                        new BigDecimal("3038.15"),
                        TipoMovimentacao.RENDIMENTO,
                        DirecaoMovimentacao.ENTRADA
                ),

                new Transferencia(

                        hoje.minusDays(5),
                        agora.minusHours(1),
                        new BigDecimal("400.00"),
                        contaCorrente,
                        contaInvestimento,
                        DirecaoMovimentacao.SAIDA,
                        TipoMovimentacao.PIX
                ),

                new Transferencia(
                        hoje.minusDays(2),
                        agora.minusMinutes(30),
                        new BigDecimal("150.00"),
                        contaInvestimento,
                        contaCorrente,
                        DirecaoMovimentacao.SAIDA,
                        TipoMovimentacao.DOC
                ),

                new Saque(
                        contaCorrente,
                        new BigDecimal("100.00"),
                        hoje,
                        agora,
                        TipoMovimentacao.SAQUE,
                        DirecaoMovimentacao.SAIDA
                )
        );
    }

    @Test
    void verExtratoDeveRetornarUmaListaDaClasseMovimentacaoResponse() {
        when(extratoRepository.verExtratos(cliente.getCpf()))
        .thenReturn(movimentacoes);
        var extrato = extratoService.verExtrato(cliente.getCpf());
        List<MovimentacaoResponse> movimentacaoResponseList =
                movimentacoes.stream().map(MovimentacaoResponse::new).toList();
        assertAll(
                () -> assertInstanceOf(movimentacaoResponseList.getClass(),extrato)
        );
    }
    @Test
    void verExtratosDeveLancarExtratoVazioException(){
        when(extratoRepository.verExtratos(cliente.getCpf()))
                .thenReturn(Collections.emptyList());
        assertThrows(ExtratoVazioException.class,
                () -> extratoService.verExtrato(cliente.getCpf()));
    }
}