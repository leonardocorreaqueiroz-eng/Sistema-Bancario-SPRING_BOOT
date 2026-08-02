package com.org.Sistema_Bancario.SpringBoot.unit.repository;

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
import com.org.Sistema_Bancario.SpringBoot.repository.ClienteRepository;
import com.org.Sistema_Bancario.SpringBoot.repository.ContaRepository;
import com.org.Sistema_Bancario.SpringBoot.repository.ExtratoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class ExtratoRepositoryTest {
    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ContaRepository contaRepository;
    @Autowired
    private ExtratoRepository extratoRepository;
    private List<Movimentacao> movimentacoes;
    private Cliente cliente;
    private Conta contaCorrente;
    private Conta contaInvestimento;

    @BeforeEach
    void setUp() {

        cliente = new Cliente(
                "Patrício Silva Rodriguete",
                "12345678909",
                "192292");

        contaCorrente = new Conta(
                cliente,
                TipoConta.CORRENTE,
                LocalDate.now());

        contaInvestimento = new Conta(
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
                        TipoMovimentacao.TED
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
    void verExtratosDeveRetornarUmaListaDeMovimentacoes() {
        clienteRepository.save(cliente);
        contaRepository.save(contaCorrente);
        contaRepository.save(contaInvestimento);
        extratoRepository.saveAll(movimentacoes);
       assertEquals(8,extratoRepository
               .verExtratos(cliente.getCpf()).size());
    }
}