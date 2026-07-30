package com.org.Sistema_Bancario.SpringBoot.model.movimentacoes;

import com.org.Sistema_Bancario.SpringBoot.model.DirecaoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Movimentacao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Getter
    @Column(precision = 19, scale = 2)
    private BigDecimal valorOriginal;
    @Getter
    private LocalDate data;
    @Getter
    private LocalTime hora;
    @Getter
    @ManyToOne
    @JoinColumn(name = "conta_numero")
    private Conta conta;
    @Getter
    @Enumerated(EnumType.STRING)
    DirecaoMovimentacao direcao;
    @Getter
    @Enumerated(EnumType.STRING)
    TipoMovimentacao tipo;

    protected Movimentacao() {}

    protected Movimentacao(BigDecimal valorOriginal,
                           LocalDate data,
                           LocalTime hora,
                           Conta conta,
                           TipoMovimentacao tipo,
                           DirecaoMovimentacao direcao) {
        this.valorOriginal = valorOriginal;
        this.data = data;
        this.hora = hora;
        this.conta = conta;
        this.tipo = tipo;
        this.direcao = direcao;
    }

    @Override
    public String toString() {
        return (getClass().getSimpleName()+"\n"
                +" | Valor "+ valorOriginal +" | Operação: "+direcao+"\n"
                +" | Data: "+ data + " | Hora: " + hora+"\n");
    }


}
