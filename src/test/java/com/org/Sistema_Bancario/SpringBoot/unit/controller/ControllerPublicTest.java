package com.org.Sistema_Bancario.SpringBoot.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.org.Sistema_Bancario.SpringBoot.controller.ContaController;
import com.org.Sistema_Bancario.SpringBoot.dto.CadastroRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.LoginRequest;
import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import com.org.Sistema_Bancario.SpringBoot.service.ContaService;
import com.org.Sistema_Bancario.SpringBoot.service.JWTService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContaController.class)
@AutoConfigureMockMvc(addFilters = false)
class ControllerPublicTest {

    @MockitoBean
    private ContaService contaService;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private JWTService jwtService;

    private ObjectMapper objectMapper;
    private LoginRequest loginRequest;
    private CadastroRequest cadastroRequest;
    @BeforeEach
    void setUp() {
        Cliente cliente = new Cliente("Patrício Silva Rodriguete",
                "12345678909",
                "192292");

        loginRequest = new LoginRequest(
                cliente.getCpf(),
                cliente.getPassword()
        );
        cadastroRequest = new CadastroRequest(
                cliente.getNome(),
                cliente.getCpf(),
                cliente.getPassword()
        );
        objectMapper = new ObjectMapper();
    }


    @Test
    void acessarConta() throws Exception {
        when(contaService.verify(loginRequest))
                .thenReturn("token");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void cadastroDaConta() throws Exception {
        mockMvc.perform(post("/api/criarConta")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cadastroRequest)))
                .andExpect(status().isCreated());
    }
}