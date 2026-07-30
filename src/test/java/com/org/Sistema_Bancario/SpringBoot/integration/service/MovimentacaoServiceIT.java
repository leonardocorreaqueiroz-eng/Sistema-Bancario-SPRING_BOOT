package com.org.Sistema_Bancario.SpringBoot.integration.service;

import com.org.Sistema_Bancario.SpringBoot.dto.CadastroRequest;
import com.org.Sistema_Bancario.SpringBoot.exceptions.AplicacaoNaoEncontradaException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ContaException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ContaNaoEncontradaException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ExtratoVazioException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.MovimentacaoException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.OperacaoNaoPermitidaException;
import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import com.org.Sistema_Bancario.SpringBoot.model.DirecaoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.RegrasDeBanco;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import com.org.Sistema_Bancario.SpringBoot.service.ContaService;
import com.org.Sistema_Bancario.SpringBoot.service.ExtratoService;
import com.org.Sistema_Bancario.SpringBoot.service.MovimentacaoService;

import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

@SpringBootTest
@Transactional
public class MovimentacaoServiceIT {
    @Autowired
    private ContaService contaService;
    @Autowired
    private ExtratoService extratoService;
    @Autowired
    private MovimentacaoService movimentacaoService;
    private CadastroRequest cadastroRequest;
    private CadastroRequest cadastroRequest2;
    private Cliente cliente;
    private Cliente cliente2;

