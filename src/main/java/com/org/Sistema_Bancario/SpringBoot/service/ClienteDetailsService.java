package com.org.Sistema_Bancario.SpringBoot.service;

import com.org.Sistema_Bancario.SpringBoot.model.ClienteDetails;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.repository.ContaRepository;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class ClienteDetailsService implements UserDetailsService {

    private static final Logger log =
            LoggerFactory.getLogger(ClienteDetailsService.class);
    private final ContaRepository contaRepository;

    public  ClienteDetailsService(ContaRepository contaRepository) {
        this.contaRepository = contaRepository;
    }

    @Override
    @NonNull
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        Conta conta = contaRepository.findByCpf(username, TipoConta.CORRENTE)
                .orElseThrow(() -> {
                    log.warn("Tentativa de autenticação para CPF inexistente: {}", username);
                    return new UsernameNotFoundException(username);
                });
        return new ClienteDetails(conta.getCliente());
    }
}
