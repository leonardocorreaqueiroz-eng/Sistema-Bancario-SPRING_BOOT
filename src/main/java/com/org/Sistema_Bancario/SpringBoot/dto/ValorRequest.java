package com.org.Sistema_Bancario.SpringBoot.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record ValorRequest (
        @NotNull
        @Positive
        BigDecimal valor
){
}
