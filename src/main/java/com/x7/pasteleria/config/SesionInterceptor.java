package com.x7.pasteleria.config;

import com.x7.pasteleria.model.Rol;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Protege las rutas privadas: /admin/** solo para el administrador y
 * /cliente/** solo para clientes autenticados.
 */
@Component
public class SesionInterceptor implements HandlerInterceptor {

    private final SesionActual sesionActual;

    public SesionInterceptor(SesionActual sesionActual) {
        this.sesionActual = sesionActual;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        Rol rolRequerido = request.getRequestURI().startsWith("/admin") ? Rol.ADMINISTRADOR : Rol.CLIENTE;

        if (sesionActual.usuarioConRol(request.getSession(), rolRequerido).isPresent()) {
            return true;
        }

        response.sendRedirect("/login?requerido=" + rolRequerido.name());
        return false;
    }
}
