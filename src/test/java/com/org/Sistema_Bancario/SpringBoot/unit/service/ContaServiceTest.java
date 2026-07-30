package com.org.Sistema_Bancario.SpringBoot.unit.service;

import com.org.Sistema_Bancario.SpringBoot.dto.CadastroRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.ContaResponse;
import com.org.Sistema_Bancario.SpringBoot.dto.LoginRequest;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ClienteJaCadastradoException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ContaException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ContaNaoEncontradaException;
import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.repository.ClienteRepository;
import com.org.Sistema_Bancario.SpringBoot.repository.ContaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private ContaRepository contaRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager auth;
    @Mock
    private JWTService jwtService;
    @Mock
    private RendimentoService rendimentoService;

    @InjectMocks
    private ContaService contaService;
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
    }

    @Test
    void criarContaDeveCriarDuasContas() {
        CadastroRequest cadastro = new CadastroRequest(
                cliente.getNome(),
                cliente.getCpf(),cliente.getPassword());
        when(passwordEncoder.encode(any()))
                .thenReturn("senhaCriptografada");
        contaService.criarConta(cadastro);
        ArgumentCaptor<Cliente> clienteCaptor =
                ArgumentCaptor.forClass(Cliente.class);
        ArgumentCaptor<Conta> contaCaptor =
                ArgumentCaptor.forClass(Conta.class);
        verify(passwordEncoder).encode(cadastro.password());
        verify(clienteRepository)
                .save(clienteCaptor.capture());
        verify(contaRepository, Mockito.times(2))
                .save(contaCaptor.capture());
        Cliente capturedCliente = clienteCaptor.getValue();
        List<Conta> capturedContas = contaCaptor.getAllValues();
        assertAll(
                () -> assertEquals(capturedCliente.getCpf(),cliente.getCpf()),
                () -> assertEquals("senhaCriptografada", capturedCliente.getPassword()),
                () -> assertTrue(capturedContas.stream()
                        .anyMatch(c -> c.getTipoConta() == TipoConta.CORRENTE)),
                () -> assertTrue(capturedContas.stream()
                        .anyMatch(c -> c.getTipoConta() == TipoConta.INVESTIMENTO))
        );
    }

    @Test
    void criarContaDeveRetornarContaException(){
        when(contaRepository
                .findContasByCpf(cliente.getCpf()))
                .thenReturn(List.of(contaCorrente,
                        contaInvestimento));
        assertThrows(ClienteJaCadastradoException.class,
                () -> contaService.criarConta(
                        new CadastroRequest(
                                cliente.getNome(),
                                cliente.getCpf(),
                                cliente.getPassword()))
        );
        verify(clienteRepository, never()).save(any());
        verify(contaRepository, never()).save(any());
    }

    @Test
    void verifyDeveAutentificarOClienteERetornarUmToken() {
        LoginRequest login = new LoginRequest(
                cliente.getCpf(),
                cliente.getPassword()
        );
        String token = "jwt-token";
        when(jwtService.generateToken(login.cpf()))
                .thenReturn(token);
        String resultado = contaService.verify(login);

        assertEquals(token, resultado);
        verify(auth).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(login.cpf());
    }

    @Test
    void verifyDeveLancarExcecaoQuandoCredenciaisForemInvalidas() {

        LoginRequest login = new LoginRequest(
                "52998224725",
                "senhaErrada"
        );

        doThrow(new BadCredentialsException("Credenciais inválidas"))
                .when(auth)
                .authenticate(any(UsernamePasswordAuthenticationToken.class));

        assertThrows(
                BadCredentialsException.class,
                () -> contaService.verify(login)
        );

        verify(jwtService, never()).generateToken(anyString());
    }

    @Test
    void listarContasDeveRetornarContasDoCliente() {

        ContaResponse conta1 = new ContaResponse(
                contaCorrente.getNumero(),
                contaCorrente.getSaldo(),
                contaCorrente.getTipoConta());
        ContaResponse conta2 = new ContaResponse(
          contaInvestimento.getNumero(),
          contaInvestimento.getSaldo(),
          contaInvestimento.getTipoConta()
        );
        when(contaRepository.findContasByCpf(cliente.getCpf()))
                .thenReturn(List.of(contaCorrente,contaInvestimento));
        List<ContaResponse> contas = List.of(
                conta1, conta2);
        assertEquals(contas,
                contaService.listarContas(cliente.getCpf()));
        verify(contaRepository).findContasByCpf(cliente.getCpf());
    }

    @Test
    void listarContasDeveRetornarContaException(){
        when(contaRepository.findContasByCpf(cliente.getCpf()))
                .thenReturn(Collections.emptyList());

        assertThrows(ContaNaoEncontradaException.class,
                () -> contaService.listarContas(cliente.getCpf()));
        verify(contaRepository)
                .findContasByCpf(cliente.getCpf());
    }


    @Test
    void saldoContaDeveRetornarSaldoDaContaCorrenteOuInvestimento() {

        contaCorrente.depositar(new BigDecimal("100"));
        contaInvestimento.depositar(new BigDecimal("200"));

        when(contaRepository
                .findByCpf(cliente.getCpf(),TipoConta.CORRENTE))
                .thenReturn(Optional.ofNullable(contaCorrente));
        when(contaRepository
                .findByCpf(cliente.getCpf(),TipoConta.INVESTIMENTO))
                .thenReturn(Optional.ofNullable(contaInvestimento));
        assertAll(
                () -> assertEquals(contaCorrente.getSaldo(),
                        contaService.saldoConta(cliente.getCpf(),TipoConta.CORRENTE)),
                () -> assertEquals(contaInvestimento.getSaldo(),
                        contaService.saldoConta(cliente.getCpf(),TipoConta.INVESTIMENTO))
        );
        verify(contaRepository)
                .findByCpf(cliente.getCpf(), TipoConta.CORRENTE);

        verify(contaRepository)
                .findByCpf(cliente.getCpf(), TipoConta.INVESTIMENTO);

    }

    @Test
    void saldoContaNaoDeveAplicarRendimentoQuandoSaldoForZero() {

        when(contaRepository.findByCpf(cliente.getCpf(), TipoConta.INVESTIMENTO))
                .thenReturn(Optional.of(contaInvestimento));

        BigDecimal saldo = contaService.saldoConta(
                cliente.getCpf(),
                TipoConta.INVESTIMENTO);

        assertEquals(BigDecimal.ZERO, saldo);

        verify(rendimentoService, never())
                .aplicarRendimentos(any());
    }

    @Test
    void saldoContaDeveLancarContaNaoEncontradaException(){
        when(contaRepository.findByCpf(anyString(), any()))
                .thenReturn(Optional.empty());

        assertThrows(
                ContaException.class,
                () -> contaService.saldoConta(
                        cliente.getCpf(),
                        TipoConta.CORRENTE
                )
        );
    }
}