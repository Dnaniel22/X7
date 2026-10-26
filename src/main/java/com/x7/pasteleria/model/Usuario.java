package com.x7.pasteleria.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Usuario → tabla "usuario".
 * Representa a los clientes y al administrador.
 */
@Entity
@Table(name = "usuario",
        uniqueConstraints = @UniqueConstraint(name = "uk_usuario_email", columnNames = "email"))
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{usuario.nombre.obligatorio}")
    @Size(min = 3, max = 100, message = "{usuario.nombre.tamano}")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "{usuario.email.obligatorio}")
    @Email(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "{usuario.email.formato}")
    @Size(max = 120, message = "{usuario.email.tamano}")
    @Column(nullable = false, length = 120)
    private String email;

    // Opcional al registrarse; si se escribe, debe ser un celular peruano (9 dígitos que empiezan en 9).
    @Pattern(regexp = "^9\\d{8}$", message = "{usuario.telefono.formato}")
    @Column(length = 9)
    private String telefono;

    @Size(max = 150, message = "{usuario.direccion.tamano}")
    @Column(length = 150)
    private String direccion;

    // WRITE_ONLY: la API REST puede recibir la contraseña, pero nunca la devuelve en el JSON.
    @NotBlank(message = "{usuario.password.obligatorio}")
    @Size(min = 6, max = 60, message = "{usuario.password.tamano}")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(nullable = false, length = 60)
    private String password;

    @NotNull(message = "{usuario.rol.obligatorio}")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol = Rol.CLIENTE;

    // Solo existe en el formulario de registro: @Transient evita que se cree una columna.
    @Transient
    @JsonIgnore
    private String confirmarPassword;

    // Lado inverso de la relación 1 a N: un usuario registra muchos pedidos.
    // La clave foránea (usuario_id) está en la tabla pedido.
    @OneToMany(mappedBy = "usuario")
    @JsonIgnore
    private List<Pedido> pedidos = new ArrayList<>();

    public Usuario() {}

    public Usuario(String nombre, String email, String telefono, String direccion, String password, Rol rol) {
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.password = password;
        this.rol = rol;
    }

    @JsonIgnore
    public boolean isAdministrador() {
        return rol == Rol.ADMINISTRADOR;
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
    public String getConfirmarPassword() { return confirmarPassword; }
    public void setConfirmarPassword(String confirmarPassword) { this.confirmarPassword = confirmarPassword; }
    public List<Pedido> getPedidos() { return pedidos; }
    public void setPedidos(List<Pedido> pedidos) { this.pedidos = pedidos; }
}
