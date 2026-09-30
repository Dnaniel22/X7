package com.x7.pasteleria.controller;

import com.x7.pasteleria.config.SesionActual;
import com.x7.pasteleria.dto.LoginForm;
import com.x7.pasteleria.dto.RegistroForm;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.service.UsuarioService;
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

import java.util.Optional;

@Controller
public class AuthController {

    private final UsuarioService usuarioService;
    private final SesionActual sesionActual;

    public AuthController(UsuarioService usuarioService, SesionActual sesionActual) {
        this.usuarioService = usuarioService;
        this.sesionActual = sesionActual;
    }

    @GetMapping("/login")
    public String login(@RequestParam(required = false) String requerido,
                        HttpSession session,
                        Model model) {

        Optional<Usuario> yaAutenticado = sesionActual.usuario(session);
        if (yaAutenticado.isPresent()) {
            return "redirect:" + rutaPanel(yaAutenticado.get());
        }

        model.addAttribute("loginForm", new LoginForm());
        model.addAttribute("requerido", requerido);
        return "autenticacion/login";
    }

    @PostMapping("/login")
    public String procesarLogin(@Valid @ModelAttribute("loginForm") LoginForm loginForm,
                                BindingResult resultado,
                                HttpSession session) {

        if (resultado.hasErrors()) {
            return "autenticacion/login";
        }

        Optional<Usuario> usuario = usuarioService.autenticar(loginForm.getEmail(), loginForm.getPassword());
        if (usuario.isEmpty()) {
            resultado.reject("credenciales", "Correo o contraseña incorrectos.");
            return "autenticacion/login";
        }

        sesionActual.iniciar(session, usuario.get());
        return "redirect:" + rutaPanel(usuario.get());
    }

    @GetMapping("/registro")
    public String registro(HttpSession session, Model model) {
        Optional<Usuario> yaAutenticado = sesionActual.usuario(session);
        if (yaAutenticado.isPresent()) {
            return "redirect:" + rutaPanel(yaAutenticado.get());
        }

        model.addAttribute("registroForm", new RegistroForm());
        return "autenticacion/registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("registroForm") RegistroForm registroForm,
                                   BindingResult resultado,
                                   HttpSession session,
                                   RedirectAttributes flash) {

        if (!registroForm.passwordsCoinciden()) {
            resultado.rejectValue("password2", "noCoincide", "Las contraseñas no coinciden.");
        }

        if (registroForm.getEmail() != null && usuarioService.emailRegistrado(registroForm.getEmail())) {
            resultado.rejectValue("email", "duplicado", "Ya existe una cuenta registrada con ese correo.");
        }

        if (resultado.hasErrors()) {
            return "autenticacion/registro";
        }

        Usuario nuevo = usuarioService.registrarCliente(registroForm);
        sesionActual.iniciar(session, nuevo);
        flash.addFlashAttribute("exito", "¡Cuenta creada! Bienvenido a Pastelería X7, " + nuevo.getPrimerNombre() + ".");
        return "redirect:/cliente/panel";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        sesionActual.cerrar(session);
        return "redirect:/";
    }

    private String rutaPanel(Usuario usuario) {
        return usuario.esAdministrador() ? "/admin/panel" : "/cliente/panel";
    }
}
