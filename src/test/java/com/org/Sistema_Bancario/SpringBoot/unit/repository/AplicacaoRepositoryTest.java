package com.org.Sistema_Bancario.SpringBoot.unit.repository;

import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.DirecaoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.RegrasDeBanco;
import com.org.Sistema_Bancario.SpringBoot.model.StatusAplicacao;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Aplicacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

@DataJpaTest
class AplicacaoRepositoryTest {

    @Autowired
    private AplicacaoRepository repository;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private ClienteRepository clienteRepository;
    private Cliente cliente;
    private Conta conta;
    private List<Aplicacao> aplicacoes;

    @BeforeEach
    void setUp() {
         cliente = new Cliente(
                "Patrício Silva Rodriguete",
                "12345678909",
                "192292");
         conta = new Conta(
                cliente,
                TipoConta.CORRENTE,
                LocalDate.now());
         aplicacoes = List.of(
                new Aplicacao(
                        conta,
                        new BigDecimal("1000.00"),
                        LocalDate.of(2026, 1, 10),
                        LocalDate.of(2026, 1, 10),
                        RegrasDeBanco.TAXA_INVESTIMENTO,
                        TipoMovimentacao.APLICACAO,
                        DirecaoMovimentacao.SAIDA
                ),
                new Aplicacao(
                        conta,
                        new BigDecimal("2500.50"),
                        LocalDate.of(2026, 3, 15),
                        LocalDate.of(2026, 3, 15),
                        RegrasDeBanco.TAXA_INVESTIMENTO,
                        TipoMovimentacao.APLICACAO,
                        DirecaoMovimentacao.SAIDA
                ),
                new Aplicacao(
                        conta,
                        new BigDecimal("5000.00"),
                        LocalDate.of(2026, 6, 20),
                        LocalDate.of(2026, 6, 20),
                        RegrasDeBanco.TAXA_INVESTIMENTO,
                        TipoMovimentacao.APLICACAO,
                        DirecaoMovimentacao.SAIDA
                )
        );
    }

    @Test
    void deveRetornarListagemDasAplicacoes() {

        clienteRepository.save(cliente);
        contaRepository.save(conta);
        repository.saveAll(aplicacoes);
        assertEquals(aplicacoes, repository.listarAplicacoes(conta, StatusAplicacao.ATIVO));
    }

    @Test
    void deveRetornarListagemDasAplicacoesVazia() {

        clienteRepository.save(cliente);
        contaRepository.save(conta);
        repository.saveAll(aplicacoes);
        assertEquals(Collections.emptyList(), repository.listarAplicacoes(conta, StatusAplicacao.RESGATADA));
    }
}