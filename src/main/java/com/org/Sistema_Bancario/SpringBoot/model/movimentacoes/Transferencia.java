package com.org.Sistema_Bancario.SpringBoot.model.movimentacoes;

import com.org.Sistema_Bancario.SpringBoot.model.DirecaoMovimentacao;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
public class Transferencia extends Movimentacao {
    @Getter
    @ManyToOne
    @JoinColumn(name = "conta_destino_numero")
    private Conta contaDestino;
    public Transferencia(){
    }

    public Transferencia(LocalDate data, LocalTime hora, BigDecimal valor,
                         Conta conta1, Conta conta2,
                         DirecaoMovimentacao direcaoMovimentacao,
                         TipoMovimentacao tipo) {

        super(valor, data, hora, conta1,tipo,direcaoMovimentacao);

        this.contaDestino = conta2;

    }

    @Override
    public String toString() {
        return getClass().getSimpleName()+"\n"+
                "Conta Origem: " + getConta().getNumero() + " | Conta Destino: " + contaDestino.getNumero() + "\n" +
                " | Valor: " + getValorOriginal() +  " | Tipo: " + tipo + " | Operação: " + direcao + "\n" +
                " | Data: "+ getData()+" | Hora: " + getHora() + "\n";
    }
}
