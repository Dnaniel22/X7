package com.x7.pasteleria.controller;

import com.x7.pasteleria.config.SesionActual;
import com.x7.pasteleria.dto.PerfilForm;
import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.service.PedidoService;
import com.x7.pasteleria.service.UsuarioService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.NoSuchElementException;

@Controller
@RequestMapping("/cliente")
public class ClienteController {

    private final PedidoService pedidoService;
    private final UsuarioService usuarioService;
    private final SesionActual sesionActual;

    public ClienteController(PedidoService pedidoService,
                             UsuarioService usuarioService,
                             SesionActual sesionActual) {
        this.pedidoService = pedidoService;
        this.usuarioService = usuarioService;
        this.sesionActual = sesionActual;
    }

    @GetMapping("/panel")
    public String panel(HttpSession session, Model model) {
        Usuario cliente = clienteAutenticado(session);
        model.addAttribute("usuario", cliente);
        model.addAttribute("pedidos", pedidoService.listarDeCliente(cliente));
        return "panel/panel-cliente";
    }

    @GetMapping("/perfil")
    public String perfil(HttpSession session, Model model) {
        Usuario cliente = clienteAutenticado(session);
        model.addAttribute("usuario", cliente);
        model.addAttribute("perfilForm", PerfilForm.desde(cliente));
        return "panel/perfil-cliente";
    }

    @PutMapping("/perfil")
    public String actualizarPerfil(@Valid @ModelAttribute("perfilForm") PerfilForm perfilForm,
                                   BindingResult resultado,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes flash) {

        Usuario cliente = clienteAutenticado(session);

        if (perfilForm.getEmail() != null
                && usuarioService.emailOcupadoPorOtro(perfilForm.getEmail(), cliente.getId())) {
            resultado.rejectValue("email", "duplicado", "Ese correo ya está registrado por otra cuenta.");
        }

        if (resultado.hasErrors()) {
            model.addAttribute("usuario", cliente);
            return "panel/perfil-cliente";
        }

        usuarioService.actualizarPerfil(cliente, perfilForm);
        flash.addFlashAttribute("exito", "Tus datos se actualizaron correctamente.");
        return "redirect:/cliente/panel";
    }

    private Usuario clienteAutenticado(HttpSession session) {
        return sesionActual.usuarioConRol(session, Rol.CLIENTE)
                .orElseThrow(() -> new NoSuchElementException("No hay una sesión de cliente activa."));
    }
}
