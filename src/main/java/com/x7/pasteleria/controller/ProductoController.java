package com.x7.pasteleria.controller;

import com.x7.pasteleria.data.Datos;
import com.x7.pasteleria.model.Producto;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Map;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    @GetMapping
    public ArrayList<Producto> listar() {
        return new ArrayList<>(Datos.PRODUCTOS);
    }

    @PostMapping
    public ResponseEntity<?> guardar(@RequestBody Producto producto, HttpSession session) {
        if (!esAdministrador(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", "Acceso exclusivo para el administrador."));
        }

        if (producto.getNombre() == null || producto.getNombre().isBlank()
                || producto.getCategoria() == null || producto.getCategoria().isBlank()
                || producto.getDescripcion() == null || producto.getDescripcion().isBlank()
                || producto.getPrecio() <= 0) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Completa correctamente los datos del producto."));
        }

        producto.setId(Datos.nuevoProductoId());
        if (producto.getEtiqueta() == null || producto.getEtiqueta().isBlank()) {
            producto.setEtiqueta(producto.getCategoria());
        }
        if (producto.getImagen() == null) producto.setImagen("");

        Datos.PRODUCTOS.add(producto);
        return ResponseEntity.status(HttpStatus.CREATED).body(producto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id, HttpSession session) {
        if (!esAdministrador(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", "Acceso exclusivo para el administrador."));
        }

        boolean eliminado = Datos.PRODUCTOS.removeIf(p -> p.getId().equals(id));
        if (!eliminado) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Producto no encontrado."));
        }

        return ResponseEntity.noContent().build();
    }

    private boolean esAdministrador(HttpSession session) {
        return "administrador".equalsIgnoreCase(String.valueOf(session.getAttribute("rol")));
    }
}
