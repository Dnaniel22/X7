package com.x7.pasteleria.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

@Entity
@Table(name = "productos")
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre del producto es obligatorio.")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres.")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "Debes seleccionar una categoría.")
    @Column(nullable = false, length = 50)
    private String categoria;

    @Size(max = 50, message = "La etiqueta no puede superar los 50 caracteres.")
    @Column(length = 50)
    private String etiqueta;

    @NotNull(message = "El precio es obligatorio.")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor a S/ 0.00.")
    @DecimalMax(value = "99999.99", message = "El precio no puede superar S/ 99,999.99.")
    @Digits(integer = 5, fraction = 2, message = "El precio admite como máximo 2 decimales.")
    @Column(nullable = false, precision = 8, scale = 2)
    private BigDecimal precio;

    @NotBlank(message = "La descripción es obligatoria.")
    @Size(min = 10, max = 500, message = "La descripción debe tener entre 10 y 500 caracteres.")
    @Column(nullable = false, length = 500)
    private String descripcion;

    @Size(max = 500, message = "La URL de la imagen no puede superar los 500 caracteres.")
    @Column(length = 500)
    private String imagen;

    public Producto() {
    }

    public Producto(String nombre, String categoria, String etiqueta, BigDecimal precio,
                    String descripcion, String imagen) {
        this.nombre = nombre;
        this.categoria = categoria;
        this.etiqueta = etiqueta;
        this.precio = precio;
        this.descripcion = descripcion;
        this.imagen = imagen;
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
}
