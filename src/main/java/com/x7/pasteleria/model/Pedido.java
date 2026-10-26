package com.x7.pasteleria.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad Pedido → tabla "pedido".
 * Registra los pedidos de productos y las solicitudes de cotización de catering.
 */
@Entity
@Table(name = "pedido",
        uniqueConstraints = @UniqueConstraint(name = "uk_pedido_codigo", columnNames = "codigo"))
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Código visible (#PED-1001). Se asigna después de guardar, cuando ya existe el id.
    @Column(length = 15)
    private String codigo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TipoPedido tipo;

    // Relación N a 1: muchos pedidos pertenecen a un usuario → clave foránea usuario_id.
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pedido_usuario"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "pedidos", "password"})
    private Usuario usuario;

    // Relación N a 1 opcional: en catering y en pedidos personalizados no hay producto (queda en NULL).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id",
            foreignKey = @ForeignKey(name = "fk_pedido_producto"))
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "pedidos"})
    private Producto producto;

    // Solo en catering: el tipo de evento no es un producto del catálogo.
    @Size(max = 50)
    @Column(name = "tipo_evento", length = 50)
    private String tipoEvento;

    @Size(max = 1000)
    @Column(columnDefinition = "TEXT")
    private String detalle;

    @NotNull
    @Column(name = "fecha_entrega", nullable = false)
    private LocalDate fechaEntrega;

    // NULL mientras el monto esté "Por confirmar" (cotizaciones y pedidos personalizados).
    @DecimalMin(value = "0.01", message = "{pedido.monto.minimo}")
    @Digits(integer = 8, fraction = 2, message = "{pedido.monto.formato}")
    @Column(precision = 10, scale = 2)
    private BigDecimal monto;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoPedido estado = EstadoPedido.PENDIENTE;

    @NotBlank
    @Size(max = 100)
    @Column(name = "nombre_contacto", nullable = false, length = 100)
    private String nombreContacto;

    @NotBlank
    @Pattern(regexp = "^9\\d{8}$")
    @Column(nullable = false, length = 9)
    private String telefono;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String direccion;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro;

    /** Callback de JPA: se ejecuta justo antes del INSERT. */
    @PrePersist
    void alRegistrar() {
        fechaRegistro = LocalDateTime.now();
    }

    /** Texto para las vistas: el producto, el tipo de evento o "Personalizado". No se guarda en la tabla. */
    public String getDescripcion() {
        if (producto != null) return producto.getNombre();
        if (tipoEvento != null) return tipoEvento;
        return "Personalizado";
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public TipoPedido getTipo() { return tipo; }
    public void setTipo(TipoPedido tipo) { this.tipo = tipo; }
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }
    public Producto getProducto() { return producto; }
    public void setProducto(Producto producto) { this.producto = producto; }
    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public LocalDate getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDate fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public String getNombreContacto() { return nombreContacto; }
    public void setNombreContacto(String nombreContacto) { this.nombreContacto = nombreContacto; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public LocalDateTime getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDateTime fechaRegistro) { this.fechaRegistro = fechaRegistro; }
}
