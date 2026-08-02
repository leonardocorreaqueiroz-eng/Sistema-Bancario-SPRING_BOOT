package com.org.Sistema_Bancario.SpringBoot.unit.repository;

import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.repository.ClienteRepository;
import com.org.Sistema_Bancario.SpringBoot.repository.ContaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.Optional;

@DataJpaTest
class ContaRepositoryTest {

    @Autowired
    private ClienteRepository clienteRepository;
    @Autowired
    private ContaRepository contaRepository;

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
    void findByCpfDeveRetornarUmaConta() {
        clienteRepository.save(cliente);
        contaRepository.save(contaCorrente);
        contaRepository.save(contaInvestimento);
        Optional<Conta> corrente = Optional.of(contaCorrente);
        Optional<Conta> investimento = Optional.of(contaInvestimento);

        assertAll(
                () -> assertEquals(corrente,contaRepository.findByCpf(cliente.getCpf(), TipoConta.CORRENTE)),
                () -> assertEquals(investimento,contaRepository.findByCpf(cliente.getCpf(), TipoConta.INVESTIMENTO))
        );
    }

    @Test
    void findByCpfDeveRetornarUmaListaDeDuasContas() {
        clienteRepository.save(cliente);
        contaRepository.save(contaCorrente);
        contaRepository.save(contaInvestimento);
        assertAll(
                () -> assertEquals(2,contaRepository
                        .findContasByCpf(cliente.getCpf()).size()),
                () -> assertTrue(contaRepository
                        .findContasByCpf(cliente.getCpf())
                        .contains(contaCorrente)),
                () -> assertTrue(contaRepository
                        .findContasByCpf(cliente.getCpf())
                        .contains(contaInvestimento))
        );
    }
}