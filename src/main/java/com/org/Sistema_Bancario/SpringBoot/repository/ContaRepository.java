package com.org.Sistema_Bancario.SpringBoot.repository;
import com.org.Sistema_Bancario.SpringBoot.model.Conta;
import com.org.Sistema_Bancario.SpringBoot.model.TipoConta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ContaRepository extends JpaRepository<Conta, Integer> {
    @Query("SELECT c FROM Conta c where c.cliente.cpf = :cpf")
    List<Conta> findContasByCpf(String cpf);
    @Query("select c from Conta c where c.cliente.cpf = :cpf and c.tipoConta = :tipo")
    Optional<Conta> findByCpf(@Param("cpf") String cpf, @Param("tipo") TipoConta tipo);
}
