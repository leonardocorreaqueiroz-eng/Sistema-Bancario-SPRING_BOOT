package com.org.Sistema_Bancario.SpringBoot.repository;

import com.org.Sistema_Bancario.SpringBoot.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Integer> {
}
