package com.x7.pasteleria.dto;

import com.x7.pasteleria.model.Usuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PerfilForm {

    @NotBlank(message = "El nombre completo es obligatorio.")
    @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres.")
    private String nombre;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "Ingresa un correo electrónico válido.")
    @Size(max = 120, message = "El correo no puede superar los 120 caracteres.")
    private String email;

    @NotBlank(message = "El teléfono es obligatorio.")
    @Pattern(regexp = "9\\d{8}", message = "El teléfono debe tener 9 dígitos y empezar con 9.")
    private String telefono;

    @NotBlank(message = "La dirección es obligatoria.")
    @Size(min = 5, max = 150, message = "La dirección debe tener entre 5 y 150 caracteres.")
    private String direccion;

    @Pattern(regexp = "^$|^.{6,60}$",
            message = "La nueva contraseña debe tener al menos 6 caracteres (déjala vacía para no cambiarla).")
    private String passwordNuevo = "";

    public PerfilForm() {
    }

    public static PerfilForm desde(Usuario usuario) {
        PerfilForm form = new PerfilForm();
        form.nombre = usuario.getNombre();
        form.email = usuario.getEmail();
        form.telefono = usuario.getTelefono();
        form.direccion = usuario.getDireccion();
        return form;
    }

    public boolean cambiaPassword() {
        return passwordNuevo != null && !passwordNuevo.isBlank();
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getPasswordNuevo() { return passwordNuevo; }
    public void setPasswordNuevo(String passwordNuevo) { this.passwordNuevo = passwordNuevo; }
}
