package com.x7.pasteleria.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Datos del formulario "Crear cuenta".
 * La contraseña llega en texto y aquí se validan sus reglas; luego el controlador la cifra con BCrypt
 * y solo el hash se guarda en la entidad Usuario.
 */
public class RegistroForm {

    @NotBlank(message = "{usuario.nombre.obligatorio}")
    @Size(min = 3, max = 100, message = "{usuario.nombre.tamano}")
    private String nombre;

    @NotBlank(message = "{usuario.email.obligatorio}")
    @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "{usuario.email.formato}")
    @Size(max = 120, message = "{usuario.email.tamano}")
    private String email;

    @Pattern(regexp = "^9\\d{8}$", message = "{usuario.telefono.formato}")
    private String telefono;

    @Size(max = 150, message = "{usuario.direccion.tamano}")
    private String direccion;

    // Contraseña segura: de 8 a 60 caracteres, con mayúscula, minúscula, número y símbolo.
    @NotBlank(message = "{usuario.password.obligatorio}")
    @Size(min = 8, max = 60, message = "{usuario.password.tamano}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).*$",
            message = "{usuario.password.formato}")
    private String password;

    private String confirmarPassword;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getConfirmarPassword() { return confirmarPassword; }
    public void setConfirmarPassword(String confirmarPassword) { this.confirmarPassword = confirmarPassword; }
}
