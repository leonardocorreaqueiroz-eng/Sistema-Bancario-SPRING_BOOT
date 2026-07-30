package com.org.Sistema_Bancario.SpringBoot.repository;

import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.StatusAplicacao;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Aplicacao;
import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface AplicacaoRepository extends JpaRepository<Movimentacao,Integer> {
    @Query("select apl from Aplicacao apl where apl.conta = :conta and apl.status = :status")
    List<Aplicacao> listarAplicacoes(@Param("conta") Conta conta,@Param("status") StatusAplicacao status);
}
