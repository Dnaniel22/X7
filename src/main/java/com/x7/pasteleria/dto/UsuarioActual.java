package com.x7.pasteleria.dto;

import com.x7.pasteleria.security.UsuarioPrincipal;

/** Datos públicos del usuario autenticado (nunca incluye la contraseña). */
public record UsuarioActual(Long id, String nombre, String email, String rol) {

    public static UsuarioActual desde(UsuarioPrincipal usuario) {
        return new UsuarioActual(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol().name());
    }
}
