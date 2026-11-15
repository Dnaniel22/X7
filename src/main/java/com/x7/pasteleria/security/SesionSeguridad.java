package com.x7.pasteleria.security;

import com.x7.pasteleria.model.Usuario;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;

/**
 * Inicia o actualiza la sesión de Spring Security desde un controlador.
 * Se usa al crear una cuenta (entra directo, sin volver a escribir la contraseña)
 * y al editar el perfil (para que la barra de navegación muestre el nombre nuevo).
 */
@Component
public class SesionSeguridad {

    private final SecurityContextRepository repositorio = new HttpSessionSecurityContextRepository();

    /** Inicia sesión con el usuario recién registrado. */
    public void iniciar(Usuario usuario, HttpServletRequest request, HttpServletResponse response) {
        // Nuevo id de sesión al autenticarse: evita la fijación de sesión.
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        guardar(usuario, request, response);
    }

    /** Reemplaza los datos del usuario en la sesión actual (por ejemplo, después de cambiar su nombre). */
    public void actualizar(Usuario usuario, HttpServletRequest request, HttpServletResponse response) {
        guardar(usuario, request, response);
    }

    private void guardar(Usuario usuario, HttpServletRequest request, HttpServletResponse response) {
        UsuarioPrincipal principal = UsuarioPrincipal.desde(usuario);
        principal.eraseCredentials();

        SecurityContext contexto = SecurityContextHolder.createEmptyContext();
        contexto.setAuthentication(UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities()));
        SecurityContextHolder.setContext(contexto);
        repositorio.saveContext(contexto, request, response);
    }
}
