package com.org.Sistema_Bancario.SpringBoot.unit.service;

import com.org.Sistema_Bancario.SpringBoot.exceptions.AplicacaoException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ContaException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ContaNaoEncontradaException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.MovimentacaoException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.OperacaoNaoPermitidaException;
import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.DirecaoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.HoraData;
import com.org.Sistema_Bancario.SpringBoot.model.StatusAplicacao;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Aplicacao;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Deposito;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Saque;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Transferencia;
import com.org.Sistema_Bancario.SpringBoot.repository.AplicacaoRepository;
import com.org.Sistema_Bancario.SpringBoot.repository.ContaRepository;
import com.org.Sistema_Bancario.SpringBoot.repository.TransferenciaRepository;
import com.org.Sistema_Bancario.SpringBoot.service.MovimentacaoService;
import com.org.Sistema_Bancario.SpringBoot.service.RendimentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.org.Sistema_Bancario.SpringBoot.model.RegrasDeBanco.TAXA_SAQUE;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimentacaoServiceTest {

    @InjectMocks
    private MovimentacaoService service;
    @Mock
    private RendimentoService rendimentoService;
    @Mock
    private ContaRepository contaRepository;
    @Mock
    private TransferenciaRepository transferenciaRepository;
    @Mock
    private AplicacaoRepository aplicacaoRepository;
    private Cliente cliente;
    private Cliente cliente2;
    private Conta contaCorrente;
    private Conta contaCorrente2;
    private Conta contaInvestimento;
    private Conta contaInvestimento2;
    private List<Aplicacao> aplicacoes;

    @BeforeEach
    void setUp() {
        cliente = new Cliente(
                "Patrício Silva Rodriguete",
                "12345678909",
                "192292");

        cliente2 = new Cliente(
                "Larissa Souza Silva",
                "11144477735",
                "204689");

        contaCorrente = new Conta(
                cliente,
                TipoConta.CORRENTE,
                LocalDate.now());

        contaInvestimento = new Conta(
                cliente,
                TipoConta.INVESTIMENTO,
                LocalDate.now());

        contaCorrente2 = new Conta(
                cliente2,
                TipoConta.CORRENTE,
                LocalDate.now());

        contaInvestimento2 = new Conta(
                cliente2,
                TipoConta.INVESTIMENTO,
                LocalDate.now());
        LocalDate hoje = LocalDate.now();
        aplicacoes = List.of(

                new Aplicacao(
                        contaInvestimento,
                        new BigDecimal("3000.00"),
                        hoje.minusDays(7),
                        hoje.minusDays(1),
                        new BigDecimal("0.00042"),
                        TipoMovimentacao.APLICACAO,
                        DirecaoMovimentacao.SAIDA
                ),
                new Aplicacao(
                        contaInvestimento,
                        new BigDecimal("3000.00"),
                        hoje.minusDays(17),
                        hoje.minusDays(2),
                        new BigDecimal("0.00042"),
                        TipoMovimentacao.APLICACAO,
                        DirecaoMovimentacao.SAIDA
                )
        );

    }

    @Test
    void getTime() {
       assertInstanceOf(HoraData.class,service.getTime());
    }

    @Test
    void transferirDeveRealizarUmaTransferencia() {
        contaCorrente.depositar(new BigDecimal("3000.00"));
        when(contaRepository.findByCpf(cliente.getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.of(contaCorrente));
        when(contaRepository.findByCpf(cliente2.getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.of(contaCorrente2));
        service.transferir(
                cliente.getCpf(),
                cliente2.getCpf(),
                new BigDecimal("1000"),
                TipoMovimentacao.DOC,
                TipoConta.CORRENTE,TipoConta.CORRENTE);
        assertAll(
                () -> assertEquals(new BigDecimal("2000.00"),
                        contaCorrente.getSaldo()),
                () -> assertEquals(new BigDecimal("1000.00"),
                        contaCorrente2.getSaldo())
        );
        verify(transferenciaRepository,times(2)).save(any(Transferencia.class));
    }

    @Test
    void transferirDeveLancarContaException() {
        assertThrows(ContaException.class,
                () -> service.transferir(
                cliente.getCpf(),
                cliente.getCpf(),
                new BigDecimal("1000"),
                TipoMovimentacao.PIX,
                TipoConta.CORRENTE,TipoConta.CORRENTE)
        );
        verify(transferenciaRepository,never()).save(any());
    }
    @Test
    void transferirDeveLancarContaNaoEncontradoParaContaOrigem() {
        when(contaRepository.findByCpf(contaCorrente.getCliente()
                .getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.empty());
        assertThrows(ContaException.class,
                () -> service.transferir(
                        cliente.getCpf(),
                        cliente2.getCpf(),
                        new BigDecimal("1000"),
                        TipoMovimentacao.TED,
                        TipoConta.CORRENTE,TipoConta.CORRENTE)
        );
        verify(transferenciaRepository,never()).save(any());
    }
    @Test
    void transferirDeveLancarContaNaoEncontradoParaContaDestino() {
        when(contaRepository.findByCpf(contaCorrente.getCliente()
                .getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.of(contaCorrente));
        when(contaRepository.findByCpf(contaCorrente2.getCliente()
                .getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.empty());
        assertThrows(ContaException.class,
                () -> service.transferir(
                        cliente.getCpf(),
                        cliente2.getCpf(),
                        new BigDecimal("1000"),
                        TipoMovimentacao.TEF,
                        TipoConta.CORRENTE,TipoConta.CORRENTE)
        );
        verify(transferenciaRepository,never()).save(any());
    }
    @Test
    void transferirDeveLancarOpercaoNaoPermitidaException() {
        when(contaRepository.findByCpf(
                cliente.getCpf(), TipoConta.INVESTIMENTO))
                .thenReturn(Optional.of(contaInvestimento));
        when(contaRepository.findByCpf(
                cliente2.getCpf(), TipoConta.INVESTIMENTO))
                .thenReturn(Optional.of(contaInvestimento2));
        assertThrows(OperacaoNaoPermitidaException.class,
                () -> service.transferir(
                        cliente.getCpf(),
                        cliente2.getCpf(),
                        new BigDecimal("1000"),
                        TipoMovimentacao.PIX,
                        TipoConta.INVESTIMENTO,
                        TipoConta.INVESTIMENTO)
        );
        verify(transferenciaRepository,never()).save(any());
    }
    @Test
    void transferirDeveLancarOpercaoExceptionQuandoClienteForDiferente() {
        when(contaRepository.findByCpf(
                cliente.getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.of(contaCorrente));
        when(contaRepository.findByCpf(
                cliente2.getCpf(), TipoConta.INVESTIMENTO))
                .thenReturn(Optional.of(contaInvestimento2));
        assertThrows(OperacaoNaoPermitidaException.class,
                () -> service.transferir(
                        cliente.getCpf(),
                        cliente2.getCpf(),
                        new BigDecimal("1000"),
                        TipoMovimentacao.APLICACAO,
                        TipoConta.CORRENTE,
                        TipoConta.INVESTIMENTO)
        );
        verify(transferenciaRepository,never()).save(any());
    }
    @Test
    void transferirDeveLancarOpercaoExceptionQuandoTipoDeMovimentacaoForDiferenteDeAplicacaoOuResgate() {
        when(contaRepository.findByCpf(
                cliente.getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.of(contaCorrente));
        when(contaRepository.findByCpf(
                cliente.getCpf(), TipoConta.INVESTIMENTO))
                .thenReturn(Optional.of(contaInvestimento));
        assertAll(
                () -> assertThrows(OperacaoNaoPermitidaException.class,
                        () -> service.transferir(
                                cliente.getCpf(),
                                cliente.getCpf(),
                                new BigDecimal("1000"),
                                TipoMovimentacao.PIX,
                                TipoConta.CORRENTE,
                                TipoConta.INVESTIMENTO)
                ),
                () -> assertThrows(OperacaoNaoPermitidaException.class,
                        () -> service.transferir(
                                cliente.getCpf(),
                                cliente.getCpf(),
                                new BigDecimal("1000"),
                                TipoMovimentacao.PIX,
                                TipoConta.INVESTIMENTO,
                                TipoConta.CORRENTE)
                )

        );
        verify(transferenciaRepository,never()).save(any());
    }
    @Test
    void transferirDeveLancarMovimentacaoExceptionQuandoPassarDoLimiteDoTipoDeMovimentacaoForDOC() {
        when(contaRepository.findByCpf(
                cliente.getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.of(contaCorrente));
        when(contaRepository.findByCpf(
                cliente2.getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.of(contaCorrente2));
        assertThrows(MovimentacaoException.class,
                () -> service.transferir(
                        cliente.getCpf(),
                        cliente2.getCpf(),
                        new BigDecimal("5000"),
                        TipoMovimentacao.DOC,
                        TipoConta.CORRENTE,
                        TipoConta.CORRENTE)
        );
        verify(transferenciaRepository,never()).save(any());
    }
    @Test
    void transferirDeveRealizarResgate() {
        contaInvestimento.depositar(new BigDecimal("6000"));
        when(contaRepository.findByCpf(
                cliente.getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.of(contaCorrente));
        when(contaRepository.findByCpf(
                cliente.getCpf(), TipoConta.INVESTIMENTO))
                .thenReturn(Optional.of(contaInvestimento));
        when(aplicacaoRepository.listarAplicacoes(contaInvestimento, StatusAplicacao.ATIVO))
                .thenReturn(aplicacoes);
        service.transferir(
                cliente.getCpf(),
                cliente.getCpf(),
                new BigDecimal("2500"),
                TipoMovimentacao.RESGATE,
                TipoConta.INVESTIMENTO,
                TipoConta.CORRENTE);
        assertAll(
                () -> assertEquals(new BigDecimal("3500.00"),
                        contaInvestimento.getSaldo()),
                () -> assertTrue(aplicacoes.stream()
                        .anyMatch(apl ->
                               apl.getValorAtual().compareTo(
                                       new BigDecimal("500.00")) == 0)),
                () -> assertTrue(aplicacoes.stream()
                        .anyMatch(apl ->
                               apl.getValorAtual().compareTo(
                                       new BigDecimal("3000.00")) == 0))
        );
        verify(transferenciaRepository,times(2)).save(any(Transferencia.class));
    }
    @Test
    void transferirDeveLancarAplicacaoExceptionParaListaVaziaAoRealizarResgate() {
        contaInvestimento.depositar(new BigDecimal("6000"));
        when(contaRepository.findByCpf(
                cliente.getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.of(contaCorrente));
        when(contaRepository.findByCpf(
                cliente.getCpf(), TipoConta.INVESTIMENTO))
                .thenReturn(Optional.of(contaInvestimento));
        when(aplicacaoRepository.listarAplicacoes(contaInvestimento, StatusAplicacao.ATIVO))
                .thenReturn(Collections.emptyList());

        assertAll(
                () -> assertThrows(AplicacaoException.class,
                        () -> service.transferir(
                                cliente.getCpf(),
                                cliente.getCpf(),
                                new BigDecimal("2500"),
                                TipoMovimentacao.RESGATE,
                                TipoConta.INVESTIMENTO,
                                TipoConta.CORRENTE))
        );
        verify(transferenciaRepository,never()).save(any());
    }
    @Test
    void transferirDeveLancarAplicacaoExceptionAoRealizarResgate() {
        contaInvestimento.depositar(new BigDecimal("6000"));
        when(contaRepository.findByCpf(
                cliente.getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.of(contaCorrente));
        when(contaRepository.findByCpf(
                cliente.getCpf(), TipoConta.INVESTIMENTO))
                .thenReturn(Optional.of(contaInvestimento));
        when(aplicacaoRepository.listarAplicacoes(contaInvestimento, StatusAplicacao.ATIVO))
                .thenReturn(aplicacoes);

        assertAll(
                () -> assertThrows(AplicacaoException.class,
                        () -> service.transferir(
                                cliente.getCpf(),
                                cliente.getCpf(),
                                new BigDecimal("7500"),
                                TipoMovimentacao.RESGATE,
                                TipoConta.INVESTIMENTO,
                                TipoConta.CORRENTE))
        );
        verify(transferenciaRepository,never()).save(any());
    }
    @Test
    void transferirDeveRealizarAplicacao() {
        contaCorrente.depositar(new BigDecimal("6000"));
        when(contaRepository.findByCpf(
                cliente.getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.of(contaCorrente));
        when(contaRepository.findByCpf(
                cliente.getCpf(), TipoConta.INVESTIMENTO))
                .thenReturn(Optional.of(contaInvestimento));
        service.transferir(
                cliente.getCpf(),
                cliente.getCpf(),
                new BigDecimal("4000"),
                TipoMovimentacao.APLICACAO,
                TipoConta.CORRENTE,
                TipoConta.INVESTIMENTO);
        assertAll(
                () -> assertEquals(new BigDecimal("2000.00"),
                        contaCorrente.getSaldo()),
                () -> assertEquals(new BigDecimal("4000.00"),
                        contaInvestimento.getSaldo())
        );
        verify(transferenciaRepository,times(1)).save(any(Aplicacao.class));
        verify(transferenciaRepository,times(2)).save(any(Transferencia.class));
    }

    @Test
    void transferirDeveLancarMovimentacaoException() {
        when(contaRepository.findByCpf(cliente.getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.of(contaCorrente));
        when(contaRepository.findByCpf(cliente2.getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.of(contaCorrente2));
        assertAll(
                () -> assertThrows(MovimentacaoException.class,
                        () -> service.transferir(
                                cliente.getCpf(),
                                cliente2.getCpf(),
                                new BigDecimal("1000"),
                                TipoMovimentacao.DEPOSITO,
                                TipoConta.CORRENTE,TipoConta.CORRENTE)
                ),
                () -> assertThrows(MovimentacaoException.class,
                        () -> service.transferir(
                                cliente.getCpf(),
                                cliente2.getCpf(),
                                new BigDecimal("1000"),
                                TipoMovimentacao.SAQUE,
                                TipoConta.CORRENTE,TipoConta.CORRENTE)
                )
        );

        verify(transferenciaRepository,never()).save(any());
    }

    @Test
    void realizarMovimentacaoDeveRealizarDeposito() {
        when(contaRepository.findByCpf(cliente.getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.of(contaCorrente));
        var valor = new BigDecimal("2000.00");
        service.realizarMovimentacao(cliente.getCpf(),
                valor,TipoMovimentacao.DEPOSITO);
        assertEquals(valor,contaCorrente.getSaldo());
        verify(transferenciaRepository).save(any(Deposito.class));
    }

    @Test
    void realizarMovimentacaoDeveLancarContaNaoEncontradaException() {
        when(contaRepository.findByCpf(cliente.getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.empty());
        var valor = new BigDecimal("2000.00");

        assertThrows(ContaNaoEncontradaException.class,
                () -> service.realizarMovimentacao(cliente.getCpf(),
                        valor,TipoMovimentacao.DEPOSITO));
        verify(transferenciaRepository, never()).save(any());
    }

    @Test
    void realizarMovimentacaoDeveRealizarSaque() {
        var valor = new BigDecimal("2000.00");
        contaCorrente.depositar(new BigDecimal("6000"));
        when(contaRepository.findByCpf(cliente.getCpf(), TipoConta.CORRENTE))
                .thenReturn(Optional.of(contaCorrente));
        var resultado = contaCorrente.getSaldo()
                .subtract(valor.add(valor.multiply(TAXA_SAQUE))
                        .setScale(2, RoundingMode.HALF_EVEN))
                .setScale(2, RoundingMode.HALF_EVEN);
        service.realizarMovimentacao(cliente.getCpf(),
                valor,TipoMovimentacao.SAQUE);
        assertEquals(resultado,contaCorrente.getSaldo());
        verify(transferenciaRepository).save(any(Saque.class));
    }
}
