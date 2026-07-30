package com.org.Sistema_Bancario.SpringBoot.dto;

import java.time.LocalDateTime;

public record ApiError (
        LocalDateTime timestamp,
        int status,
        String error
) {
}
