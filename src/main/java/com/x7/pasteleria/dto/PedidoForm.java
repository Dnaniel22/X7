package com.x7.pasteleria.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/**
 * Datos del formulario "Encargar Pedido".
 * Se usa una clase aparte porque el formulario tiene campos (tamaño, cantidad, colores)
 * que no son columnas de la tabla pedido: el controlador los combina en el detalle.
 */
public class PedidoForm {

    @NotBlank(message = "{pedido.contacto.obligatorio}")
    @Size(min = 3, max = 100, message = "{pedido.contacto.tamano}")
    private String nombreContacto;

    @NotBlank(message = "{pedido.telefono.obligatorio}")
    @Pattern(regexp = "^9\\d{8}$", message = "{pedido.telefono.formato}")
    private String telefono;

    @NotBlank(message = "{pedido.direccion.obligatoria}")
    @Size(min = 5, max = 200, message = "{pedido.direccion.tamano}")
    private String direccion;

    /** Id del producto del catálogo; 0 significa "Otro / Personalizado". */
    @NotNull(message = "{pedido.producto.obligatorio}")
    private Long productoId;

    @NotNull(message = "{pedido.fecha.obligatoria}")
    @Future(message = "{pedido.fecha.futura}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaEntrega;

    /** Solo para tortas: el controlador lo exige cuando el producto es de la categoría "Tortas". */
    private String tamano;

    @NotNull(message = "{pedido.cantidad.obligatoria}")
    @Min(value = 1, message = "{pedido.cantidad.minima}")
    @Max(value = 50, message = "{pedido.cantidad.maxima}")
    private Integer cantidad = 1;

    @Size(max = 100, message = "{pedido.colores.tamano}")
    private String colores;

    @Size(max = 500, message = "{pedido.detalles.tamano}")
    private String detalles;

    public String getNombreContacto() { return nombreContacto; }
    public void setNombreContacto(String nombreContacto) { this.nombreContacto = nombreContacto; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public Long getProductoId() { return productoId; }
    public void setProductoId(Long productoId) { this.productoId = productoId; }
    public LocalDate getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDate fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public String getTamano() { return tamano; }
    public void setTamano(String tamano) { this.tamano = tamano; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public String getColores() { return colores; }
    public void setColores(String colores) { this.colores = colores; }
    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }
}
