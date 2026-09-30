package com.x7.pasteleria.controller;

import com.x7.pasteleria.config.SesionActual;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.service.PedidoService;
import com.x7.pasteleria.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;
    private final PedidoService pedidoService;
    private final SesionActual sesionActual;

    public UsuarioController(UsuarioService usuarioService,
                             PedidoService pedidoService,
                             SesionActual sesionActual) {
        this.usuarioService = usuarioService;
        this.pedidoService = pedidoService;
        this.sesionActual = sesionActual;
    }

    @DeleteMapping("/{id}")
    public String eliminar(@PathVariable Long id, HttpSession session, RedirectAttributes flash) {
        Optional<Usuario> usuario = usuarioService.buscarPorId(id);
        if (usuario.isEmpty()) {
            flash.addFlashAttribute("error", "El usuario #" + id + " ya no existe.");
            return destino();
        }

        Usuario objetivo = usuario.get();

        boolean esUnoMismo = sesionActual.usuario(session)
                .map(actual -> actual.getId().equals(id))
                .orElse(false);

        if (esUnoMismo || objetivo.esAdministrador()) {
            flash.addFlashAttribute("error", "No se puede eliminar una cuenta de administrador.");
            return destino();
        }

        long pedidos = pedidoService.contarDeCliente(objetivo);
        if (pedidos > 0) {
            flash.addFlashAttribute("error",
                    "No se puede eliminar a " + objetivo.getNombre() + ": tiene " + pedidos
                            + " pedido(s) registrados. Elimina primero sus pedidos.");
            return destino();
        }

        usuarioService.eliminar(id);
        flash.addFlashAttribute("exito", "Se eliminó la cuenta de " + objetivo.getNombre() + ".");
        return destino();
    }

    private String destino() {
        return "redirect:/admin/panel?tab=usuarios";
    }
}
