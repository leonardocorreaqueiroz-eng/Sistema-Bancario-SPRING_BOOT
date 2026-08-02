package com.org.Sistema_Bancario.SpringBoot.unit.service;

import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import com.org.Sistema_Bancario.SpringBoot.model.ClienteDetails;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.repository.ContaRepository;
import com.org.Sistema_Bancario.SpringBoot.service.ClienteDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class ClienteDetailsServiceTest {

    @InjectMocks
    private ClienteDetailsService clienteDetailsService;
    @Mock
    private ContaRepository contaRepository;

    private Cliente cliente;
    private Conta conta;

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
    }

    @Test
    void loadUserByUsernameDeveCarregarUsuarioPeloCpf() {

        when(contaRepository.findByCpf(cliente.getCpf(),TipoConta.CORRENTE))
                .thenReturn(Optional.of(conta));
        UserDetails user =  clienteDetailsService.loadUserByUsername(cliente.getCpf());
        assertAll(
                () -> assertInstanceOf(ClienteDetails.class,user),
                () -> assertEquals(cliente.getCpf(),user.getUsername()),
                () -> assertEquals(cliente.getPassword(), user.getPassword())
        );
    }
    @Test
    void loadUserByUsernameDeveLancarUserNameNotFoundException()
    {
        when(contaRepository.findByCpf(cliente.getCpf(),TipoConta.CORRENTE))
                .thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class,
                () -> clienteDetailsService
                        .loadUserByUsername(cliente.getCpf()));
    }

}