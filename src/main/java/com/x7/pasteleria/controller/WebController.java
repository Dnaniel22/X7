package com.x7.pasteleria.controller;

import com.x7.pasteleria.data.Datos;
import com.x7.pasteleria.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String inicio() {
        return "index";
    }

    @GetMapping("/catalogo")
    public String catalogo(Model model) {
        model.addAttribute("productos", Datos.PRODUCTOS);
        return "catalogo";
    }

    @GetMapping("/pedido")
    public String pedido() {
        return "pedido";
    }

    @GetMapping("/reservar")
    public String reservar() {
        return "reservar";
    }

    @GetMapping("/login")
    public String login() {
        return "autenticacion/login";
    }

    @GetMapping("/registro")
    public String registro() {
        return "autenticacion/registro";
    }

    @GetMapping("/cliente/panel")
    public String panelCliente(HttpSession session, Model model) {
        if (!"cliente".equalsIgnoreCase(String.valueOf(session.getAttribute("rol")))) {
            return "redirect:/login";
        }

        String email = String.valueOf(session.getAttribute("email"));
        Usuario usuario = Datos.USUARIOS.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email))
                .findFirst()
                .orElse(null);

        if (usuario == null) {
            session.invalidate();
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        return "panel/panel-cliente";
    }

    @GetMapping("/admin/panel")
    public String panelAdmin(HttpSession session) {
        if (!"administrador".equalsIgnoreCase(String.valueOf(session.getAttribute("rol")))) {
            return "redirect:/login";
        }
        return "panel/panel-admin";
    }
}
