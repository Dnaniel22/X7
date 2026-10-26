package com.x7.pasteleria.controller.api;

import com.x7.pasteleria.controller.SesionUsuario;
import com.x7.pasteleria.dto.LoginRequest;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

/**
 * Inicio de sesión para la API (por ejemplo, desde Postman).
 * Usa la misma HttpSession que las vistas: Postman guarda la cookie JSESSIONID
 * y la envía en las siguientes peticiones.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private final UsuarioRepository usuarioRepository;
    private final ApiRespuestas respuestas;

    public AuthApiController(UsuarioRepository usuarioRepository, ApiRespuestas respuestas) {
        this.usuarioRepository = usuarioRepository;
        this.respuestas = respuestas;
    }

    /** POST /api/auth/login → 200 OK con el usuario, 400 si faltan datos, 401 si no coinciden */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest datos, BindingResult result, HttpSession session) {
        if (result.hasErrors()) {
            return respuestas.errorValidacion(result);
        }

        Optional<Usuario> usuario = usuarioRepository.findByEmailIgnoreCase(datos.email().trim())
                .filter(u -> u.getPassword().equals(datos.password()));
        if (usuario.isEmpty()) {
            return respuestas.error(HttpStatus.UNAUTHORIZED, "Correo o contraseña incorrectos.");
        }

        SesionUsuario.iniciar(session, usuario.get());
        return ResponseEntity.ok(usuario.get());   // la contraseña no se incluye (WRITE_ONLY)
    }

    /** GET /api/auth/sesion → 200 OK con el usuario en sesión o 401 */
    @GetMapping("/sesion")
    public ResponseEntity<?> sesion(HttpSession session) {
        Usuario usuario = SesionUsuario.actual(session);
        if (usuario == null) {
            return respuestas.error(HttpStatus.UNAUTHORIZED, "No hay una sesión iniciada.");
        }
        return ResponseEntity.ok(usuario);
    }

    /** POST /api/auth/logout → 204 No Content */
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }
}
