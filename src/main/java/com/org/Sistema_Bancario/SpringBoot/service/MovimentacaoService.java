package com.org.Sistema_Bancario.SpringBoot.service;
import com.org.Sistema_Bancario.SpringBoot.exceptions.AplicacaoException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.AplicacaoNaoEncontradaException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ContaException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.ContaNaoEncontradaException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.MovimentacaoException;
import com.org.Sistema_Bancario.SpringBoot.exceptions.OperacaoNaoPermitidaException;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.DirecaoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.HoraData;
import com.org.Sistema_Bancario.SpringBoot.model.StatusAplicacao;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Aplicacao;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Deposito;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Saque;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Transferencia;
import com.org.Sistema_Bancario.SpringBoot.repository.AplicacaoRepository;
import com.org.Sistema_Bancario.SpringBoot.repository.ContaRepository;
import com.org.Sistema_Bancario.SpringBoot.repository.TransferenciaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.org.Sistema_Bancario.SpringBoot.model.RegrasDeBanco.LIMITE_DOC;
import static com.org.Sistema_Bancario.SpringBoot.model.RegrasDeBanco.TAXA_INVESTIMENTO;

@Service
public class MovimentacaoService {
    private static final Logger log =
            LoggerFactory.getLogger(MovimentacaoService.class);

    private final TransferenciaRepository transferenciaRepository;
    private final AplicacaoRepository aplicacaoRepository;
    private final ContaRepository contaRepository;
    private final RendimentoService rendimentoService;

    public MovimentacaoService(TransferenciaRepository transferenciaRepository,
                               AplicacaoRepository aplicacaoRepository,
                               ContaRepository contaRepository,
                               RendimentoService rendimentoService) {
        this.transferenciaRepository = transferenciaRepository;
        this.aplicacaoRepository = aplicacaoRepository;
        this.contaRepository = contaRepository;
        this.rendimentoService = rendimentoService;
    }

    public HoraData getTime(){
        LocalDate date = LocalDate.now();
        LocalTime time = LocalTime.now();
        return new HoraData(date ,time);
    }

    @Transactional
    public void transferir(String origem,
                           String destino,
                           BigDecimal valor,
                           TipoMovimentacao tipo,
                           TipoConta tipoConta1, TipoConta tipoConta2) {
        log.info("Iniciando transferência de R$ {} da conta {} para a conta {}",
                valor, origem, destino);
        if (origem.equals(destino) && tipoConta1 == tipoConta2)
            throw new ContaException("Não é permitido transferir para a mesma conta.");

        Conta contaOrigem = contaRepository.findByCpf(origem, tipoConta1)
                .orElseThrow((ContaNaoEncontradaException::new));

        Conta contaDestino = contaRepository.findByCpf(destino, tipoConta2)
                .orElseThrow(ContaNaoEncontradaException::new);
        validarTransferencia(valor, tipo, getTime(), contaOrigem, contaDestino);
        transferenciaRepository.save(
                criarTransferencia(valor,tipo,getTime(),contaOrigem,contaDestino,DirecaoMovimentacao.SAIDA));
        transferenciaRepository.save(
                criarTransferencia(valor,tipo,getTime(),contaDestino,contaOrigem,DirecaoMovimentacao.ENTRADA));
    }

    private void validarTransferencia(BigDecimal valor,
                                      TipoMovimentacao tipo,
                                      HoraData getTime, Conta contaOrigem,
                                      Conta contaDestino) {

        if (contaOrigem.getTipoConta() == TipoConta.INVESTIMENTO && contaDestino.getTipoConta() == TipoConta.INVESTIMENTO)
            throw new OperacaoNaoPermitidaException("Não é permitido realizar transferencia entre contas investimento!");
        if ((contaOrigem.getTipoConta() == TipoConta.CORRENTE && contaDestino.getTipoConta() == TipoConta.CORRENTE)
        && (tipo == TipoMovimentacao.APLICACAO || tipo == TipoMovimentacao.RESGATE)){
            throw new MovimentacaoException("Tipo de movimentação não permitida entre contas correntes!");
        }
        if (contaOrigem.getTipoConta() == TipoConta.CORRENTE && contaDestino.getTipoConta() == TipoConta.INVESTIMENTO ||
                contaOrigem.getTipoConta() == TipoConta.INVESTIMENTO && contaDestino.getTipoConta() == TipoConta.CORRENTE){
            if (!contaOrigem.getCliente().getCpf().equals(contaDestino.getCliente().getCpf())){
                log.warn("Transferência da conta {}: {} para conta {}: {} não permitida",
                        contaOrigem.getTipoConta(),contaOrigem.getNumero(),
                        contaDestino.getTipoConta(), contaDestino.getNumero());
                throw new OperacaoNaoPermitidaException("Transferência entre conta corrente e" +
                        " investimento deve ser do mesmo cliente!");
            }
            if (tipo != TipoMovimentacao.APLICACAO && contaOrigem.getTipoConta() == TipoConta.CORRENTE) {
                log.warn("Transferência da conta {}: {} para conta {}: {} não permitida para tipo" +
                                " de movimentação: {}",
                        contaOrigem.getTipoConta(),contaOrigem.getNumero(),
                        contaDestino.getTipoConta(), contaDestino.getNumero(), tipo);
                throw new OperacaoNaoPermitidaException("Transferência entre conta corrente e" +
                        " investimento deve ser aplicação ou resgate!");
            }
            if (tipo != TipoMovimentacao.RESGATE && contaOrigem.getTipoConta() == TipoConta.INVESTIMENTO) {
                log.warn("Transferência da conta {}: {} para conta {}: {} não permitida para tipo" +
                                " de movimentacao: {}",
                        contaOrigem.getTipoConta(),contaOrigem.getNumero(),
                        contaDestino.getTipoConta(), contaDestino.getNumero(), tipo);
                throw new OperacaoNaoPermitidaException("Transferência entre conta corrente e" +
                        " investimento deve ser aplicação ou resgate!");
            }
        }

        if (tipo == TipoMovimentacao.DOC && valor.compareTo(LIMITE_DOC) > 0){
            log.warn("Tentativa de DOC acima do limite. Valor: {}", valor);
            throw new MovimentacaoException("Limite de transferência excedido por DOC");
        }

        if (tipo == TipoMovimentacao.RESGATE) {
            log.info("Realizando transferência de RESGATE da conta {} para conta {}",
                    contaDestino.getNumero() ,contaOrigem.getNumero());
            executarResgate(valor, contaOrigem);
        }
        if (tipo == TipoMovimentacao.DEPOSITO || tipo == TipoMovimentacao.SAQUE){
            log.warn("Transferência da conta {}: {} para conta {}: {} não permitida para tipo" +
                            " de movimentação: {}",
                    contaOrigem.getTipoConta(),contaOrigem.getNumero(),
                    contaDestino.getTipoConta(), contaDestino.getNumero(), tipo);
            throw new MovimentacaoException("Tipo de movimentação não permitida em transferência");
        }
        contaOrigem.transferir(valor, contaDestino);
        log.info("Transferência realizada com sucesso de R$ {} da conta {} para a conta {}",
                valor,contaOrigem.getNumero(),contaDestino.getNumero());
        if (tipo == TipoMovimentacao.APLICACAO){
            Aplicacao aplicacao = criarAplicacao(valor, getTime, contaDestino);
            transferenciaRepository.save(aplicacao);
        }
    }

