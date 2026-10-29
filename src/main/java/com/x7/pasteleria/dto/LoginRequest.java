package com.x7.pasteleria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "{usuario.email.obligatorio}")
        @Email(message = "{usuario.email.formato}")
        String email,

        @NotBlank(message = "{usuario.password.obligatorio}")
        String password) {
}
