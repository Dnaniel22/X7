package com.x7.pasteleria.controller;

import com.x7.pasteleria.data.Datos;
import com.x7.pasteleria.model.Pedido;
import com.x7.pasteleria.model.Producto;
import com.x7.pasteleria.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class PedidoController {

    /**
     * Cada tamaño multiplica el precio base del producto.
     * Es un LinkedHashMap para que el desplegable respete este orden.
     */
    private static final Map<String, Double> TAMANOS = new LinkedHashMap<>();

    static {
        TAMANOS.put("Pequeña (6-8 porciones)", 1.0);
        TAMANOS.put("Mediana (12-15 porciones)", 1.5);
        TAMANOS.put("Grande (20-25 porciones)", 2.0);
        TAMANOS.put("Extra Grande (30-40 porciones)", 3.0);
    }

    private static final List<String> TIPOS_EVENTO = List.of(
            "Matrimonio", "Quinceañero", "Evento Corporativo", "Aniversario / Cumpleaños");

    @ModelAttribute("productos")
    public List<Producto> productos() {
        return Datos.PRODUCTOS;
    }

    @ModelAttribute("tamanos")
    public List<String> tamanos() {
        return List.copyOf(TAMANOS.keySet());
    }

    @ModelAttribute("tiposEvento")
    public List<String> tiposEvento() {
        return TIPOS_EVENTO;
    }

    // ---------------- Encargar pedido ----------------

    @GetMapping("/pedido")
    public String formularioPedido(@RequestParam(required = false) String producto,
                                   HttpSession session,
                                   Model model) {

        Pedido pedido = new Pedido();
        pedido.setProducto(producto);

        Usuario cliente = clienteEnSesion(session);
        if (cliente != null) {
            pedido.setNombreContacto(cliente.getNombre());
            pedido.setTelefono(cliente.getTelefono());
            pedido.setDireccion(cliente.getDireccion());
        }

        model.addAttribute("pedido", pedido);
        return "pedido";
    }

    @PostMapping("/pedido")
    public String registrarPedido(@ModelAttribute("pedido") Pedido pedido,
                                  @RequestParam(defaultValue = "") String tamano,
                                  @RequestParam(defaultValue = "1") int cantidad,
                                  @RequestParam(defaultValue = "") String colores,
                                  @RequestParam(defaultValue = "") String detalles,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes flash) {

        Usuario cliente = clienteEnSesion(session);
        if (cliente == null) {
            model.addAttribute("mostrarModalAuth", true);
            return "pedido";
        }

        if (vacio(pedido.getProducto()) || vacio(pedido.getFechaEntrega()) || vacio(tamano)) {
            model.addAttribute("error", "Completa el producto, la fecha de entrega y el tamaño.");
            return "pedido";
        }

        pedido.setTipo("Pedido");
        pedido.setDetalle(unir(tamano,
                cantidad > 1 ? "Cantidad: " + cantidad : "",
                colores,
                detalles));
        pedido.setMonto(calcularMonto(pedido.getProducto(), tamano, cantidad));

        guardar(pedido, cliente);
        flash.addFlashAttribute("exito", "¡Pedido " + pedido.getCodigo() + " registrado!");
        return "redirect:/cliente/panel";
    }

    // ---------------- Cotizar catering ----------------

    @GetMapping("/reservar")
    public String formularioReserva(Model model) {
        model.addAttribute("pedido", new Pedido());
        return "reservar";
    }

    @PostMapping("/reservar")
    public String registrarReserva(@ModelAttribute("pedido") Pedido pedido,
                                   @RequestParam(defaultValue = "") String invitados,
                                   @RequestParam(defaultValue = "") String lugar,
                                   @RequestParam(defaultValue = "") String requerimientos,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes flash) {

        Usuario cliente = clienteEnSesion(session);
        if (cliente == null) {
            model.addAttribute("mostrarModalAuth", true);
            return "reservar";
        }

        if (vacio(pedido.getProducto()) || vacio(pedido.getFechaEntrega()) || vacio(lugar)) {
            model.addAttribute("error", "Completa el tipo de evento, la fecha y el lugar.");
            return "reservar";
        }

        pedido.setTipo("Catering");
        pedido.setDetalle(unir(
                vacio(invitados) ? "" : invitados + " invitados",
                "Lugar: " + lugar,
                requerimientos));
        pedido.setMonto("Por confirmar");
        pedido.setNombreContacto(cliente.getNombre());
        pedido.setTelefono(cliente.getTelefono());
        pedido.setDireccion(lugar);

        guardar(pedido, cliente);
        flash.addFlashAttribute("exito", "¡Solicitud " + pedido.getCodigo() + " registrada!");
        return "redirect:/cliente/panel";
    }

    // ---------------- Apoyo ----------------

    private void guardar(Pedido pedido, Usuario cliente) {
        long id = Datos.nuevoPedidoId();
        pedido.setId(id);
        pedido.setCodigo(String.format("#PED-%04d", 1000 + id));
        pedido.setEstado("PENDIENTE");
        pedido.setNombreCliente(cliente.getNombre());
        pedido.setEmailCliente(cliente.getEmail());
        Datos.PEDIDOS.add(pedido);
    }

    private String calcularMonto(String nombreProducto, String tamano, int cantidad) {
        Double factor = TAMANOS.get(tamano);
        Producto producto = Datos.PRODUCTOS.stream()
                .filter(p -> p.getNombre().equalsIgnoreCase(nombreProducto))
                .findFirst()
                .orElse(null);

        if (producto == null || factor == null) {
            return "Por confirmar";
        }
        return String.format("S/ %.2f", producto.getPrecio() * factor * cantidad);
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

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private Usuario clienteEnSesion(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        return usuario != null && "cliente".equalsIgnoreCase(usuario.getRol()) ? usuario : null;
    }
}
