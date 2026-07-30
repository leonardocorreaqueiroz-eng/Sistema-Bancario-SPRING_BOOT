package com.org.Sistema_Bancario.SpringBoot.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    private String nome;
    @Getter
    @Setter
    @Column(unique = true)
    private String cpf;
    @Getter
    @Setter
    private String password;
    private final LocalDate dataDeCriacao = LocalDate.now();
    protected Cliente() {}

    public Cliente(String nome, String cpf, String password) {
        this.nome = nome;
        this.cpf = cpf;
        this.password = password;
    }
}