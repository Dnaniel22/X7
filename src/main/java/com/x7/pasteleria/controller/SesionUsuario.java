package com.x7.pasteleria.controller;

import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.Usuario;
import jakarta.servlet.http.HttpSession;

/** Lectura y escritura del usuario autenticado en HttpSession (vistas y API REST). */
public final class SesionUsuario {

    public static final String ATRIBUTO = "usuario";

    private SesionUsuario() {}

    public static Usuario actual(HttpSession session) {
        return (Usuario) session.getAttribute(ATRIBUTO);
    }

    /** Devuelve el usuario en sesión solo si tiene el rol indicado; si no, null. */
    public static Usuario conRol(HttpSession session, Rol rol) {
        Usuario usuario = actual(session);
        return usuario != null && usuario.getRol() == rol ? usuario : null;
    }

    public static void iniciar(HttpSession session, Usuario usuario) {
        session.setAttribute(ATRIBUTO, usuario);
    }

    public static String rutaPanel(Usuario usuario) {
        return usuario.isAdministrador() ? "/admin/panel" : "/cliente/panel";
    }
}
