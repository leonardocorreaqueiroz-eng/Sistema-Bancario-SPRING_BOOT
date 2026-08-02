package com.org.Sistema_Bancario.SpringBoot.model.movimentacoes;

import com.org.Sistema_Bancario.SpringBoot.model.DirecaoMovimentacao;
import com.org.Sistema_Bancario.SpringBoot.model.TipoMovimentacao;
import jakarta.persistence.Entity;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
@Entity
public class Saque extends Movimentacao {
    public Saque() {}

    public Saque(Conta conta, BigDecimal valor, LocalDate data, LocalTime hora,
                 TipoMovimentacao tipo, DirecaoMovimentacao direcaoMovimentacao) {
        super(valor,data,hora,conta,tipo,direcaoMovimentacao);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
