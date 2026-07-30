package com.org.Sistema_Bancario.SpringBoot.model;

import jakarta.persistence.Embeddable;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Embeddable
public class HoraData {
    private final LocalDate data;
    private final LocalTime hora;


    public HoraData(LocalDate data, LocalTime hora) {
        this.data = data;
        this.hora = hora;
    }

}