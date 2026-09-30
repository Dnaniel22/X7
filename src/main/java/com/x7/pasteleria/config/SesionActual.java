package com.x7.pasteleria.config;

import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SesionActual {

    private static final String ATRIBUTO_USUARIO = "usuarioId";

    private final UsuarioService usuarioService;

    public SesionActual(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    public void iniciar(HttpSession session, Usuario usuario) {
        session.setAttribute(ATRIBUTO_USUARIO, usuario.getId());
    }

    public void cerrar(HttpSession session) {
        session.invalidate();
    }

    public Optional<Usuario> usuario(HttpSession session) {
        Object id = session.getAttribute(ATRIBUTO_USUARIO);
        if (!(id instanceof Long usuarioId)) {
            return Optional.empty();
        }
        return usuarioService.buscarPorId(usuarioId);
    }

    public Optional<Usuario> usuarioConRol(HttpSession session, Rol rol) {
        return usuario(session).filter(u -> u.getRol() == rol);
    }
}
