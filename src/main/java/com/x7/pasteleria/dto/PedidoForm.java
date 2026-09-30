package com.x7.pasteleria.dto;

import com.x7.pasteleria.model.TamanoTorta;
import com.x7.pasteleria.model.Usuario;
import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class PedidoForm {

    public static final String PRODUCTO_PERSONALIZADO = "Otro / Personalizado";

    @NotBlank(message = "El nombre de contacto es obligatorio.")
    @Size(min = 3, max = 80, message = "El nombre debe tener entre 3 y 80 caracteres.")
    private String nombreContacto;

    @NotBlank(message = "El teléfono es obligatorio.")
    @Pattern(regexp = "9\\d{8}", message = "El teléfono debe tener 9 dígitos y empezar con 9.")
    private String telefono;

    @NotBlank(message = "La dirección de entrega es obligatoria.")
    @Size(min = 5, max = 150, message = "La dirección debe tener entre 5 y 150 caracteres.")
    private String direccion;

    @NotBlank(message = "Selecciona el producto que deseas encargar.")
    @Size(max = 120)
    private String producto;

    @NotNull(message = "Indica la fecha de entrega.")
    @Future(message = "La fecha de entrega debe ser posterior al día de hoy.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaEntrega;

    @NotNull(message = "Selecciona el tamaño.")
    private TamanoTorta tamano;

    @NotNull(message = "Indica la cantidad.")
    @Min(value = 1, message = "La cantidad mínima es 1.")
    @Max(value = 50, message = "Para más de 50 unidades solicita una cotización de catering.")
    private Integer cantidad = 1;

    @Size(max = 120, message = "Los colores o temática no pueden superar los 120 caracteres.")
    private String colores;

    @Size(max = 300, message = "Los detalles especiales no pueden superar los 300 caracteres.")
    private String detalles;

    public PedidoForm() {
    }

    public static PedidoForm paraCliente(Usuario cliente, String productoPreseleccionado) {
        PedidoForm form = new PedidoForm();
        if (cliente != null) {
            form.nombreContacto = cliente.getNombre();
            form.telefono = cliente.getTelefono();
            form.direccion = cliente.getDireccion();
        }
        form.producto = productoPreseleccionado;
        return form;
    }

    public boolean esPersonalizado() {
        return PRODUCTO_PERSONALIZADO.equals(producto);
    }

    public String getNombreContacto() { return nombreContacto; }
    public void setNombreContacto(String nombreContacto) { this.nombreContacto = nombreContacto; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto; }
    public LocalDate getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDate fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public TamanoTorta getTamano() { return tamano; }
    public void setTamano(TamanoTorta tamano) { this.tamano = tamano; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public String getColores() { return colores; }
    public void setColores(String colores) { this.colores = colores; }
    public String getDetalles() { return detalles; }
    public void setDetalles(String detalles) { this.detalles = detalles; }
}