    private void executarResgate(BigDecimal valor, Conta contaOrigem) {
        rendimentoService.aplicarRendimentos(contaOrigem);
        saqueDasAplicacoes(contaOrigem, valor);
        log.info("Resgate de R$ {} realizado na conta de investimento {}.",
        valor, contaOrigem.getNumero());
    }

    private Aplicacao criarAplicacao(BigDecimal valor, HoraData getTime, Conta contaDestino) {
        LocalDate dataAplicacao = getTime.getData();
        return new Aplicacao(contaDestino,
                valor,
                dataAplicacao,
                dataAplicacao,
                TAXA_INVESTIMENTO,
                TipoMovimentacao.APLICACAO,
                DirecaoMovimentacao.SAIDA);
    }

    private Transferencia criarTransferencia(BigDecimal valor,
                                             TipoMovimentacao tipo,
                                             HoraData getTime,
                                             Conta contaOrigem,
                                             Conta contaDestino,
                                             DirecaoMovimentacao direcaoMovimentacao) {
        return new Transferencia(
                getTime.getData(),
                getTime.getHora(),
                valor,
                contaOrigem,
                contaDestino,
                direcaoMovimentacao,
                tipo
        );
    }

    private void saqueDasAplicacoes(Conta conta, BigDecimal valor){
        List<Aplicacao> aplicacoes = aplicacaoRepository.listarAplicacoes(conta, StatusAplicacao.ATIVO);

        if (aplicacoes.isEmpty()) {
            log.warn("Nemhuma aplicação foi encontrada na conta {}", conta.getNumero());
            throw new AplicacaoNaoEncontradaException("Nenhuma aplicação foi encontrada");
        }

        BigDecimal totalAplicado = aplicacoes.stream().map(Aplicacao::getValorAtual)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (valor.compareTo(totalAplicado) > 0){
            log.warn("Total aplicado insuficiente {} para o valor {}",
                    totalAplicado,valor);
            throw new AplicacaoException("Total aplicado insuficiente");
        }


        BigDecimal restante = valor;

        for (int i = aplicacoes.size() - 1; i >= 0; i--) {

            restante = aplicacoes.get(i).resgatar(restante);

            if (restante.compareTo(BigDecimal.ZERO) == 0) {
                break;
            }
        }
        //Proteção contra possível modificação
        if (restante.compareTo(BigDecimal.ZERO) != 0) {
            log.warn("Valor {} insuficiente", restante);
            throw new AplicacaoException(
                    "Erro ao processar o resgate das aplicações.");
        }
    }
    @Transactional
    public void realizarMovimentacao(String cpf, BigDecimal valor, TipoMovimentacao tipo){
        Conta conta = contaRepository.findByCpf(cpf,TipoConta.CORRENTE)
                .orElseThrow(ContaNaoEncontradaException::new);
        HoraData time = getTime();
        if (tipo == TipoMovimentacao.DEPOSITO) {
            conta.depositar(valor);
            Deposito deposito =  new Deposito(
                    conta,valor, time.getData(), time.getHora(),
                    TipoMovimentacao.DEPOSITO,
                    DirecaoMovimentacao.ENTRADA);
            transferenciaRepository.save(deposito);
            log.info("Depósito no valor de R$ {} realizada com sucesso na conta {}.",
                    valor, conta.getNumero());
            }
        if (tipo == TipoMovimentacao.SAQUE) {
            conta.sacar(valor);
            Saque saque =  new Saque(conta,valor, time.getData(),
                    time.getHora(),
                    TipoMovimentacao.SAQUE,
                    DirecaoMovimentacao.SAIDA);
            transferenciaRepository.save(saque);
            log.info("Saque no valor de R$ {} realizada com sucesso na conta {}.",
                    valor, conta.getNumero());
        }
    }

}
