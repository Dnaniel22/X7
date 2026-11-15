package com.x7.pasteleria.security;

import com.x7.pasteleria.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Le dice a Spring Security cómo buscar a un usuario: lo lee de la tabla usuario de PostgreSQL.
 * Lo usan el formulario de login (web), el login de la API y el filtro JWT.
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // SELECT ... FROM usuario WHERE upper(email) = upper(?)
        return usuarioRepository.findByEmailIgnoreCase(email == null ? "" : email.trim())
                .map(UsuarioPrincipal::desde)
                .orElseThrow(() -> new UsernameNotFoundException("No existe una cuenta con ese correo."));
    }
}
