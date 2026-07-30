package com.org.Sistema_Bancario.SpringBoot.service;
import com.org.Sistema_Bancario.SpringBoot.dto.MovimentacaoResponse;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ExtratoVazioException;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Movimentacao;
import com.org.Sistema_Bancario.SpringBoot.repository.ExtratoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExtratoService {

    private static final Logger log =
            LoggerFactory.getLogger(ExtratoService.class);
    private final ExtratoRepository extratoRepository;

    public ExtratoService(ExtratoRepository extratoRepository) {
        this.extratoRepository = extratoRepository;
    }

    public List<MovimentacaoResponse> verExtrato(String cpf) {
        List<Movimentacao> listaMovimentacao = extratoRepository.verExtratos(cpf);
        if (listaMovimentacao.isEmpty()) {
            log.warn("Nenhum extrato encontrado para o CPF {}.", cpf);
            throw new ExtratoVazioException();
        }
        return listaMovimentacao.stream()
                .map(MovimentacaoResponse::new).toList();
    }
}
