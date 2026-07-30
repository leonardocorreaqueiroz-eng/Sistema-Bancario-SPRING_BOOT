package com.org.Sistema_Bancario.SpringBoot.repository;

import com.org.Sistema_Bancario.SpringBoot.model.movimentacoes.Movimentacao;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferenciaRepository extends JpaRepository<Movimentacao,Integer> {
}
