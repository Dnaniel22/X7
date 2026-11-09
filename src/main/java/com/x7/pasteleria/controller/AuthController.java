package com.x7.pasteleria.controller;

import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
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

@Controller
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
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

    // Solo se muestra el formulario: el POST /login lo procesa Spring Security (ver SecurityConfig).
    @GetMapping("/login")
    public String login(HttpSession session) {
        Usuario enSesion = SesionUsuario.actual(session);
        if (enSesion != null) {
            return "redirect:" + SesionUsuario.rutaPanel(enSesion);
        }
        return "autenticacion/login";
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
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));   // se guarda cifrada con BCrypt
        usuario.setRol(Rol.CLIENTE);
        Usuario guardado = usuarioRepository.save(usuario);   // INSERT INTO usuario ...

        flash.addFlashAttribute("exito", "¡Bienvenido, " + guardado.getNombre() + "! Tu cuenta fue creada. Inicia sesión.");
        return "redirect:/login";
    }

    // POST /logout también lo procesa Spring Security.
}
