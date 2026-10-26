package com.x7.pasteleria.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Entidad Producto → tabla "producto".
 * Es la oferta del catálogo que administra el personal.
 */
@Entity
@Table(name = "producto",
        uniqueConstraints = @UniqueConstraint(name = "uk_producto_nombre", columnNames = "nombre"))
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "{producto.nombre.obligatorio}")
    @Size(min = 3, max = 80, message = "{producto.nombre.tamano}")
    @Column(nullable = false, length = 80)
    private String nombre;

    @NotBlank(message = "{producto.categoria.obligatoria}")
    @Pattern(regexp = "Tortas|Bocaditos|Buffets|Otros", message = "{producto.categoria.valida}")
    @Column(nullable = false, length = 30)
    private String categoria;

    @Size(max = 30, message = "{producto.etiqueta.tamano}")
    @Column(length = 30)
    private String etiqueta;

    // BigDecimal + precision/scale → columna numeric(10,2): el dinero no pierde decimales.
    @NotNull(message = "{producto.precio.obligatorio}")
    @DecimalMin(value = "0.01", message = "{producto.precio.minimo}")
    @Digits(integer = 8, fraction = 2, message = "{producto.precio.formato}")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @NotBlank(message = "{producto.descripcion.obligatoria}")
    @Size(min = 10, max = 500, message = "{producto.descripcion.tamano}")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Pattern(regexp = "^https?://\\S+$", message = "{producto.imagen.formato}")
    @Size(max = 500, message = "{producto.imagen.tamano}")
    @Column(length = 500)
    private String imagen;

    // Permite ocultar un producto del catálogo sin borrarlo (y sin romper los pedidos que lo usan).
    @Column(nullable = false)
    private boolean disponible = true;

    // Lado inverso de la relación 1 a N: un producto aparece en muchos pedidos.
    @OneToMany(mappedBy = "producto")
    @JsonIgnore
    private List<Pedido> pedidos = new ArrayList<>();

    public Producto() {}

    public Producto(String nombre, String categoria, String etiqueta,
                    BigDecimal precio, String descripcion, String imagen) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.etiqueta = etiqueta;
        this.precio = precio;
        this.descripcion = descripcion;
        this.imagen = imagen;
    }

    /** Copia los datos editables desde otro producto (se usa al actualizar). */
    public void actualizarCon(Producto datos) {
        this.nombre = datos.getNombre();
        this.categoria = datos.getCategoria();
        this.etiqueta = datos.getEtiqueta();
        this.precio = datos.getPrecio();
        this.descripcion = datos.getDescripcion();
        this.imagen = datos.getImagen();
        this.disponible = datos.isDisponible();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public String getEtiqueta() { return etiqueta; }
    public void setEtiqueta(String etiqueta) { this.etiqueta = etiqueta; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public List<Pedido> getPedidos() { return pedidos; }
    public void setPedidos(List<Pedido> pedidos) { this.pedidos = pedidos; }
}
