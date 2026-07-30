package com.org.Sistema_Bancario.SpringBoot.unit.controller;

import com.org.Sistema_Bancario.SpringBoot.dto.MovimentacaoResponse;
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
import com.org.Sistema_Bancario.SpringBoot.service.ExtratoService;
import com.org.Sistema_Bancario.SpringBoot.service.JWTService;
import com.org.Sistema_Bancario.SpringBoot.service.MovimentacaoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;


import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(MovimentacaoController.class)
class MovimentacaoControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private MovimentacaoService movimentacaoService;
    @MockitoBean
    private ExtratoService extratoService;
    @MockitoBean
    private JWTService jwtService;


    private Cliente cliente;
    private List<Movimentacao> movimentacoes;

    @BeforeEach
    void setUp() {
        cliente =
                new Cliente("Patrício Silva Rodriguete",
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

        LocalDate hoje = LocalDate.of(2026, 7, 23);
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
    }

    @Test
    void listarMovimentacoes() throws Exception {
        List<MovimentacaoResponse> movimentacaoResponseList =
                movimentacoes.stream().map(MovimentacaoResponse::new).toList();
        when(extratoService.verExtrato(cliente.getCpf()))
                .thenReturn(movimentacaoResponseList);
        mockMvc.perform(get("/api/contas/verExtrato")
                .with(user(cliente.getCpf())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(8));
    }

    @Test
    void saque() throws Exception {
        mockMvc.perform(post("/api/contas/saque")
                        .with(user(cliente.getCpf()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "valor": 400
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Saque realizado com sucesso!"));

        verify(movimentacaoService).realizarMovimentacao(
                cliente.getCpf(),
                new BigDecimal("400"),
                TipoMovimentacao.SAQUE
        );
    }

    @Test
    void deposito() throws Exception {
        mockMvc.perform(post("/api/contas/deposito")
                        .with(user(cliente.getCpf()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "valor": 400
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Depósito realizado com sucesso!"));

        verify(movimentacaoService).realizarMovimentacao(
                cliente.getCpf(),
                new BigDecimal("400"),
                TipoMovimentacao.DEPOSITO
        );
    }

    @Test
    void transferencia() throws Exception {
        mockMvc.perform(post("/api/transferencia")
                .with(csrf())
                .with(user(cliente.getCpf()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "destino": "12345678906",
                                    "valor": "1600",
                                    "tipo": "PIX"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(content().string("Transferencia realizada com sucesso!"));
        verify(movimentacaoService).transferir(
                cliente.getCpf(),
                "12345678906",
                new BigDecimal("1600"),
                TipoMovimentacao.PIX,
                TipoConta.CORRENTE,TipoConta.CORRENTE
        );
    }
}