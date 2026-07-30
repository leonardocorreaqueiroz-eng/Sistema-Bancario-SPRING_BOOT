package com.org.Sistema_Bancario.SpringBoot.service;

import com.org.Sistema_Bancario.SpringBoot.dto.CadastroRequest;
import com.org.Sistema_Bancario.SpringBoot.dto.ContaResponse;
import com.org.Sistema_Bancario.SpringBoot.dto.LoginRequest;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ClienteJaCadastradoException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ContaNaoEncontradaException;
import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.repository.ClienteRepository;
import com.org.Sistema_Bancario.SpringBoot.repository.ContaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;

@Service
public class ContaService {

    private static final Logger log =
            LoggerFactory.getLogger(ContaService.class);
    private final ClienteRepository clienteRepository;
    private final ContaRepository contaRepository;
    private final RendimentoService rendimentoService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager auth;
    private final JWTService jwtService;

    public  ContaService(ClienteRepository clienteRepository,
                         ContaRepository contaRepository,
                         RendimentoService rendimentoService,
                         AuthenticationManager auth,
                         JWTService jwtService,
                         PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.contaRepository = contaRepository;
        this.rendimentoService = rendimentoService;
        this.auth = auth;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void criarConta(CadastroRequest cadastro) {
        String nome = cadastro.nome();
        String cpf = cadastro.cpf();
        cpfExiste(cpf);
        Cliente cliente = new Cliente(nome,
                cpf,cadastro.password());
        cliente.setPassword(passwordEncoder.encode(cliente.getPassword()));
        Conta contaCorrente = new Conta(cliente, TipoConta.CORRENTE,LocalDate.now());
        Conta contaInvestimento = new Conta(cliente,TipoConta.INVESTIMENTO,LocalDate.now());
        clienteRepository.save(cliente);
        contaRepository.save(contaCorrente);
        contaRepository.save(contaInvestimento);
        log.info("Conta criada com sucesso para o CPF {}.", cpf);    }

    private void cpfExiste(String cpf) {

        if (!contaRepository.findContasByCpf(cpf).isEmpty()){
            log.warn("Tentativa de cadastro com CPF já existente: {}.", cpf);
            throw new ClienteJaCadastradoException("CPF já cadastrado!");
        }

    }

    public String verify(LoginRequest cliente) {
        auth.authenticate(
                new UsernamePasswordAuthenticationToken(
                        cliente.cpf(), cliente.senha()));
        log.info("Login realizado com sucesso para o CPF {}.", cliente.cpf());
        return jwtService.generateToken(cliente.cpf());
    }

    public List<ContaResponse> listarContas(String cpf) {
        List<Conta> contas = contaRepository.findContasByCpf(cpf);
        if (contas.isEmpty()) {
            log.warn("Contas não encontradas no CPF {}.", cpf);
            throw new ContaNaoEncontradaException();
        }
        log.info("Listagem de contas realizada para o CPF {}.", cpf);
        return contas.stream().map(c ->
                new ContaResponse(c.getNumero(),c.getSaldo(),c.getTipoConta())).toList();
    }

    public BigDecimal saldoConta(String cpf,TipoConta tipo){
        Conta conta = contaRepository.findByCpf(cpf,tipo)
                .orElseThrow(() -> {
                   log.warn("Conta não encontrada no CPF {}.", cpf);
                   return new ContaNaoEncontradaException();
                });
        if (TipoConta.INVESTIMENTO.equals(conta.getTipoConta())){
            if (conta.getSaldo().compareTo(BigDecimal.ZERO) > 0){
                log.debug("Aplicando rendimentos da conta {}.",
                        conta.getNumero());
                rendimentoService.aplicarRendimentos(conta);
            }
        }
        return conta.getSaldo();
    }

}