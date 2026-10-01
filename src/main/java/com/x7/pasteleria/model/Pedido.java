package com.x7.pasteleria.model;

public class Pedido {
    private Long id;
    private String codigo;
    private String tipo;
    private String producto;
    private String detalle;
    private String fechaEntrega;
    private String monto;
    private String estado;
    private String nombreContacto;
    private String telefono;
    private String direccion;
    private String nombreCliente;
    private String emailCliente;

    public Pedido() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto; }
    public String getDetalle() { return detalle; }
    public void setDetalle(String detalle) { this.detalle = detalle; }
    public String getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(String fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public String getMonto() { return monto; }
    public void setMonto(String monto) { this.monto = monto; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getNombreContacto() { return nombreContacto; }
    public void setNombreContacto(String nombreContacto) { this.nombreContacto = nombreContacto; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getDireccion() { return direccion; }
    public void setDireccion(String direccion) { this.direccion = direccion; }
    public String getNombreCliente() { return nombreCliente; }
    public void setNombreCliente(String nombreCliente) { this.nombreCliente = nombreCliente; }
    public String getEmailCliente() { return emailCliente; }
    public void setEmailCliente(String emailCliente) { this.emailCliente = emailCliente; }
}
