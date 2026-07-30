package com.org.Sistema_Bancario.SpringBoot.repository;

import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExtratoRepository extends JpaRepository<Movimentacao,Integer> {
    @Query("select m from Movimentacao m where m.conta.cliente.cpf = :cpf " +
            "order by m.data desc, m.hora desc")
    List<Movimentacao> verExtratos(String cpf);
}
