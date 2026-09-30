package com.x7.pasteleria.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegistroForm {

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

    @NotBlank(message = "La contraseña es obligatoria.")
    @Size(min = 6, max = 60, message = "La contraseña debe tener al menos 6 caracteres.")
    private String password;

    @NotBlank(message = "Debes confirmar tu contraseña.")
    private String password2;

    @AssertTrue(message = "Debes aceptar los términos y condiciones para continuar.")
    private boolean terminos;

    public boolean passwordsCoinciden() {
        return password != null && password.equals(password2);
    }

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
    public String getPassword2() { return password2; }
    public void setPassword2(String password2) { this.password2 = password2; }
    public boolean isTerminos() { return terminos; }
    public void setTerminos(boolean terminos) { this.terminos = terminos; }
}
