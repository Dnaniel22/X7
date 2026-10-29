package com.x7.pasteleria.controller.api;

import com.x7.pasteleria.controller.SesionUsuario;
import com.x7.pasteleria.dto.ActualizarPedidoRequest;
import com.x7.pasteleria.model.EstadoPedido;
import com.x7.pasteleria.model.Pedido;
import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.repository.PedidoRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/pedidos")
public class PedidoApiController {

    private final PedidoRepository pedidoRepository;
    private final ApiRespuestas respuestas;

    public PedidoApiController(PedidoRepository pedidoRepository, ApiRespuestas respuestas) {
        this.pedidoRepository = pedidoRepository;
        this.respuestas = respuestas;
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) String estado, HttpSession session) {
        ResponseEntity<Map<String, Object>> sinSesion = respuestas.verificarRol(session, null);
        if (sinSesion != null) return sinSesion;

        Usuario usuario = SesionUsuario.actual(session);
        if (!usuario.isAdministrador()) {
            return ResponseEntity.ok(pedidoRepository.findByUsuarioIdOrderByIdDesc(usuario.getId()));
        }

        if (estado == null || estado.isBlank()) {
            return ResponseEntity.ok(pedidoRepository.findAllByOrderByIdDesc());
        }
        Optional<EstadoPedido> filtro = Arrays.stream(EstadoPedido.values())
                .filter(e -> e.name().equalsIgnoreCase(estado.trim()))
                .findFirst();
        if (filtro.isEmpty()) {
            return respuestas.error(HttpStatus.BAD_REQUEST,
                    "Estado no válido. Usa uno de estos: " + Arrays.toString(EstadoPedido.values()));
        }
        List<Pedido> pedidos = pedidoRepository.findByEstadoOrderByIdDesc(filtro.get());
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id, HttpSession session) {
        ResponseEntity<Map<String, Object>> sinSesion = respuestas.verificarRol(session, null);
        if (sinSesion != null) return sinSesion;

        Optional<Pedido> pedido = pedidoRepository.findCompletoById(id);
        if (pedido.isEmpty()) {
            return respuestas.error(HttpStatus.NOT_FOUND, "No existe un pedido con id " + id + ".");
        }

        Usuario usuario = SesionUsuario.actual(session);
        boolean esPropio = pedido.get().getUsuario().getId().equals(usuario.getId());
        if (!usuario.isAdministrador() && !esPropio) {
            return respuestas.error(HttpStatus.FORBIDDEN, "Ese pedido pertenece a otro cliente.");
        }
        return ResponseEntity.ok(pedido.get());
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody ActualizarPedidoRequest datos,
                                        BindingResult result,
                                        HttpSession session) {

        ResponseEntity<Map<String, Object>> sinPermiso = respuestas.verificarRol(session, Rol.ADMINISTRADOR);
        if (sinPermiso != null) return sinPermiso;

        Optional<Pedido> pedido = pedidoRepository.findCompletoById(id);
        if (pedido.isEmpty()) {
            return respuestas.error(HttpStatus.NOT_FOUND, "No existe un pedido con id " + id + ".");
        }
        if (result.hasErrors()) {
            return respuestas.errorValidacion(result);
        }

        pedido.get().setEstado(datos.estado());
        if (datos.monto() != null) {
            pedido.get().setMonto(datos.monto());
        }
        pedidoRepository.save(pedido.get());
        return ResponseEntity.ok(pedido.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id, HttpSession session) {
        ResponseEntity<Map<String, Object>> sinPermiso = respuestas.verificarRol(session, Rol.ADMINISTRADOR);
        if (sinPermiso != null) return sinPermiso;

        if (!pedidoRepository.existsById(id)) {
            return respuestas.error(HttpStatus.NOT_FOUND, "No existe un pedido con id " + id + ".");
        }
        pedidoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
