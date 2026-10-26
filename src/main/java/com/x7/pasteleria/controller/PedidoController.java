package com.x7.pasteleria.controller;

import com.x7.pasteleria.dto.CateringForm;
import com.x7.pasteleria.dto.PedidoForm;
import com.x7.pasteleria.model.Pedido;
import com.x7.pasteleria.model.Producto;
import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.TipoPedido;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.repository.PedidoRepository;
import com.x7.pasteleria.repository.ProductoRepository;
import com.x7.pasteleria.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PedidoController {

    /** Valor del desplegable para un producto que no está en el catálogo. */
    private static final long PRODUCTO_PERSONALIZADO = 0L;

    /**
     * Cada tamaño multiplica el precio base del producto.
     * Es un LinkedHashMap para que el desplegable respete este orden.
     */
    private static final Map<String, BigDecimal> TAMANOS = new LinkedHashMap<>();

    static {
        TAMANOS.put("Pequeña (6-8 porciones)", new BigDecimal("1.0"));
        TAMANOS.put("Mediana (12-15 porciones)", new BigDecimal("1.5"));
        TAMANOS.put("Grande (20-25 porciones)", new BigDecimal("2.0"));
        TAMANOS.put("Extra Grande (30-40 porciones)", new BigDecimal("3.0"));
    }

    private static final List<String> TIPOS_EVENTO = List.of(
            "Matrimonio", "Quinceañero", "Evento Corporativo", "Aniversario / Cumpleaños");

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;

    public PedidoController(PedidoRepository pedidoRepository,
                            ProductoRepository productoRepository,
                            UsuarioRepository usuarioRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // Listas que necesitan los desplegables de las dos vistas.

    @ModelAttribute("productos")
    public List<Producto> productos() {
        return productoRepository.findByDisponibleTrueOrderByCategoriaAscNombreAsc();
    }

    @ModelAttribute("tamanos")
    public Map<String, BigDecimal> tamanos() {
        return TAMANOS;
    }

    @ModelAttribute("tiposEvento")
    public List<String> tiposEvento() {
        return TIPOS_EVENTO;
    }

    @ModelAttribute("fechaMinima")
    public LocalDate fechaMinima() {
        return LocalDate.now().plusDays(1);
    }

    // ---------------- Encargar pedido ----------------

    @GetMapping("/pedido")
    public String formularioPedido(@RequestParam(name = "producto", required = false) Long productoId,
                                   HttpSession session,
                                   Model model) {

        PedidoForm form = new PedidoForm();
        form.setProductoId(productoId);

        // Si es un cliente, sus datos de contacto aparecen ya completados.
        Usuario cliente = SesionUsuario.conRol(session, Rol.CLIENTE);
        if (cliente != null) {
            form.setNombreContacto(cliente.getNombre());
            form.setTelefono(cliente.getTelefono());
            form.setDireccion(cliente.getDireccion());
        }

        model.addAttribute("pedido", form);
        return "pedido";
    }

    @PostMapping("/pedido")
    public String registrarPedido(@Valid @ModelAttribute("pedido") PedidoForm form,
                                  BindingResult result,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes flash) {

        Usuario cliente = SesionUsuario.conRol(session, Rol.CLIENTE);
        if (cliente == null) {
            model.addAttribute("mostrarModalAuth", true);
            return "pedido";
        }

        // Reglas que dependen de la base de datos o de las listas del negocio.
        Producto producto = null;
        if (form.getProductoId() != null && form.getProductoId() != PRODUCTO_PERSONALIZADO) {
            producto = productoRepository.findById(form.getProductoId())
                    .filter(Producto::isDisponible)
                    .orElse(null);
            if (producto == null) {
                result.rejectValue("productoId", "pedido.producto.noDisponible");
            }
        }
        if (form.getTamano() != null && !TAMANOS.containsKey(form.getTamano())) {
            result.rejectValue("tamano", "pedido.tamano.valido");
        }
        if (form.getFechaEntrega() != null && form.getFechaEntrega().isAfter(LocalDate.now().plusYears(1))) {
            result.rejectValue("fechaEntrega", "pedido.fecha.lejana");
        }

        if (result.hasErrors()) {
            return "pedido";   // vuelve al formulario con los mensajes junto a cada campo
        }

        Pedido pedido = new Pedido();
        pedido.setTipo(TipoPedido.PEDIDO);
        pedido.setUsuario(usuarioRepository.getReferenceById(cliente.getId()));
        pedido.setProducto(producto);   // null si es personalizado
        pedido.setFechaEntrega(form.getFechaEntrega());
        pedido.setNombreContacto(form.getNombreContacto());
        pedido.setTelefono(form.getTelefono());
        pedido.setDireccion(form.getDireccion());
        pedido.setDetalle(unir(
                form.getTamano(),
                form.getCantidad() > 1 ? "Cantidad: " + form.getCantidad() : "",
                form.getColores(),
                form.getDetalles()));
        // Producto del catálogo: precio base × factor del tamaño × cantidad. Personalizado: por confirmar (NULL).
        pedido.setMonto(producto == null ? null : calcularMonto(producto, form.getTamano(), form.getCantidad()));

        Pedido guardado = guardarConCodigo(pedido);
        flash.addFlashAttribute("exito", "¡Pedido " + guardado.getCodigo() + " registrado!");
        return "redirect:/cliente/panel";
    }

    // ---------------- Cotizar catering ----------------

    @GetMapping("/reservar")
    public String formularioReserva(HttpSession session, Model model) {
        CateringForm form = new CateringForm();
        Usuario cliente = SesionUsuario.conRol(session, Rol.CLIENTE);
        if (cliente != null) {
            form.setTelefono(cliente.getTelefono());
        }
        model.addAttribute("catering", form);
        return "reservar";
    }

    @PostMapping("/reservar")
    public String registrarReserva(@Valid @ModelAttribute("catering") CateringForm form,
                                   BindingResult result,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes flash) {

        Usuario cliente = SesionUsuario.conRol(session, Rol.CLIENTE);
        if (cliente == null) {
            model.addAttribute("mostrarModalAuth", true);
            return "reservar";
        }

        if (form.getTipoEvento() != null && !TIPOS_EVENTO.contains(form.getTipoEvento())) {
            result.rejectValue("tipoEvento", "catering.tipoEvento.valido");
        }
        if (form.getFechaEvento() != null && form.getFechaEvento().isAfter(LocalDate.now().plusYears(1))) {
            result.rejectValue("fechaEvento", "pedido.fecha.lejana");
        }

        if (result.hasErrors()) {
            return "reservar";
        }

        Pedido pedido = new Pedido();
        pedido.setTipo(TipoPedido.CATERING);
        pedido.setUsuario(usuarioRepository.getReferenceById(cliente.getId()));
        pedido.setTipoEvento(form.getTipoEvento());   // el catering no tiene producto del catálogo
        pedido.setFechaEntrega(form.getFechaEvento());
        pedido.setNombreContacto(cliente.getNombre());
        pedido.setTelefono(form.getTelefono());
        pedido.setDireccion(form.getLugar());
        pedido.setDetalle(unir(
                form.getInvitados() + " invitados",
                "Lugar: " + form.getLugar(),
                form.getRequerimientos()));
        pedido.setMonto(null);   // lo confirma el administrador

        Pedido guardado = guardarConCodigo(pedido);
        flash.addFlashAttribute("exito", "¡Solicitud " + guardado.getCodigo() + " registrada!");
        return "redirect:/cliente/panel";
    }

    // ---------------- Apoyo ----------------

    /** Guarda el pedido y, con el id que generó PostgreSQL, le asigna su código visible. */
    private Pedido guardarConCodigo(Pedido pedido) {
        Pedido guardado = pedidoRepository.save(pedido);                        // INSERT
        guardado.setCodigo(String.format("#PED-%04d", 1000 + guardado.getId()));
        return pedidoRepository.save(guardado);                                 // UPDATE del código
    }

    private BigDecimal calcularMonto(Producto producto, String tamano, int cantidad) {
        return producto.getPrecio()
                .multiply(TAMANOS.get(tamano))
                .multiply(BigDecimal.valueOf(cantidad))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private String unir(String... partes) {
        StringBuilder sb = new StringBuilder();
        for (String parte : partes) {
            if (parte != null && !parte.isBlank()) {
                if (sb.length() > 0) sb.append(" · ");
                sb.append(parte.trim());
            }
        }
        return sb.toString();
    }
}