    @BeforeEach
    void setUp() {
        cliente = new Cliente(
                "Patrício Silva Rodriguete",
                "12345678909",
                "192292");
        cadastroRequest = new CadastroRequest(
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getPassword()
        );
        cliente2 = new Cliente(
                "Larissa Souza Silva",
                "11144477735",
                "204689");
        cadastroRequest2 = new CadastroRequest(
                cliente2.getNome(),
                cliente2.getCpf(),
                cliente2.getPassword()
        );
    }
    @Test
    @DisplayName("Deve realizar transferências de conta corrente")
    void deveRealizarTransferenciaDeContaCorrente() {
        criarDuasContas();
        movimentacaoService.realizarMovimentacao(
                cliente.getCpf(),
                new BigDecimal("400"),
                TipoMovimentacao.DEPOSITO
                );
        assertEquals(new BigDecimal("400.00"),
                contaService.saldoConta(cliente.getCpf(),TipoConta.CORRENTE));
        movimentacaoService.transferir(
                cliente.getCpf(),
                cliente2.getCpf(),
                new BigDecimal("300.8"),
                TipoMovimentacao.PIX,
                TipoConta.CORRENTE,TipoConta.CORRENTE
        );
        var numeroEsperadoConta1 = 2;
        var numeroEsperadoConta2 = 1;
        var extrato1 = extratoService.verExtrato(cliente.getCpf());
        var extrato2 = extratoService.verExtrato(cliente2.getCpf());
        assertAll(
                () -> assertEquals(new BigDecimal("99.20"),
                        contaService.saldoConta(cliente.getCpf(),TipoConta.CORRENTE)),
                () -> assertEquals(new BigDecimal("300.80"),
                        contaService.saldoConta(cliente2.getCpf(),TipoConta.CORRENTE)),
                () -> assertEquals(numeroEsperadoConta1,extrato1.size()),
                () -> assertEquals(TipoMovimentacao.DEPOSITO,
                        extrato1.getFirst().movimentacao().getTipo()),
                () -> assertEquals(TipoMovimentacao.PIX,
                        extrato1.get(1).movimentacao().getTipo()),
                () -> assertEquals(DirecaoMovimentacao.SAIDA,
                        extrato1.get(1).movimentacao().getDirecao()),
                () -> assertEquals(numeroEsperadoConta2, extrato2.size()),
                () -> assertEquals(TipoMovimentacao.PIX,
                        extrato2.getFirst().movimentacao().getTipo()),
                () -> assertEquals(DirecaoMovimentacao.ENTRADA,
                        extrato2.getFirst().movimentacao().getDirecao())
        );
    }
    @Test
    @DisplayName("Deve realizar movimentações de depósito e saque")
    void deveRealizarMovimentacaoDeDepositoESaque() {
        contaService.criarConta(cadastroRequest);
        movimentacaoService.realizarMovimentacao(
                cliente.getCpf(),
                new BigDecimal("400"),
                TipoMovimentacao.DEPOSITO
        );
        var valor = new BigDecimal("100")
                .setScale(2, RoundingMode.HALF_EVEN);
        var resultado = contaService.saldoConta(cliente.getCpf(),TipoConta.CORRENTE)
                .subtract(valor
                        .add(valor
                                .multiply(RegrasDeBanco.TAXA_SAQUE)))
                .setScale(2, RoundingMode.HALF_EVEN);
        movimentacaoService.realizarMovimentacao(
                cliente.getCpf(),
                valor,
                TipoMovimentacao.SAQUE
        );
        var extrato = extratoService.verExtrato(cliente.getCpf());
        assertAll(
                () -> assertEquals(resultado,
                        contaService.saldoConta(cliente.getCpf(), TipoConta.CORRENTE)),
                () -> assertEquals(2, extrato.size()),
                () -> assertEquals(resultado,
                    contaService.saldoConta(cliente.getCpf(),TipoConta.CORRENTE))
        );
    }
    @Test
    @DisplayName("Deve realizar transações de conta investimento")
    void deveRealizarTransferenciaDeContaInvestimento() {
        contaService.criarConta(cadastroRequest);
        movimentacaoService.realizarMovimentacao(
                cliente.getCpf(),
                new BigDecimal("4000"),
                TipoMovimentacao.DEPOSITO);
        assertEquals(new BigDecimal("4000.00"),
                contaService.saldoConta(cliente.getCpf(),TipoConta.CORRENTE));
        movimentacaoService.transferir(
                cliente.getCpf(),
                cliente.getCpf(),
                new BigDecimal("1500"),
                TipoMovimentacao.APLICACAO,
                TipoConta.CORRENTE,TipoConta.INVESTIMENTO
        );
        movimentacaoService.transferir(
                cliente.getCpf(),
                cliente.getCpf(),
                new BigDecimal("1500"),
                TipoMovimentacao.APLICACAO,
                TipoConta.CORRENTE,TipoConta.INVESTIMENTO
        );
        var numeroEsperadoPosAplicacao = 7;
        assertAll(
                () -> assertEquals(numeroEsperadoPosAplicacao,extratoService.verExtrato(cliente.getCpf()).size()),
                () -> assertEquals(new BigDecimal("1000.00"),contaService.saldoConta(cliente.getCpf(),TipoConta.CORRENTE)),
                () -> assertEquals(new BigDecimal("3000.00"),contaService.saldoConta(cliente.getCpf(),TipoConta.INVESTIMENTO))
        );
        movimentacaoService.transferir(
                cliente.getCpf(),
                cliente.getCpf(),
                new BigDecimal("2500"),
                TipoMovimentacao.RESGATE,
                TipoConta.INVESTIMENTO,TipoConta.CORRENTE
        );
        var numeroEsperadoPosResgate = 9;
        assertAll(
                () -> assertEquals(numeroEsperadoPosResgate,extratoService.verExtrato(cliente.getCpf()).size()),
                () -> assertEquals(new BigDecimal("3500.00"),contaService.saldoConta(cliente.getCpf(),TipoConta.CORRENTE)),
                () -> assertEquals(new BigDecimal("500.00"),contaService.saldoConta(cliente.getCpf(),TipoConta.INVESTIMENTO))
        );
    }
    @Test
    @DisplayName("Deve lançar erro ao transferir para a mesma conta")
    void deveLancarErroAoTransferirParaMesmaConta() {
        contaService.criarConta(cadastroRequest);
        assertThrows(ContaException.class,
                () ->         movimentacaoService.transferir(
                        cliente.getCpf(),
                        cliente.getCpf(),
                        new BigDecimal("2500"),
                        TipoMovimentacao.PIX,
                        TipoConta.CORRENTE,TipoConta.CORRENTE
                ));
        assertThrows(ExtratoVazioException.class,
                () -> extratoService.verExtrato(cliente.getCpf()));
    }
    @Test
    @DisplayName("Deve lançar erro ao transferir com uma conta inexistente")
    void deveLancarErroAoTransferirComUmaContaInexistente() {
        contaService.criarConta(cadastroRequest2);
        assertThrows(ContaNaoEncontradaException.class,
                () ->   movimentacaoService.transferir(
                        cliente.getCpf(),
                        cliente2.getCpf(),
                        new BigDecimal("2500"),
                        TipoMovimentacao.PIX,
                        TipoConta.CORRENTE,TipoConta.CORRENTE
                )
        );
        assertThrows(ExtratoVazioException.class,
                () -> extratoService.verExtrato(cliente2.getCpf()));
    }
    @Test
    @DisplayName("Deve lançar erro ao transferir com para uma conta inexistente")
    void deveLancarErroAoTransferirParaUmaContaInexistente() {
        contaService.criarConta(cadastroRequest);
        assertThrows(ContaNaoEncontradaException.class,
                () ->   movimentacaoService.transferir(
                        cliente.getCpf(),
                        cliente2.getCpf(),
                        new BigDecimal("2500"),
                        TipoMovimentacao.PIX,
                        TipoConta.CORRENTE,TipoConta.CORRENTE
                )
        );
        assertThrows(ExtratoVazioException.class,
                () -> extratoService.verExtrato(cliente.getCpf()));
    }
    @Test
    @DisplayName("Deve lançar erro ao transferir com valor de DOC acima do limite")
    void deveLancarErroAoTransferirComValorDeDOCAcimaDoLimite() {
        criarDuasContas();
        movimentacaoService.realizarMovimentacao(
                cliente.getCpf(),
                new BigDecimal("10000"),
                TipoMovimentacao.DEPOSITO);
        assertThrows(MovimentacaoException.class,
                () ->   movimentacaoService.transferir(
                        cliente.getCpf(),
                        cliente2.getCpf(),
                        new BigDecimal("5000"),
                        TipoMovimentacao.DOC,
                        TipoConta.CORRENTE,TipoConta.CORRENTE
                )
        );
        var extrato = extratoService.verExtrato(cliente.getCpf());
        var numeroEsperado = 1;
        assertAll(
                () -> assertEquals(new BigDecimal("10000.00"),
                        contaService.saldoConta(cliente.getCpf(),TipoConta.CORRENTE)),
                () -> assertEquals(numeroEsperado,extrato.size()),
                () -> assertEquals(TipoMovimentacao.DEPOSITO,extrato.getFirst().movimentacao().getTipo()),
                () -> assertThrows(ExtratoVazioException.class,
                        () -> extratoService.verExtrato(cliente2.getCpf()))
        );
    }
    @Test
    @DisplayName("Deve lançar erro ao realizar aplicação entre clientes diferentes")
    void deveLancarErroAoRealizarAplicacaoEntreClientesDiferentes() {
        criarDuasContas();
        movimentacaoService.realizarMovimentacao(
                cliente.getCpf(),
                new BigDecimal("10000"),
                TipoMovimentacao.DEPOSITO);
        assertThrows(OperacaoNaoPermitidaException.class,
                () ->   movimentacaoService.transferir(
                        cliente.getCpf(),
                        cliente2.getCpf(),
                        new BigDecimal("5000"),
                        TipoMovimentacao.APLICACAO,
                        TipoConta.CORRENTE,TipoConta.INVESTIMENTO
                )
        );
        var extrato = extratoService.verExtrato(cliente.getCpf());
        var numeroEsperado = 1;
        assertAll(
                () -> assertEquals(new BigDecimal("10000.00"),
                        contaService.saldoConta(cliente.getCpf(),TipoConta.CORRENTE)),
                () -> assertEquals(numeroEsperado,extrato.size()),
                () -> assertEquals(TipoMovimentacao.DEPOSITO,extrato.getFirst().movimentacao().getTipo()),
                () -> assertThrows(ExtratoVazioException.class,
                        () -> extratoService.verExtrato(cliente2.getCpf()))
        );

    }

