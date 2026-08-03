package com.org.Sistema_Bancario.SpringBoot.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.Sistema_Bancario.SpringBoot.controller.ContaController;
import com.org.Sistema_Bancario.SpringBoot.dto.ContaResponse;
import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.service.ContaService;
import com.org.Sistema_Bancario.SpringBoot.service.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(ContaController.class)
class ContaControllerSecurityTest {

    @MockitoBean
    private ContaService contaService;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private JWTService jwtService;

    private Cliente cliente;
    private Conta contaCorrente;
    private Conta contaInvestimento;
    private ObjectMapper objectMapper;

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
        objectMapper = new ObjectMapper();
    }

    @Test
    void listarContasDeveRetornarOk_E_UmaLista() throws Exception {
        List<ContaResponse> lista = Stream.of(contaCorrente, contaInvestimento)
                .map(c -> new ContaResponse(
                        c.getNumero(),c.getSaldo(),c.getTipoConta())).toList();
        when(contaService.listarContas(cliente.getCpf()))
                .thenReturn(lista);
        mockMvc.perform(get("/api/contas")
                        .with(user(cliente.getCpf())))
        .andExpect(status().isOk())
        .andExpect(content().contentType(MediaType.APPLICATION_JSON))
        .andExpect(content().string(objectMapper.writeValueAsString(lista)));
    }

    @Test
    void saldoContaDeveRetornarSucesso_E_UmaSaldo() throws Exception {
        contaCorrente.depositar(new BigDecimal("400"));
        when(contaService.saldoConta(cliente.getCpf(), TipoConta.CORRENTE))
                .thenReturn(contaCorrente.getSaldo());
        mockMvc.perform(get("/api/conta/saldo")
                        .param("tipo", TipoConta.CORRENTE.name())
                        .with(user(cliente.getCpf())))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.saldo").value(contaCorrente.getSaldo()
                        .setScale(1, RoundingMode.UNNECESSARY)));
    }
}