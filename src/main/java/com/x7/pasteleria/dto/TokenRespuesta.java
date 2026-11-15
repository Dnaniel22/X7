package com.x7.pasteleria.dto;

/** Respuesta de POST /api/auth/login: el JWT y los datos básicos del usuario. */
public record TokenRespuesta(String token, String tipo, long expiraEnSegundos, UsuarioActual usuario) {
}
