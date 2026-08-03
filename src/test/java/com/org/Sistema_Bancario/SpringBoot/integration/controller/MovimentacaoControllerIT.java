package com.org.Sistema_Bancario.SpringBoot.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.org.Sistema_Bancario.SpringBoot.dto.MovimentacaoResponse;
import com.org.Sistema_Bancario.SpringBoot.dto.TransferenciaRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.ValorRequest;
import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.DirecaoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.RegrasDeBanco;
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
import com.org.Sistema_Bancario.SpringBoot.repository.TransferenciaRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class MovimentacaoControllerIT {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ExtratoRepository extratoRepository;
    @Autowired
    private ContaRepository contaRepository;
    @Autowired
    private TransferenciaRepository transferenciaRepository;
    @Autowired
    private ClienteRepository clienteRepository;
    private ObjectMapper objectMapper;
    private Cliente cliente;
    private Cliente cliente2;
    private List<Movimentacao> movimentacoes;
    private Conta contaCorrente;
    private Conta contaCorrente2;
    private Conta contaInvestimento;
    private LocalDate hoje;

    @BeforeEach
    void setUp() {
        cliente =
                new Cliente("Patrício Silva Rodriguete",
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
        cliente2 =
                new Cliente(
                        "Larissa Souza Silva",
                        "11144477735",
                        "204689");
        contaCorrente2 = new Conta(
                cliente2,
                TipoConta.CORRENTE,
                LocalDate.now());


        hoje = LocalDate.of(2026, 7, 23);
        LocalTime agora = LocalTime.of(15, 30);

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
        objectMapper = JsonMapper.builder()
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .findAndAddModules()
                .build();
    }
    @Test
    void develistarMovimentacao() throws Exception{
        clienteRepository.save(cliente);
        contaRepository.save(contaCorrente);
        contaRepository.save(contaInvestimento);
        transferenciaRepository.saveAll(movimentacoes);
        List<MovimentacaoResponse> movimentacaoResponseList = movimentacoes.stream()
                .sorted(Comparator.comparing(Movimentacao::getData, Comparator.reverseOrder())
                .thenComparing(Movimentacao::getHora, Comparator.reverseOrder()))
                .map(MovimentacaoResponse::new).toList();
        mockMvc.perform(get("/api/contas/verExtrato")
                .with(user(cliente.getCpf())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(objectMapper
                        .writeValueAsString(movimentacaoResponseList)));
    }
    @Test
    void deveLancarExtratoVazioExceptionlistarMovimentacao() throws Exception{
        clienteRepository.save(cliente);
        contaRepository.save(contaCorrente);
        contaRepository.save(contaInvestimento);
        mockMvc.perform(get("/api/contas/verExtrato")
                .with(user(cliente.getCpf())))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    @Test
    void deveRealizarMovimentacaoDeSaque() throws Exception{
        clienteRepository.save(cliente);
        contaCorrente.depositar(new BigDecimal("1600"));
        var valor = new BigDecimal("600")
                .setScale(2, RoundingMode.HALF_EVEN);
        var resultado = contaCorrente.getSaldo()
                .subtract(valor
                        .add(valor
                                .multiply(RegrasDeBanco.TAXA_SAQUE)))
                .setScale(2, RoundingMode.HALF_EVEN);
        contaRepository.save(contaCorrente);
        mockMvc.perform(post("/api/contas/saque")
                .with(user(cliente.getCpf()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ValorRequest(valor))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Saque realizado com sucesso!"));
        Conta conta = contaRepository.findByCpf(cliente.getCpf(),TipoConta.CORRENTE).orElseThrow();
        assertAll(
                () -> assertEquals(conta.getSaldo(), resultado),
                () -> assertEquals(1, extratoRepository.verExtratos(cliente.getCpf()).size())
        );
    }

    @Test
    void deveLancarSaldoInsuficienteExceptionRealizarMovimentacaoDeSaque() throws Exception{
        clienteRepository.save(cliente);
        var valor = new BigDecimal("600")
                .setScale(2, RoundingMode.HALF_EVEN);
        contaRepository.save(contaCorrente);
        mockMvc.perform(post("/api/contas/saque")
                .with(user(cliente.getCpf()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ValorRequest(valor))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        Conta conta = contaRepository.findByCpf(cliente.getCpf(),TipoConta.CORRENTE).orElseThrow();
        assertAll(
                () -> assertEquals(BigDecimal.ZERO, conta.getSaldo()),
                () -> assertEquals(0, extratoRepository.verExtratos(cliente.getCpf()).size())
        );
    }

    @Test
    void deveRealizarMovimentacaoDeDeposito() throws Exception{
        clienteRepository.save(cliente);
        contaRepository.save(contaCorrente);
        var valor = new BigDecimal("600")
                .setScale(2, RoundingMode.HALF_EVEN);
        mockMvc.perform(post("/api/contas/deposito")
                .with(user(cliente.getCpf()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ValorRequest(valor))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Depósito realizado com sucesso!"));
        Conta conta = contaRepository.findByCpf(cliente.getCpf(),TipoConta.CORRENTE).orElseThrow();
        assertAll(
                () -> assertEquals(conta.getSaldo(), valor),
                () -> assertEquals(1, extratoRepository.verExtratos(cliente.getCpf()).size())
        );
    }

    @Test
    void deveLancarValorInvalidoExceptionRealizarMovimentacaoDeDeposito() throws Exception{
        clienteRepository.save(cliente);
        contaRepository.save(contaCorrente);
        var valor = new BigDecimal("-600")
                .setScale(2, RoundingMode.HALF_EVEN);
        mockMvc.perform(post("/api/contas/deposito")
                .with(user(cliente.getCpf()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ValorRequest(valor))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        Conta conta = contaRepository.findByCpf(cliente.getCpf(),TipoConta.CORRENTE).orElseThrow();
        assertAll(
                () -> assertEquals(BigDecimal.ZERO, conta.getSaldo()),
                () -> assertEquals(0, extratoRepository.verExtratos(cliente.getCpf()).size())
        );
    }
    @Test
    void deveLancarContaNaoEncontradaExceptionAoRealizarMovimentacao() throws Exception{
        var valor = new BigDecimal("600")
                .setScale(2, RoundingMode.HALF_EVEN);
        mockMvc.perform(post("/api/contas/deposito")
                .with(user(cliente.getCpf()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ValorRequest(valor))))
                .andExpect(status().isNotFound());
    }
    @Test
    void deveRealizarTransferencia() throws Exception{
        clienteRepository.save(cliente);
        clienteRepository.save(cliente2);
        var deposito = new BigDecimal("1600");
        contaCorrente.depositar(deposito);
        contaRepository.save(contaCorrente);
        contaRepository.save(contaCorrente2);
        var valor = new BigDecimal("600")
                .setScale(2, RoundingMode.HALF_EVEN);
        mockMvc.perform(post("/api/transferencia")
                .with(user(cliente.getCpf()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(
                                        new TransferenciaRequest(
                                                cliente2.getCpf(),
                                                valor,
                                                TipoMovimentacao.PIX))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Transferencia realizada com sucesso!"));
        Conta conta1 = contaRepository.findByCpf(cliente.getCpf(),TipoConta.CORRENTE).orElseThrow();
        Conta conta2 = contaRepository.findByCpf(cliente2.getCpf(),TipoConta.CORRENTE).orElseThrow();
        assertAll(
                () -> assertEquals(deposito.subtract(valor), conta1.getSaldo()),
                () -> assertEquals(valor, conta2.getSaldo()),
                () -> assertEquals(1, extratoRepository.verExtratos(cliente.getCpf()).size()),
                () -> assertEquals(1, extratoRepository.verExtratos(cliente2.getCpf()).size())
        );
    }
    @Test
    void deveLancarContaExceptionAoRealizarTransferencia() throws Exception{
        clienteRepository.save(cliente);
        var deposito = new BigDecimal("1600");
        contaCorrente.depositar(deposito);
        contaRepository.save(contaCorrente);
        var valor = new BigDecimal("600")
                .setScale(2, RoundingMode.HALF_EVEN);
        mockMvc.perform(post("/api/transferencia")
                .with(user(cliente.getCpf()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(
                                        new TransferenciaRequest(
                                                cliente.getCpf(),
                                                valor,
                                                TipoMovimentacao.PIX))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        Conta conta = contaRepository.findByCpf(cliente.getCpf(),TipoConta.CORRENTE).orElseThrow();
        assertAll(
                () -> assertEquals(deposito.setScale(2,RoundingMode.UNNECESSARY), conta.getSaldo()),
                () -> assertEquals(0, extratoRepository.verExtratos(cliente.getCpf()).size())
        );
    }

    @Test
    void deveLancarMovimentacaoExceptionAoRealizarTransferencia() throws Exception{
        clienteRepository.save(cliente);
        clienteRepository.save(cliente2);
        var deposito = new BigDecimal("6000");
        contaCorrente.depositar(deposito);
        contaRepository.save(contaCorrente);
        contaRepository.save(contaCorrente2);
        var valor = new BigDecimal("5000")
                .setScale(2, RoundingMode.HALF_EVEN);
        mockMvc.perform(post("/api/transferencia")
                .with(user(cliente.getCpf()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(
                                        new TransferenciaRequest(
                                                cliente2.getCpf(),
                                                valor,
                                                TipoMovimentacao.DOC))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        Conta conta1 = contaRepository.findByCpf(cliente.getCpf(),TipoConta.CORRENTE).orElseThrow();
        Conta conta2 = contaRepository.findByCpf(cliente2.getCpf(),TipoConta.CORRENTE).orElseThrow();
        assertAll(
                () -> assertEquals(deposito.setScale(2, RoundingMode.UNNECESSARY), conta1.getSaldo()),
                () -> assertEquals(BigDecimal.ZERO, conta2.getSaldo()),
                () -> assertEquals(0, extratoRepository.verExtratos(cliente.getCpf()).size()),
                () -> assertEquals(0, extratoRepository.verExtratos(cliente2.getCpf()).size())
        );
    }

    @Test
    void deveRealizarAplicacaoNaContaInvestimento() throws Exception{
        clienteRepository.save(cliente);
        var deposito = new BigDecimal("1600");
        contaCorrente.depositar(deposito);
        contaRepository.save(contaCorrente);
        contaRepository.save(contaInvestimento);
        var valor = new BigDecimal("600")
                .setScale(2, RoundingMode.HALF_EVEN);
        mockMvc.perform(post("/api/transferencia")
                .with(user(cliente.getCpf()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(
                                        new TransferenciaRequest(
                                                cliente.getCpf(),
                                                valor,
                                                TipoMovimentacao.APLICACAO))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Transferencia realizada com sucesso!"));
        Conta conta1 = contaRepository.findByCpf(cliente.getCpf(),TipoConta.CORRENTE).orElseThrow();
        Conta conta2 = contaRepository.findByCpf(cliente.getCpf(),TipoConta.INVESTIMENTO).orElseThrow();
        assertAll(
                () -> assertEquals(deposito.subtract(valor), conta1.getSaldo()),
                () -> assertEquals(valor, conta2.getSaldo()),
                () -> assertEquals(3, extratoRepository.verExtratos(cliente.getCpf()).size())
        );
    }
    @Test
    void deveRealizarResgateNaContaInvestimento() throws Exception{
        clienteRepository.save(cliente);
        var deposito = new BigDecimal("6000");
        contaInvestimento.depositar(deposito);
        contaRepository.save(contaCorrente);
        contaRepository.save(contaInvestimento);
        transferenciaRepository.saveAll(List.of(

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
        ));
        var valor = new BigDecimal("4300")
                .setScale(2, RoundingMode.HALF_EVEN);
        mockMvc.perform(post("/api/transferencia")
                .with(user(cliente.getCpf()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(
                                        new TransferenciaRequest(
                                                cliente.getCpf(),
                                                valor,
                                                TipoMovimentacao.RESGATE))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mensagem").value("Transferencia realizada com sucesso!"));
        Conta conta1 = contaRepository.findByCpf(cliente.getCpf(),TipoConta.CORRENTE).orElseThrow();
        assertAll(
                () -> assertEquals(valor, conta1.getSaldo()),
                () -> assertEquals(6, extratoRepository.verExtratos(cliente.getCpf()).size())
        );
    }
    @Test
    void deveLancarAplicacaoExceptionAoRealizarResgateNaContaInvestimento() throws Exception{
        clienteRepository.save(cliente);
        var deposito = new BigDecimal("6000");
        contaInvestimento.depositar(deposito);
        contaRepository.save(contaCorrente);
        contaRepository.save(contaInvestimento);
        transferenciaRepository.saveAll(List.of(

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
                        new BigDecimal("1000.00"),
                        hoje.minusDays(17),
                        hoje.minusDays(2),
                        new BigDecimal("0.00042"),
                        TipoMovimentacao.APLICACAO,
                        DirecaoMovimentacao.SAIDA
                )
        ));
        var valor = new BigDecimal("4300")
                .setScale(2, RoundingMode.HALF_EVEN);
        mockMvc.perform(post("/api/transferencia")
                .with(user(cliente.getCpf()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper
                                .writeValueAsString(
                                        new TransferenciaRequest(
                                                cliente.getCpf(),
                                                valor,
                                                TipoMovimentacao.RESGATE))))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        Conta conta = contaRepository.findByCpf(cliente.getCpf(),TipoConta.CORRENTE).orElseThrow();
        extratoRepository.verExtratos(cliente.getCpf()).forEach(System.out::println);
        assertAll(
                () -> assertEquals(BigDecimal.ZERO, conta.getSaldo()),
                () -> assertEquals(4, extratoRepository.verExtratos(cliente.getCpf()).size())
        );
    }
}
