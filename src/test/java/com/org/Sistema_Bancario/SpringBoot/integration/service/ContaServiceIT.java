package com.org.Sistema_Bancario.SpringBoot.integration.service;

import com.org.Sistema_Bancario.SpringBoot.dto.CadastroRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.LoginRequest;
import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.transaction.annotation.Transactional;
import com.org.Sistema_Bancario.SpringBoot.service.ContaService;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@Transactional
public class ContaServiceIT {
    @Autowired
    private ContaService contaService;
    private CadastroRequest cadastroRequest;
    private Cliente cliente;

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
    }

    @Test
    @DisplayName("Deve criar as contas e retornar saldo inicial zerado")
    void deveCriarContaEListarContasComSaldoInicialZerado()
    {
        contaService.criarConta(cadastroRequest);
        var lista =  contaService.listarContas(cliente.getCpf());
        var saldoDaContaCorrente = contaService
                .saldoConta(cliente.getCpf(), TipoConta.CORRENTE);
        var saldoDaContaInvestimento = contaService
                .saldoConta(cliente.getCpf(), TipoConta.INVESTIMENTO);
        assertAll(
                () -> assertTrue(lista.stream().anyMatch(
                        c -> c.tipo() == TipoConta.CORRENTE)),
                () -> assertTrue(lista.stream().anyMatch(
                        c -> c.tipo() == TipoConta.INVESTIMENTO)),
                () -> assertEquals(2, lista.size()),
                () -> assertEquals(BigDecimal.ZERO,saldoDaContaCorrente
                        ),
                () -> assertEquals(BigDecimal.ZERO,saldoDaContaInvestimento)

        );
    }
    @Test
    @DisplayName("Deve realizar login")
    void deveRealizarLogin()
    {
        contaService.criarConta(cadastroRequest);
        LoginRequest loginRequest = new LoginRequest(
                cliente.getCpf(),
                cliente.getPassword()
        );
        String token = contaService.verify(loginRequest);

        assertNotNull(token);
        assertFalse(token.isBlank());
    }
    @Test
    @DisplayName("Não deve realizar login com senha inválida")
    void naoDeveRealizarLoginComSenhaInvalida() {

        contaService.criarConta(cadastroRequest);

        LoginRequest loginRequest = new LoginRequest(
                cliente.getCpf(),
                "senhaErrada"
        );

        assertThrows(
                BadCredentialsException.class,
                () -> contaService.verify(loginRequest)
        );
    }
}
