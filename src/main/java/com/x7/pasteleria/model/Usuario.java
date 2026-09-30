package com.x7.pasteleria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre completo es obligatorio.")
    @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres.")
    @Column(nullable = false, length = 80)
    private String nombre;

    @NotBlank(message = "El correo electrónico es obligatorio.")
    @Email(message = "Ingresa un correo electrónico válido.")
    @Size(max = 120, message = "El correo no puede superar los 120 caracteres.")
    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Pattern(regexp = "^$|^9\\d{8}$", message = "El teléfono debe tener 9 dígitos y empezar con 9.")
    @Column(length = 15)
    private String telefono;

    @Size(max = 150, message = "La dirección no puede superar los 150 caracteres.")
    @Column(length = 150)
    private String direccion;

    @NotBlank(message = "La contraseña es obligatoria.")
    @Column(nullable = false, length = 120)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    public Usuario() {
    }

    public Usuario(String nombre, String email, String telefono, String direccion, String password, Rol rol) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.password = password;
        this.rol = rol;
    }

    public boolean esAdministrador() {
        return rol == Rol.ADMINISTRADOR;
    }

    public String getPrimerNombre() {
        return nombre == null || nombre.isBlank() ? "Cliente" : nombre.trim().split("\\s+")[0];
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
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
    public Rol getRol() { return rol; }
    public void setRol(Rol rol) { this.rol = rol; }
}
