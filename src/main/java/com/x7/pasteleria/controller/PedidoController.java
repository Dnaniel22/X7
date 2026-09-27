package com.x7.pasteleria.controller;

import com.x7.pasteleria.data.Datos;
import com.x7.pasteleria.model.Pedido;
import com.x7.pasteleria.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoController {

    @GetMapping
    public ResponseEntity<?> listarTodos(HttpSession session) {
        if (!esAdministrador(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", "Acceso exclusivo para el administrador."));
        }

        return ResponseEntity.ok(ordenados(Datos.PEDIDOS));
    }

    @GetMapping("/mis-pedidos")
    public ResponseEntity<?> misPedidos(HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null || !"cliente".equalsIgnoreCase(String.valueOf(session.getAttribute("rol")))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Debes iniciar sesión como cliente."));
        }

        List<Pedido> pedidos = Datos.PEDIDOS.stream()
                .filter(p -> email.equalsIgnoreCase(p.getEmailCliente()))
                .sorted(Comparator.comparing(Pedido::getId).reversed())
                .toList();

        return ResponseEntity.ok(pedidos);
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Pedido pedido, HttpSession session) {
        String email = (String) session.getAttribute("email");
        if (email == null || !"cliente".equalsIgnoreCase(String.valueOf(session.getAttribute("rol")))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Debes iniciar sesión como cliente."));
        }

        if (pedido.getTipo() == null || pedido.getTipo().isBlank()
                || pedido.getProducto() == null || pedido.getProducto().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "El tipo y el producto son obligatorios."));
        }

        Usuario usuario = Datos.USUARIOS.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);

        if (usuario == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("mensaje", "Usuario no encontrado."));
        }

        long id = Datos.nuevoPedidoId();
        pedido.setId(id);
        pedido.setCodigo(String.format("#PED-%04d", 1000 + id));
        pedido.setEstado("PENDIENTE");
        pedido.setNombreCliente(usuario.getNombre());
        pedido.setEmailCliente(usuario.getEmail());
        if (pedido.getMonto() == null || pedido.getMonto().isBlank()) {
            pedido.setMonto("Por confirmar");
        }

        Datos.PEDIDOS.add(pedido);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id,
                                               @RequestBody Map<String, String> datos,
                                               HttpSession session) {
        if (!esAdministrador(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", "Acceso exclusivo para el administrador."));
        }

        Pedido pedido = Datos.PEDIDOS.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (pedido == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Pedido no encontrado."));
        }

        String estado = datos.getOrDefault("estado", "");
        if (!estado.equals("PENDIENTE")
                && !estado.equals("EN PREPARACIÓN")
                && !estado.equals("ENTREGADO")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("mensaje", "Estado no válido."));
        }

        pedido.setEstado(estado);
        return ResponseEntity.ok(pedido);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id, HttpSession session) {
        if (!esAdministrador(session)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("mensaje", "Acceso exclusivo para el administrador."));
        }

        boolean eliminado = Datos.PEDIDOS.removeIf(p -> p.getId().equals(id));
        if (!eliminado) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("mensaje", "Pedido no encontrado."));
        }

        return ResponseEntity.noContent().build();
    }

    private boolean esAdministrador(HttpSession session) {
        return "administrador".equalsIgnoreCase(String.valueOf(session.getAttribute("rol")));
    }

    private List<Pedido> ordenados(List<Pedido> pedidos) {
        return pedidos.stream()
                .sorted(Comparator.comparing(Pedido::getId).reversed())
                .toList();
    }
}
