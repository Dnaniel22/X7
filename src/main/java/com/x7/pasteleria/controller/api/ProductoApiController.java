package com.x7.pasteleria.controller.api;

import com.x7.pasteleria.model.Producto;
import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.repository.PedidoRepository;
import com.x7.pasteleria.repository.ProductoRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/productos")
public class ProductoApiController {

    private final ProductoRepository productoRepository;
    private final PedidoRepository pedidoRepository;
    private final ApiRespuestas respuestas;

    public ProductoApiController(ProductoRepository productoRepository,
                                 PedidoRepository pedidoRepository,
                                 ApiRespuestas respuestas) {
        this.productoRepository = productoRepository;
        this.pedidoRepository = pedidoRepository;
        this.respuestas = respuestas;
    }

    @GetMapping
    public List<Producto> listar(@RequestParam(required = false) String nombre,
                                 @RequestParam(required = false) String categoria) {
        return productoRepository.buscar(nombre, categoria);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscar(@PathVariable Long id) {
        Optional<Producto> producto = productoRepository.findById(id);
        if (producto.isEmpty()) {
            return respuestas.error(HttpStatus.NOT_FOUND, "No existe un producto con id " + id + ".");
        }
        return ResponseEntity.ok(producto.get());
    }

    @PostMapping
    public ResponseEntity<?> registrar(@Valid @RequestBody Producto producto,
                                       BindingResult result,
                                       HttpSession session) {

        ResponseEntity<Map<String, Object>> sinPermiso = respuestas.verificarRol(session, Rol.ADMINISTRADOR);
        if (sinPermiso != null) return sinPermiso;

        if (result.hasErrors()) {
            return respuestas.errorValidacion(result);
        }
        if (productoRepository.existsByNombreIgnoreCase(producto.getNombre())) {
            return respuestas.error(HttpStatus.CONFLICT, "Ya existe un producto con ese nombre en el catálogo.");
        }

        producto.setId(null);   // el id siempre lo genera PostgreSQL
        if (producto.getEtiqueta() == null || producto.getEtiqueta().isBlank()) {
            producto.setEtiqueta(producto.getCategoria());
        }
        Producto guardado = productoRepository.save(producto);
        return ResponseEntity.created(URI.create("/api/productos/" + guardado.getId())).body(guardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizar(@PathVariable Long id,
                                        @Valid @RequestBody Producto datos,
                                        BindingResult result,
                                        HttpSession session) {

        ResponseEntity<Map<String, Object>> sinPermiso = respuestas.verificarRol(session, Rol.ADMINISTRADOR);
        if (sinPermiso != null) return sinPermiso;

        Optional<Producto> existente = productoRepository.findById(id);
        if (existente.isEmpty()) {
            return respuestas.error(HttpStatus.NOT_FOUND, "No existe un producto con id " + id + ".");
        }
        if (result.hasErrors()) {
            return respuestas.errorValidacion(result);
        }
        if (productoRepository.existsByNombreIgnoreCaseAndIdNot(datos.getNombre(), id)) {
            return respuestas.error(HttpStatus.CONFLICT, "Ya existe otro producto con ese nombre en el catálogo.");
        }

        Producto producto = existente.get();
        producto.actualizarCon(datos);
        if (producto.getEtiqueta() == null || producto.getEtiqueta().isBlank()) {
            producto.setEtiqueta(producto.getCategoria());
        }
        return ResponseEntity.ok(productoRepository.save(producto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable Long id, HttpSession session) {
        ResponseEntity<Map<String, Object>> sinPermiso = respuestas.verificarRol(session, Rol.ADMINISTRADOR);
        if (sinPermiso != null) return sinPermiso;

        if (!productoRepository.existsById(id)) {
            return respuestas.error(HttpStatus.NOT_FOUND, "No existe un producto con id " + id + ".");
        }
        long enPedidos = pedidoRepository.countByProductoId(id);
        if (enPedidos > 0) {
            return respuestas.error(HttpStatus.CONFLICT, "El producto está en " + enPedidos
                    + " pedido(s) y no se puede eliminar. Actualízalo con \"disponible\": false para ocultarlo.");
        }

        productoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
