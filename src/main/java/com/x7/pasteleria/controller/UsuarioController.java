package com.x7.pasteleria.controller;

import com.x7.pasteleria.data.Datos;
import com.x7.pasteleria.model.Usuario;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody Usuario usuario) {
        if (usuario.getNombre() == null || usuario.getNombre().isBlank()
                || usuario.getEmail() == null || usuario.getEmail().isBlank()
                || usuario.getPassword() == null || usuario.getPassword().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Nombre, correo y contraseña son obligatorios."));
        }

        boolean existe = Datos.USUARIOS.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(usuario.getEmail().trim()));

        if (existe) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("mensaje", "Ya existe una cuenta con ese correo."));
        }

        usuario.setId(Datos.nuevoUsuarioId());
        usuario.setEmail(usuario.getEmail().trim().toLowerCase());
        usuario.setRol("cliente");
        if (usuario.getTelefono() == null) usuario.setTelefono("");
        if (usuario.getDireccion() == null) usuario.setDireccion("");
        Datos.USUARIOS.add(usuario);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("id", usuario.getId());
        respuesta.put("nombre", usuario.getNombre());
        respuesta.put("email", usuario.getEmail());
        respuesta.put("rol", usuario.getRol());

        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}
