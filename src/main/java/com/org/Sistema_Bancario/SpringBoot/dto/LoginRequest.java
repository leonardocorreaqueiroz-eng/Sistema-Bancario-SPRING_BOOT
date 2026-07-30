package com.org.Sistema_Bancario.SpringBoot.dto;

import com.org.Sistema_Bancario.SpringBoot.util.CPF;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record LoginRequest (

        @CPF
        String cpf,

        @NotBlank(message = "A senha é obrigatória.")
        @Size(min = 6, max = 6)
        @Pattern(
                regexp = "^\\d{6}$",
                message = "A senha deve conter " +
                        "exatamente 6 dígitos numéricos.")
        String senha
) {
}
