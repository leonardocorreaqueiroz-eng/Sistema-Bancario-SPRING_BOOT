package com.org.Sistema_Bancario.SpringBoot.integration.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.Sistema_Bancario.SpringBoot.dto.CadastroRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.LoginRequest;
import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.DirecaoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.RegrasDeBanco;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Aplicacao;
import com.org.Sistema_Bancario.SpringBoot.repository.ContaRepository;
import com.org.Sistema_Bancario.SpringBoot.repository.TransferenciaRepository;
import com.org.Sistema_Bancario.SpringBoot.service.ContaService;
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
import java.util.List;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class ContaControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ContaService contaService;
    @Autowired
    private ContaRepository contaRepository;
    @Autowired
    private TransferenciaRepository transferenciaRepository;
    private ObjectMapper objectMapper;
    private Cliente cliente;
    private CadastroRequest cadastroRequest;
    private LoginRequest loginRequest;
    @BeforeEach
    void setUp() {
        cliente =
                new Cliente("Patrício Silva Rodriguete",
                        "12345678909",
                        "192292");
        cadastroRequest = new CadastroRequest(
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getPassword()
        );
        loginRequest = new LoginRequest(
                cliente.getCpf(),
                cliente.getPassword()
        );
        objectMapper = new ObjectMapper();
    }
    @Test
    void deveCadastrarConta() throws Exception {
        mockMvc.perform(post("/api/criarConta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cadastroRequest)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Conta criada com sucesso!"));
    }
    @Test
    void deveLancarCpfInvalidoExceptionAoCadastrarConta() throws Exception {
        Cliente cliente1 =
                new Cliente("Patrício Silva Rodriguete",
                        "12345678908",
                        "192292");
        CadastroRequest cadastroRequest1 = new CadastroRequest(
                cliente1.getNome(),
                cliente1.getCpf(),
                cliente1.getPassword()
        );
        mockMvc.perform(post("/api/criarConta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cadastroRequest1)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    @Test
    void deveLancarClienteJaCadastradoExceptionAoCadastrarConta() throws Exception {
        contaService.criarConta(cadastroRequest);
        mockMvc.perform(post("/api/criarConta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cadastroRequest)))
                .andExpect(status().isConflict())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    @Test
    void deveAcessarConta() throws Exception {
        contaService.criarConta(cadastroRequest);
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));

    }
    @Test
    void deveLancarBadCredentialsExceptionAoCadastrarConta() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
    @Test
    void deveListarContas() throws Exception {
        contaService.criarConta(cadastroRequest);
        mockMvc.perform(get("/api/contas")
                        .with(user(cliente.getCpf())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].tipo").value(TipoConta.CORRENTE.name()))
                .andExpect(jsonPath("$[1].tipo").value(TipoConta.INVESTIMENTO.name()));
    }
    @Test
    void deveLancarContaNaoEncontradaExceptionListarContas() throws Exception {
        mockMvc.perform(get("/api/contas")
                        .with(user(cliente.getCpf())))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void deveMostrarSaldoNaContaCorrente() throws Exception {
        contaService.criarConta(cadastroRequest);
        Conta contaCorrente = contaRepository.findByCpf(cliente.getCpf(),TipoConta.CORRENTE)
                .orElseThrow();
        contaCorrente.depositar(new BigDecimal("1600"));
        mockMvc.perform(get("/api/conta/saldo?tipo=CORRENTE")
                .with(user(cliente.getCpf())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(contaCorrente.getSaldo()
                        .setScale(1, RoundingMode.UNNECESSARY)));
    }
    @Test
    void deveLancarContaNaoEncontradaExceptionAoMostrarSaldoNaContaCorrente() throws Exception {
        mockMvc.perform(get("/api/conta/saldo?tipo=CORRENTE")
                .with(user(cliente.getCpf())))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    void deveMostrarSaldoNaContaInvestimento() throws Exception {
        contaService.criarConta(cadastroRequest);
        Conta contaInvestimento = contaRepository.findByCpf(cliente.getCpf(),TipoConta.INVESTIMENTO)
                .orElseThrow();
        contaInvestimento.depositar(new BigDecimal("1600"));
        transferenciaRepository.saveAll(List.of(
                new Aplicacao(
                        contaInvestimento,
                        new BigDecimal("1600"),
                        LocalDate.now().minusDays(10),
                        LocalDate.now().minusDays(10),
                        RegrasDeBanco.TAXA_INVESTIMENTO,
                        TipoMovimentacao.APLICACAO,
                        DirecaoMovimentacao.SAIDA
                ),
                new Aplicacao(
                        contaInvestimento,
                        new BigDecimal("1600"),
                        LocalDate.now().minusDays(10),
                        LocalDate.now().minusDays(10),
                        RegrasDeBanco.TAXA_INVESTIMENTO,
                        TipoMovimentacao.APLICACAO,
                        DirecaoMovimentacao.SAIDA
                ))
        );
        mockMvc.perform(get("/api/conta/saldo?tipo=INVESTIMENTO")
                .with(user(cliente.getCpf())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").value(contaInvestimento.getSaldo()));
    }
    @Test
    void deveLancarAplicacaoNaoEncontradaExceptionMostrarSaldoNaContaInvestimento() throws Exception {
        contaService.criarConta(cadastroRequest);
        Conta contaInvestimento = contaRepository.findByCpf(cliente.getCpf(),TipoConta.INVESTIMENTO)
                .orElseThrow();
        contaInvestimento.depositar(new BigDecimal("1600"));
        mockMvc.perform(get("/api/conta/saldo?tipo=INVESTIMENTO")
                .with(user(cliente.getCpf())))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
