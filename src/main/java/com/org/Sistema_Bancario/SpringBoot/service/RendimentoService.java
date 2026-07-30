package com.org.Sistema_Bancario.SpringBoot.service;

import com.org.Sistema_Bancario.SpringBoot.exceptions.AplicacaoNaoEncontradaException;
import com.org.Sistema_Bancario.SpringBoot.model.DirecaoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.StatusAplicacao;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.repository.AplicacaoRepository;
import com.org.Sistema_Bancario.SpringBoot.repository.TransferenciaRepository;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Aplicacao;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Rendimentos;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;


@Service
public class RendimentoService {
    private static final Logger log =
            LoggerFactory.getLogger(RendimentoService.class);

    private final AplicacaoRepository aplicacaoRepository;
    private final TransferenciaRepository transferenciaRepository;

    public RendimentoService(AplicacaoRepository aplicacaoRepository,
                             TransferenciaRepository transferenciaRepository) {
        this.aplicacaoRepository = aplicacaoRepository;
        this.transferenciaRepository = transferenciaRepository;
    }

    @Transactional
    public void aplicarRendimentos(Conta conta){
        log.info("Iniciando aplicação de rendimentos na conta {}", conta.getNumero());

        List<Aplicacao> aplicacoes = aplicacaoRepository.listarAplicacoes(conta, StatusAplicacao.ATIVO);
        if (aplicacoes.isEmpty()) {
            log.warn("Nenhuma aplicação ativa encontrada para a conta {}.",
                    conta.getNumero());
            throw new AplicacaoNaoEncontradaException();
        }
        realizarRendimentoNasAplicacoes(aplicacoes, conta);
    }

    private void realizarRendimentoNasAplicacoes(List<Aplicacao> aplicacoes, Conta conta) {
        for(Aplicacao apl : aplicacoes) {
            BigDecimal valorAntes = apl.getValorAtual();
            LocalDate data1 = apl.getUltimaCapitalizacao();
            LocalDate data2 = LocalDate.now();
            BigDecimal render = apl.getValorAtual().multiply(apl.getTaxaDiaria());
            BigDecimal dias = new BigDecimal(ChronoUnit.DAYS.between(data1,data2));
            BigDecimal rendimento = render.multiply(dias);
            if (dias.compareTo(BigDecimal.ZERO) <= 0) continue;
            apl.aplicarRendimento(data2,rendimento);
            log.debug(
                    "Aplicação - Valor: {}, Taxa: {}, Dias: {}, Rendimento: {}",
                    apl.getValorAtual(),
                    apl.getTaxaDiaria(),
                    dias,
                    rendimento
            );
            conta.depositar(rendimento);
            var valorAtual = valorAntes.add(rendimento);
            Rendimentos rendimentos =  new Rendimentos(
                    conta,data2,LocalTime.now(),dias.intValue(),apl.getTaxaDiaria(),
                    valorAntes,valorAtual,
                    TipoMovimentacao.RENDIMENTO,
                    DirecaoMovimentacao.ENTRADA
            );
            transferenciaRepository.save(rendimentos);
            log.info(
                    "Rendimento aplicado. Conta: {}, Dias: {}, Valor: {}.",
                    conta.getNumero(),
                    dias,
                    rendimento
            );        }
    }
}
