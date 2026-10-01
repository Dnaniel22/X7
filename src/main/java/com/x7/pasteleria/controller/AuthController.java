package com.x7.pasteleria.controller;

import com.x7.pasteleria.data.Datos;
import com.x7.pasteleria.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    // ---------------- Iniciar sesión ----------------

    @GetMapping("/login")
    public String login(HttpSession session, Model model) {
        Usuario enSesion = (Usuario) session.getAttribute("usuario");
        if (enSesion != null) {
            return "redirect:" + rutaPanel(enSesion);
        }
        model.addAttribute("email", "");
        return "autenticacion/login";
    }

    @PostMapping("/login")
    public String procesarLogin(@RequestParam String email,
                                @RequestParam String password,
                                HttpSession session,
                                Model model) {

        Usuario usuario = Datos.USUARIOS.stream()
                .filter(u -> u.getEmail().equalsIgnoreCase(email.trim())
                        && u.getPassword().equals(password))
                .findFirst()
                .orElse(null);

        if (usuario == null) {
            model.addAttribute("error", "Correo o contraseña incorrectos.");
            model.addAttribute("email", email);
            return "autenticacion/login";
        }

        session.setAttribute("usuario", usuario);
        return "redirect:" + rutaPanel(usuario);
    }

    // ---------------- Crear cuenta ----------------

    @GetMapping("/registro")
    public String registro(HttpSession session, Model model) {
        Usuario enSesion = (Usuario) session.getAttribute("usuario");
        if (enSesion != null) {
            return "redirect:" + rutaPanel(enSesion);
        }
        model.addAttribute("usuario", new Usuario());
        return "autenticacion/registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute("usuario") Usuario usuario,
                                   @RequestParam(defaultValue = "") String password2,
                                   HttpSession session,
                                   Model model) {

        String error = revisarRegistro(usuario, password2);
        if (error != null) {
            model.addAttribute("error", error);
            return "autenticacion/registro";
        }

        usuario.setId(Datos.nuevoUsuarioId());
        usuario.setNombre(usuario.getNombre().trim());
        usuario.setEmail(usuario.getEmail().trim().toLowerCase());
        usuario.setRol("cliente");
        Datos.USUARIOS.add(usuario);

        session.setAttribute("usuario", usuario);
        return "redirect:/cliente/panel";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/";
    }

    /** Devuelve el mensaje de error, o null si los datos son correctos. */
    private String revisarRegistro(Usuario usuario, String password2) {
        if (vacio(usuario.getNombre()) || vacio(usuario.getEmail()) || vacio(usuario.getPassword())) {
            return "Nombre, correo y contraseña son obligatorios.";
        }
        if (!usuario.getPassword().equals(password2)) {
            return "Las contraseñas no coinciden.";
        }
        boolean existe = Datos.USUARIOS.stream()
                .anyMatch(u -> u.getEmail().equalsIgnoreCase(usuario.getEmail().trim()));
        if (existe) {
            return "Ya existe una cuenta registrada con ese correo.";
        }
        return null;
    }

    private boolean vacio(String texto) {
        return texto == null || texto.isBlank();
    }

    private String rutaPanel(Usuario usuario) {
        return "administrador".equalsIgnoreCase(usuario.getRol()) ? "/admin/panel" : "/cliente/panel";
    }
}
