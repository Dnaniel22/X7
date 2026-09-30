package com.x7.pasteleria.config;

import com.x7.pasteleria.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Publica el usuario de la sesión en el modelo de todas las vistas, para que el
 * menú de la barra de navegación se construya en el servidor con Thymeleaf.
 */
@ControllerAdvice
public class SesionAdvice {

    private final SesionActual sesionActual;

    public SesionAdvice(SesionActual sesionActual) {
        this.sesionActual = sesionActual;
    }

    @ModelAttribute("usuarioSesion")
    public Usuario usuarioSesion(HttpSession session) {
        return sesionActual.usuario(session).orElse(null);
    }
}
