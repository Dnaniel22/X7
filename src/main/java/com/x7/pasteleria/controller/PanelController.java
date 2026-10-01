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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Comparator;
import java.util.List;

@Controller
public class PanelController {

    private static final List<String> CATEGORIAS = List.of("Tortas", "Bocaditos", "Buffets", "Otros");
    private static final List<String> ESTADOS = List.of("PENDIENTE", "EN PREPARACIÓN", "ENTREGADO");

    // ---------------- Panel del cliente ----------------

    @GetMapping("/cliente/panel")
    public String panelCliente(HttpSession session, Model model) {
        Usuario cliente = usuarioConRol(session, "cliente");
        if (cliente == null) {
            return "redirect:/login";
        }

        List<Pedido> mios = Datos.PEDIDOS.stream()
                .filter(p -> cliente.getEmail().equalsIgnoreCase(p.getEmailCliente()))
                .sorted(Comparator.comparing(Pedido::getId).reversed())
                .toList();

        model.addAttribute("usuario", cliente);
        model.addAttribute("pedidos", mios);
        return "panel/panel-cliente";
    }

    // ---------------- Panel del administrador ----------------

    @GetMapping("/admin/panel")
    public String panelAdmin(@RequestParam(defaultValue = "pedidos") String tab,
                             HttpSession session,
                             Model model) {

        if (usuarioConRol(session, "administrador") == null) {
            return "redirect:/login";
        }

        List<Pedido> pedidos = Datos.PEDIDOS.stream()
                .sorted(Comparator.comparing(Pedido::getId).reversed())
                .toList();

        model.addAttribute("pedidos", pedidos);
        model.addAttribute("productos", Datos.PRODUCTOS);
        model.addAttribute("estados", ESTADOS);
        model.addAttribute("tab", tab);
        if (!model.containsAttribute("producto")) {
            model.addAttribute("producto", new Producto());
        }
        return "panel/panel-admin";
    }

    @PostMapping("/admin/productos")
    public String agregarProducto(@ModelAttribute("producto") Producto producto,
                                  HttpSession session,
                                  RedirectAttributes flash) {

        if (usuarioConRol(session, "administrador") == null) {
            return "redirect:/login";
        }

        if (vacio(producto.getNombre()) || vacio(producto.getCategoria())
                || vacio(producto.getDescripcion()) || producto.getPrecio() <= 0) {
            flash.addFlashAttribute("error", "Completa correctamente los datos del producto.");
            return "redirect:/admin/panel?tab=productos";
        }

        producto.setId(Datos.nuevoProductoId());
        producto.setNombre(producto.getNombre().trim());
        if (vacio(producto.getEtiqueta())) {
            producto.setEtiqueta(producto.getCategoria());
        }
        Datos.PRODUCTOS.add(producto);

        flash.addFlashAttribute("exito", "Se agregó « " + producto.getNombre() + " » al catálogo.");
        return "redirect:/admin/panel?tab=productos";
    }

    @PostMapping("/admin/productos/{id}/eliminar")
    public String eliminarProducto(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (usuarioConRol(session, "administrador") == null) {
            return "redirect:/login";
        }

        boolean eliminado = Datos.PRODUCTOS.removeIf(p -> p.getId().equals(id));
        flash.addFlashAttribute(eliminado ? "exito" : "error",
                eliminado ? "Producto eliminado del catálogo." : "Ese producto ya no existe.");
        return "redirect:/admin/panel?tab=productos";
    }

    @PostMapping("/admin/pedidos/{id}/estado")
    public String cambiarEstado(@PathVariable Long id,
                                @RequestParam String estado,
                                HttpSession session,
                                RedirectAttributes flash) {

        if (usuarioConRol(session, "administrador") == null) {
            return "redirect:/login";
        }

        Pedido pedido = Datos.PEDIDOS.stream()
                .filter(p -> p.getId().equals(id))
                .findFirst()
                .orElse(null);

        if (pedido == null || !ESTADOS.contains(estado)) {
            flash.addFlashAttribute("error", "No se pudo actualizar el pedido.");
            return "redirect:/admin/panel";
        }

        pedido.setEstado(estado);
        flash.addFlashAttribute("exito", "El pedido " + pedido.getCodigo() + " pasó a « " + estado + " ».");
        return "redirect:/admin/panel";
    }

    @PostMapping("/admin/pedidos/{id}/eliminar")
    public String eliminarPedido(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        if (usuarioConRol(session, "administrador") == null) {
            return "redirect:/login";
        }

        boolean eliminado = Datos.PEDIDOS.removeIf(p -> p.getId().equals(id));
        flash.addFlashAttribute(eliminado ? "exito" : "error",
                eliminado ? "Pedido eliminado." : "Ese pedido ya no existe.");
        return "redirect:/admin/panel";
    }

    @ModelAttribute("categorias")
    public List<String> categorias() {
        return CATEGORIAS;
    }

    private Usuario usuarioConRol(HttpSession session, String rol) {
        Usuario usuario = (Usuario) session.getAttribute("usuario");
        return usuario != null && rol.equalsIgnoreCase(usuario.getRol()) ? usuario : null;
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }
}
