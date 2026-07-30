package com.org.Sistema_Bancario.SpringBoot.model.movimentacoes;

import com.org.Sistema_Bancario.SpringBoot.model.DirecaoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Rendimentos extends Movimentacao {


    private int diasCapitalizados;
    @Column(precision = 10, scale = 5)
    private BigDecimal taxa;
    private BigDecimal valorAntes;


    public Rendimentos() {}

    public Rendimentos(Conta conta,
                       LocalDate dataAtual,
                       LocalTime hora,
                       int diasCapitalizados,
                       BigDecimal taxa,
                       BigDecimal valorAntes,
                       BigDecimal valorDepois,
                       TipoMovimentacao tipo,
                       DirecaoMovimentacao direcaoMovimentacao
                       ) {
        super(valorDepois,dataAtual,hora,conta,tipo,direcaoMovimentacao);

        this.diasCapitalizados = diasCapitalizados;
        this.taxa = taxa;
        this.valorAntes = valorAntes;

    }

    @Override
    public String toString() {
        return super.toString() + "\nDias Capitalizados: "+diasCapitalizados+" | Taxa: "+taxa+" " +
                "| ValorAntes: "+valorAntes+"\n";
    }
}