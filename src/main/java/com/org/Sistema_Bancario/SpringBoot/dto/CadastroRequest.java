package com.org.Sistema_Bancario.SpringBoot.dto;

import com.org.Sistema_Bancario.SpringBoot.util.CPF;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CadastroRequest (
        @NotBlank
        @Size(min = 2, max = 50)
        @Pattern(
                regexp = "[a-zA-ZÀ-ÿ\\s]+",
                message = "Nome inválido."
        )
        String nome,

        @CPF
        String cpf,

        @NotBlank(message = "A senha é obrigatória.")
        @Pattern(
                regexp = "^\\d{6}$",
                message = "A senha deve conter " +
                        "exatamente 6 dígitos numéricos."
        )
        String password
) {
}
