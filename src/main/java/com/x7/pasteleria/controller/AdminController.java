package com.x7.pasteleria.controller;

import com.x7.pasteleria.model.EstadoPedido;
import com.x7.pasteleria.model.Pedido;
import com.x7.pasteleria.service.PedidoService;
import com.x7.pasteleria.service.ProductoService;
import com.x7.pasteleria.service.UsuarioService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final PedidoService pedidoService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;

    public AdminController(PedidoService pedidoService,
                           ProductoService productoService,
                           UsuarioService usuarioService) {
        this.pedidoService = pedidoService;
        this.productoService = productoService;
        this.usuarioService = usuarioService;
    }

    @GetMapping("/panel")
    public String panel(@RequestParam(required = false) EstadoPedido estado,
                        @RequestParam(required = false) String q,
                        @RequestParam(defaultValue = "pedidos") String tab,
                        Model model) {

        model.addAttribute("pedidos", pedidoService.buscar(estado, q));
        model.addAttribute("productos", productoService.listar());
        model.addAttribute("usuarios", usuarioService.listar());

        model.addAttribute("estados", EstadoPedido.values());
        model.addAttribute("filtroEstado", estado);
        model.addAttribute("filtroTexto", q);
        model.addAttribute("tab", tab);

        model.addAttribute("totalPedidos", pedidoService.total());
        model.addAttribute("totalPendientes", pedidoService.contarPorEstado(EstadoPedido.PENDIENTE));
        model.addAttribute("totalEnPreparacion", pedidoService.contarPorEstado(EstadoPedido.EN_PREPARACION));
        model.addAttribute("totalEntregados", pedidoService.contarPorEstado(EstadoPedido.ENTREGADO));
        model.addAttribute("totalProductos", productoService.total());
        model.addAttribute("totalClientes", usuarioService.totalClientes());

        return "panel/panel-admin";
    }

    @PutMapping("/pedidos/{id}/estado")
    public String actualizarEstado(@PathVariable Long id,
                                   @RequestParam EstadoPedido estado,
                                   RedirectAttributes flash) {

        Optional<Pedido> pedido = pedidoService.buscarPorId(id);
        if (pedido.isEmpty()) {
            flash.addFlashAttribute("error", "El pedido #" + id + " ya no existe.");
            return "redirect:/admin/panel";
        }

        pedidoService.cambiarEstado(pedido.get(), estado);
        flash.addFlashAttribute("exito",
                "El pedido " + pedido.get().getCodigo() + " pasó a « " + estado.getEtiqueta() + " ».");
        return "redirect:/admin/panel";
    }

    @DeleteMapping("/pedidos/{id}")
    public String eliminarPedido(@PathVariable Long id, RedirectAttributes flash) {
        Optional<Pedido> pedido = pedidoService.buscarPorId(id);
        if (pedido.isEmpty()) {
            flash.addFlashAttribute("error", "El pedido #" + id + " ya no existe.");
            return "redirect:/admin/panel";
        }

        String codigo = pedido.get().getCodigo();
        pedidoService.eliminar(id);
        flash.addFlashAttribute("exito", "Se eliminó el pedido " + codigo + ".");
        return "redirect:/admin/panel";
    }
}
