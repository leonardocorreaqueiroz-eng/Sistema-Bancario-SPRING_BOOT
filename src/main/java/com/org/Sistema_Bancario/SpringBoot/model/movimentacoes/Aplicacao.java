package com.org.Sistema_Bancario.SpringBoot.model.movimentacoes;

import com.org.Sistema_Bancario.SpringBoot.model.DirecaoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.StatusAplicacao;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Aplicacao extends Movimentacao {

    @Getter
    private BigDecimal valorAtual;
    @Getter
    private LocalDate dataAplicacao;
    @Getter
    private LocalDate ultimaCapitalizacao;
    @Getter
    @Column(precision = 10, scale = 5)
    private BigDecimal taxaDiaria;
    @Enumerated(EnumType.STRING)
    @Getter
    private StatusAplicacao status = StatusAplicacao.ATIVO;

    public Aplicacao() {
    }

    public Aplicacao(Conta conta,
                     BigDecimal valorAtual,
                     LocalDate dataAplicacao,
                     LocalDate ultimaCapitalizacao,
                     BigDecimal taxaDiaria,
                     TipoMovimentacao tipo,
                     DirecaoMovimentacao direcaoMovimentacao) {

        super(valorAtual, dataAplicacao, LocalTime.now(), conta,tipo,direcaoMovimentacao);

        this.valorAtual = valorAtual;
        this.dataAplicacao = dataAplicacao;
        this.ultimaCapitalizacao = ultimaCapitalizacao;
        this.taxaDiaria = taxaDiaria;
    }

    public void aplicarRendimento(LocalDate ultimaCapitalizacao,
                                  BigDecimal rendimento) {
        this.ultimaCapitalizacao = ultimaCapitalizacao;
        this.valorAtual = this.valorAtual.add(rendimento);
    }

    public BigDecimal resgatar(BigDecimal valor){

        if (valorAtual.compareTo(valor) >= 0) {

            valorAtual = valorAtual.subtract(valor);

            if (valorAtual.compareTo(BigDecimal.ZERO) <= 0) {
                status = StatusAplicacao.RESGATADA;
            }

            return BigDecimal.ZERO;
        }

        BigDecimal restante = valor.subtract(valorAtual);

        valorAtual = BigDecimal.ZERO;
        status = StatusAplicacao.RESGATADA;

        return restante;
    }

    @Override
    public String toString() {
        return (getClass().getSimpleName()+"\n" +
                "Valor: "+ valorAtual + " | Operação: " + direcao +"\n"
                + " | Taxa Diária: " + taxaDiaria + " | Status: " + status+"\n"
                + " | Ultima capitalizacao: " + ultimaCapitalizacao + " | Data: " + dataAplicacao + "\n"
        );
    }
}



