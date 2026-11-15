package com.x7.pasteleria.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro que se ejecuta en cada petición a /api/**.
 * Lee la cabecera "Authorization: Bearer <token>", valida el JWT y, si es correcto,
 * registra al usuario en el SecurityContext para que las reglas hasRole(...) puedan revisarlo.
 *
 * No es un @Component a propósito: así Spring Boot no lo registra también como filtro global
 * y solo se ejecuta dentro de la cadena de seguridad de la API (ver SecurityConfig).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** Atributo de la petición donde se deja el motivo del rechazo, para la respuesta 401. */
    public static final String ATRIBUTO_ERROR = "jwt.error";

    private static final String PREFIJO = "Bearer ";

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String cabecera = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera == null || !cabecera.startsWith(PREFIJO)) {
            // Sin token: la petición sigue como anónima. Si la ruta es protegida, responderá 401.
            filterChain.doFilter(request, response);
            return;
        }

        String token = cabecera.substring(PREFIJO.length()).trim();
        try {
            Claims datos = jwtService.validar(token);

            // Se vuelve a leer el usuario de la base: si lo eliminaron o lo desactivaron, el token deja de servir.
            UserDetails usuario = userDetailsService.loadUserByUsername(datos.getSubject());
            if (!usuario.isEnabled()) {
                request.setAttribute(ATRIBUTO_ERROR, "La cuenta está desactivada.");
            } else {
                if (usuario instanceof CredentialsContainer conCredenciales) {
                    conCredenciales.eraseCredentials();
                }
                UsernamePasswordAuthenticationToken autenticacion =
                        UsernamePasswordAuthenticationToken.authenticated(usuario, null, usuario.getAuthorities());
                autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContext contexto = SecurityContextHolder.createEmptyContext();
                contexto.setAuthentication(autenticacion);
                SecurityContextHolder.setContext(contexto);
            }
        } catch (ExpiredJwtException e) {
            request.setAttribute(ATRIBUTO_ERROR, "El token expiró. Inicia sesión otra vez con POST /api/auth/login.");
        } catch (JwtException | IllegalArgumentException e) {
            request.setAttribute(ATRIBUTO_ERROR, "El token no es válido (firma, formato o emisor incorrectos).");
        } catch (UsernameNotFoundException e) {
            request.setAttribute(ATRIBUTO_ERROR, "El usuario del token ya no existe.");
        }

        filterChain.doFilter(request, response);
    }
}