    private void criarDuasContas() {
        contaService.criarConta(cadastroRequest);
        contaService.criarConta(cadastroRequest2);
    }

    @Test
    @DisplayName("Deve lançar erro ao realizar resgate sem aplicações")
    void deveLancarErroAoRealizarResgateSemAplicacoes() {
        criarDuasContas();

        assertThrows(AplicacaoNaoEncontradaException.class,
                () ->   movimentacaoService.transferir(
                        cliente.getCpf(),
                        cliente.getCpf(),
                        new BigDecimal("5000"),
                        TipoMovimentacao.RESGATE,
                        TipoConta.INVESTIMENTO,TipoConta.CORRENTE
                )
        );
        assertEquals(BigDecimal.ZERO,
                contaService.saldoConta(cliente.getCpf(),TipoConta.CORRENTE));
        assertThrows(ExtratoVazioException.class,
                () -> extratoService.verExtrato(cliente.getCpf()));
    }
    @Test
    @DisplayName("Deve lançar erro ao realizar transferência entre contas investimentos")
    void deveLancarErroAoTransferirEntreContasInvestimento() {
        criarDuasContas();

        assertThrows(OperacaoNaoPermitidaException.class,
                () ->   movimentacaoService.transferir(
                        cliente.getCpf(),
                        cliente2.getCpf(),
                        new BigDecimal("5000"),
                        TipoMovimentacao.RESGATE,
                        TipoConta.INVESTIMENTO,TipoConta.INVESTIMENTO
                )
        );
        assertEquals(BigDecimal.ZERO,
                contaService.saldoConta(cliente.getCpf(),TipoConta.INVESTIMENTO));
        assertEquals(BigDecimal.ZERO,
                contaService.saldoConta(cliente2.getCpf(),TipoConta.INVESTIMENTO));
        assertThrows(ExtratoVazioException.class,
                () -> extratoService.verExtrato(cliente.getCpf()));
        assertThrows(ExtratoVazioException.class,
                () -> extratoService.verExtrato(cliente2.getCpf()));
    }
}
