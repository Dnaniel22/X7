package com.x7.pasteleria.security;

import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.Usuario;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Usuario autenticado tal como lo ve Spring Security.
 * Se guarda en el SecurityContext (en la sesión para la web, en cada petición para la API con JWT).
 * En las vistas se lee con sec:authentication="principal.nombre".
 */
public class UsuarioPrincipal implements UserDetails, CredentialsContainer {

    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String nombre;
    private final String email;
    private String password;
    private final Rol rol;
    private final boolean activo;

    private UsuarioPrincipal(Long id, String nombre, String email, String password, Rol rol, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.password = password;
        this.rol = rol;
        this.activo = activo;
    }

    public static UsuarioPrincipal desde(Usuario usuario) {
        return new UsuarioPrincipal(usuario.getId(), usuario.getNombre(), usuario.getEmail(),
                usuario.getPassword(), usuario.getRol(), usuario.isActivo());
    }

    /** El rol se convierte en la autoridad ROLE_CLIENTE o ROLE_ADMINISTRADOR, que es lo que revisa hasRole(...). */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + rol.name()));
    }

    /** El correo es el nombre de usuario con el que se inicia sesión. */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    /** Una cuenta desactivada por el administrador no puede iniciar sesión ni usar su token. */
    @Override
    public boolean isEnabled() {
        return activo;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** Spring Security llama a este método después del login para no dejar el hash en memoria. */
    @Override
    public void eraseCredentials() {
        this.password = null;
    }

    public boolean isAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }

    /** Página a la que se envía al usuario después de iniciar sesión. */
    public String rutaPanel() {
        return isAdministrador() ? "/admin/panel" : "/cliente/panel";
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public Rol getRol() { return rol; }
}
