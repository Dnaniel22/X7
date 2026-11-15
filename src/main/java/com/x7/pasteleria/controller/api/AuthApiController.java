package com.x7.pasteleria.controller.api;

import com.x7.pasteleria.dto.LoginRequest;
import com.x7.pasteleria.dto.TokenRespuesta;
import com.x7.pasteleria.dto.UsuarioActual;
import com.x7.pasteleria.security.UsuarioPrincipal;
import com.x7.pasteleria.security.jwt.JwtService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Autenticación de la API con JWT.
 *
 * Flujo:
 *  1. POST /api/auth/login con {"email", "password"} → Spring Security comprueba la contraseña (BCrypt)
 *     y la API responde con un token firmado.
 *  2. Las siguientes peticiones envían la cabecera  Authorization: Bearer <token>.
 *  3. JwtAuthenticationFilter valida el token en cada petición; no se usa sesión.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final ApiRespuestas respuestas;

    public AuthApiController(AuthenticationManager authenticationManager,
                             JwtService jwtService,
                             ApiRespuestas respuestas) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.respuestas = respuestas;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest datos, BindingResult result) {
        if (result.hasErrors()) {
            return respuestas.errorValidacion(result);
        }

        Authentication autenticacion;
        try {
            // Usa UsuarioDetailsService (busca el correo en la BD) y PasswordEncoder (compara con el hash BCrypt).
            autenticacion = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(datos.email().trim(), datos.password()));
        } catch (DisabledException e) {
            return respuestas.error(HttpStatus.UNAUTHORIZED, "La cuenta está desactivada. Comunícate con la pastelería.");
        } catch (AuthenticationException e) {
            return respuestas.error(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos.");
        }

        UsuarioPrincipal usuario = (UsuarioPrincipal) autenticacion.getPrincipal();
        String token = jwtService.generarToken(usuario);
        return ResponseEntity.ok(new TokenRespuesta(
                token, "Bearer", jwtService.getDuracionSegundos(), UsuarioActual.desde(usuario)));
    }

    /** Datos del usuario dueño del token. Sirve para comprobar que el token funciona. */
    @GetMapping("/perfil")
    public UsuarioActual perfil(@AuthenticationPrincipal UsuarioPrincipal usuario) {
        return UsuarioActual.desde(usuario);
    }
}
