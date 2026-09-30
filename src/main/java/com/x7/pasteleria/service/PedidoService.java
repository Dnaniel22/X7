package com.x7.pasteleria.service;

import com.x7.pasteleria.dto.PedidoForm;
import com.x7.pasteleria.dto.ReservaForm;
import com.x7.pasteleria.model.EstadoPedido;
import com.x7.pasteleria.model.Pedido;
import com.x7.pasteleria.model.Producto;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.repository.PedidoRepository;
import com.x7.pasteleria.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Transactional(readOnly = true)
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    public List<Pedido> listarTodos() {
        return pedidoRepository.findAllByOrderByIdDesc();
    }

    public List<Pedido> buscar(EstadoPedido estado, String texto) {
        return pedidoRepository.buscar(estado, textoONulo(texto));
    }

    public List<Pedido> listarDeCliente(Usuario cliente) {
        return pedidoRepository.findByClienteOrderByIdDesc(cliente);
    }

    public Optional<Pedido> buscarPorId(Long id) {
        return pedidoRepository.findById(id);
    }

    public long contarPorEstado(EstadoPedido estado) {
        return pedidoRepository.countByEstado(estado);
    }

    public long contarDeCliente(Usuario cliente) {
        return pedidoRepository.countByCliente(cliente);
    }

    public long total() {
        return pedidoRepository.count();
    }

    @Transactional
    public Pedido registrarPedido(PedidoForm form, Usuario cliente) {
        Pedido pedido = new Pedido();
        pedido.setTipo("Pedido");
        pedido.setProducto(form.getProducto());
        pedido.setDetalle(componerDetallePedido(form));
        pedido.setFechaEntrega(form.getFechaEntrega());
        pedido.setMonto(calcularMonto(form));
        pedido.setNombreContacto(form.getNombreContacto().trim());
        pedido.setTelefono(form.getTelefono().trim());
        pedido.setDireccion(form.getDireccion().trim());
        return guardarConCodigo(pedido, cliente);
    }

    @Transactional
    public Pedido registrarReserva(ReservaForm form, Usuario cliente) {
        Pedido pedido = new Pedido();
        pedido.setTipo("Catering");
        pedido.setProducto(form.getTipoEvento());
        pedido.setDetalle(componerDetalleReserva(form));
        pedido.setFechaEntrega(form.getFechaEvento());
        pedido.setMonto(null);
        pedido.setNombreContacto(cliente.getNombre());
        pedido.setTelefono(cliente.getTelefono());
        pedido.setDireccion(form.getLugar().trim());
        return guardarConCodigo(pedido, cliente);
    }

    @Transactional
    public Pedido cambiarEstado(Pedido pedido, EstadoPedido estado) {
        pedido.setEstado(estado);
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public Pedido avanzarEstado(Pedido pedido) {
        pedido.avanzarEstado();
        return pedidoRepository.save(pedido);
    }

    @Transactional
    public void eliminar(Long id) {
        pedidoRepository.deleteById(id);
    }

    private Pedido guardarConCodigo(Pedido pedido, Usuario cliente) {
        pedido.setCliente(cliente);
        pedido.setEstado(EstadoPedido.PENDIENTE);
        Pedido guardado = pedidoRepository.save(pedido);
        guardado.setCodigo(String.format("#PED-%04d", 1000 + guardado.getId()));
        return pedidoRepository.save(guardado);
    }

    private BigDecimal calcularMonto(PedidoForm form) {
        if (form.esPersonalizado()) {
            return null;
        }
        return productoRepository.findByNombreIgnoreCase(form.getProducto())
                .map(Producto::getPrecio)
                .map(precio -> precio
                        .multiply(form.getTamano().getFactor())
                        .multiply(BigDecimal.valueOf(form.getCantidad()))
                        .setScale(2, RoundingMode.HALF_UP))
                .orElse(null);
    }

    private String componerDetallePedido(PedidoForm form) {
        return unir(
                form.getTamano().getEtiqueta(),
                form.getCantidad() > 1 ? "Cantidad: " + form.getCantidad() : null,
                form.getColores(),
                form.getDetalles()
        );
    }

    private String componerDetalleReserva(ReservaForm form) {
        return unir(
                form.getInvitados() + " invitados",
                "Lugar: " + form.getLugar().trim(),
                form.getRequerimientos()
        );
    }

    private String unir(String... partes) {
        return Stream.of(partes)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(parte -> !parte.isEmpty())
                .collect(Collectors.joining(" · "));
    }

    private String textoONulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }
}
