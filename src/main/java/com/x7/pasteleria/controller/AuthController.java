package com.x7.pasteleria.controller;

import com.x7.pasteleria.dto.RegistroForm;
import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.Usuario;
import com.x7.pasteleria.repository.UsuarioRepository;
import com.x7.pasteleria.security.SecurityConfig;
import com.x7.pasteleria.security.SesionSeguridad;
import com.x7.pasteleria.security.UsuarioPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Vistas de inicio de sesión y registro.
 * El POST /login y el POST /logout los procesa Spring Security (ver SecurityConfig),
 * por eso aquí solo se muestra el formulario de login.
 */
@Controller
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SesionSeguridad sesionSeguridad;

    public AuthController(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          SesionSeguridad sesionSeguridad) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.sesionSeguridad = sesionSeguridad;
    }

    // ---------------- Iniciar sesión ----------------

    @GetMapping("/login")
    public String login(@AuthenticationPrincipal UsuarioPrincipal usuario, HttpSession session, Model model) {
        if (usuario != null) {
            return "redirect:" + usuario.rutaPanel();
        }
        // Si el intento anterior falló, se vuelve a mostrar el correo que se escribió.
        Object email = session.getAttribute(SecurityConfig.LOGIN_EMAIL);
        session.removeAttribute(SecurityConfig.LOGIN_EMAIL);
        model.addAttribute("email", email == null ? "" : email);
        return "autenticacion/login";
    }

    // ---------------- Crear cuenta ----------------

    @GetMapping("/registro")
    public String registro(@AuthenticationPrincipal UsuarioPrincipal usuario, Model model) {
        if (usuario != null) {
            return "redirect:" + usuario.rutaPanel();
        }
        model.addAttribute("usuario", new RegistroForm());
        return "autenticacion/registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@Valid @ModelAttribute("usuario") RegistroForm form,
                                   BindingResult result,
                                   @RequestParam(defaultValue = "false") boolean terminos,
                                   HttpServletRequest request,
                                   HttpServletResponse response,
                                   RedirectAttributes flash) {

        // 1. Las anotaciones de RegistroForm (@NotBlank, @Email, @Size, @Pattern...) ya se revisaron con @Valid.
        // 2. Reglas que comparan campos o consultan la base de datos: se agregan al mismo BindingResult.
        if (form.getConfirmarPassword() == null) {
            result.rejectValue("confirmarPassword", "usuario.password.confirmar");
        } else if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmarPassword())) {
            result.rejectValue("confirmarPassword", "usuario.password.noCoincide");
        }
        if (form.getEmail() != null && usuarioRepository.existsByEmailIgnoreCase(form.getEmail())) {
            result.rejectValue("email", "usuario.email.duplicado");
        }
        if (!terminos) {
            result.reject("usuario.terminos.obligatorio");
        }

        if (result.hasErrors()) {
            return "autenticacion/registro";
        }

        // La contraseña se guarda cifrada con BCrypt; el rol siempre es CLIENTE (no se toma del formulario).
        Usuario usuario = new Usuario(
                form.getNombre(),
                form.getEmail().toLowerCase(),
                form.getTelefono(),
                form.getDireccion(),
                passwordEncoder.encode(form.getPassword()),
                Rol.CLIENTE);
        Usuario guardado = usuarioRepository.save(usuario);   // INSERT INTO usuario ...

        sesionSeguridad.iniciar(guardado, request, response);   // entra directo a su panel
        flash.addFlashAttribute("exito", "¡Bienvenido, " + guardado.getNombre() + "! Tu cuenta fue creada.");
        return "redirect:/cliente/panel";
    }
}
