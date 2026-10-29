package com.x7.pasteleria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class PerfilForm {

    @NotBlank(message = "{usuario.nombre.obligatorio}")
    @Size(min = 3, max = 100, message = "{usuario.nombre.tamano}")
    private String nombre;

    @Pattern(regexp = "^9\\d{8}$", message = "{usuario.telefono.formato}")
    private String telefono;

    @Size(max = 150, message = "{usuario.direccion.tamano}")
    private String direccion;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
}
