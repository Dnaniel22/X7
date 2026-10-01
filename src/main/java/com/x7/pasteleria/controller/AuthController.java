package com.x7.pasteleria.controller;

import com.x7.pasteleria.data.Datos;
import com.x7.pasteleria.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> datos, HttpSession session) {
        String email = datos.getOrDefault("email", "").trim();
        String password = datos.getOrDefault("password", "");

        Usuario usuario = Datos.USUARIOS.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email)
                        && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Correo o contraseña incorrectos."));
        }

        session.setAttribute("email", usuario.getEmail());
        session.setAttribute("rol", usuario.getRol());

        return ResponseEntity.ok(respuestaSesion(usuario));
    }

    @GetMapping("/sesion")
    public ResponseEntity<?> sesion(HttpSession session) {
        String email = (String) session.getAttribute("email");

        if (email == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "No hay una sesión activa."));
        }

        Usuario usuario = Datos.USUARIOS.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);

        if (usuario == null) {
            session.invalidate();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "La sesión ya no es válida."));
        }

        return ResponseEntity.ok(respuestaSesion(usuario));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.noContent().build();
    }

    private Map<String, Object> respuestaSesion(Usuario usuario) {
        Map<String, Object> usuarioSeguro = new LinkedHashMap<>();
        usuarioSeguro.put("id", usuario.getId());
        usuarioSeguro.put("nombre", usuario.getNombre());
        usuarioSeguro.put("email", usuario.getEmail());
        usuarioSeguro.put("telefono", usuario.getTelefono());
        usuarioSeguro.put("direccion", usuario.getDireccion());
        usuarioSeguro.put("rol", usuario.getRol());

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("rol", usuario.getRol());
        respuesta.put("email", usuario.getEmail());
        respuesta.put("usuario", usuarioSeguro);
        return respuesta;
    }
}
