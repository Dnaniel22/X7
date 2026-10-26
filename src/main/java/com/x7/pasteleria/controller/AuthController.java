package com.x7.pasteleria.controller;

import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AuthController {

    private final UsuarioRepository usuarioRepository;

    public AuthController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    /**
     * El formulario de registro solo puede llenar estos campos.
     * Así nadie puede enviar "rol=ADMINISTRADOR" o un "id" desde el navegador.
     */
    @InitBinder("usuario")
    public void camposPermitidos(WebDataBinder binder) {
        binder.setAllowedFields("nombre", "email", "telefono", "direccion", "password", "confirmarPassword");
    }

    // ---------------- Iniciar sesión ----------------

    @GetMapping("/login")
    public String login(HttpSession session, Model model) {
        Usuario enSesion = SesionUsuario.actual(session);
        if (enSesion != null) {
            return "redirect:" + SesionUsuario.rutaPanel(enSesion);
        }
        model.addAttribute("email", "");
        return "autenticacion/login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam(required = false) String email,
                                @RequestParam(required = false) String password,
                                HttpSession session,
                                Model model) {

        model.addAttribute("email", email);
        if (email == null || password == null) {
            model.addAttribute("error", "Ingresa tu correo y tu contraseña.");
            return "autenticacion/login";
        }

        // SELECT ... FROM usuario WHERE upper(email) = upper(?)
        Optional<Usuario> usuario = usuarioRepository.findByEmailIgnoreCase(email)
                .filter(u -> u.getPassword().equals(password));

        if (usuario.isEmpty()) {
            model.addAttribute("error", "Correo o contraseña incorrectos.");
            return "autenticacion/login";
        }

        SesionUsuario.iniciar(session, usuario.get());
        return "redirect:" + SesionUsuario.rutaPanel(usuario.get());
    }

    // ---------------- Crear cuenta ----------------

    @GetMapping("/registro")
    public String registro(HttpSession session, Model model) {
        Usuario enSesion = SesionUsuario.actual(session);
        if (enSesion != null) {
            return "redirect:" + SesionUsuario.rutaPanel(enSesion);
        }
        model.addAttribute("usuario", new Usuario());
        return "autenticacion/registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("usuario") Usuario usuario,
                                   BindingResult result,
                                   @RequestParam(defaultValue = "false") boolean terminos,
                                   HttpSession session,
                                   RedirectAttributes flash) {

        // 1. Las anotaciones de Usuario (@NotBlank, @Email, @Size...) ya se revisaron con @Valid.
        // 2. Reglas que comparan campos o consultan la base de datos: se agregan al mismo BindingResult.
        if (usuario.getConfirmarPassword() == null) {
            result.rejectValue("confirmarPassword", "usuario.password.confirmar");
        } else if (usuario.getPassword() != null && !usuario.getPassword().equals(usuario.getConfirmarPassword())) {
            result.rejectValue("confirmarPassword", "usuario.password.noCoincide");
        }
        if (usuario.getEmail() != null && usuarioRepository.existsByEmailIgnoreCase(usuario.getEmail())) {
            result.rejectValue("email", "usuario.email.duplicado");
        }
        if (!terminos) {
            result.reject("usuario.terminos.obligatorio");
        }

        if (result.hasErrors()) {
            return "autenticacion/registro";
        }

        usuario.setEmail(usuario.getEmail().toLowerCase());
        usuario.setRol(Rol.CLIENTE);
        Usuario guardado = usuarioRepository.save(usuario);   // INSERT INTO usuario ...

        SesionUsuario.iniciar(session, guardado);
        flash.addFlashAttribute("exito", "¡Bienvenido, " + guardado.getNombre() + "! Tu cuenta fue creada.");
        return "redirect:/cliente/panel";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }
}
