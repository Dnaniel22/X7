package com.x7.pasteleria.model;

public class Producto {
    private Long id;
    private String nombre;
    private String categoria;
    private String etiqueta;
    private double precio;
    private String descripcion;
    private String imagen;

    public Producto() {}

    public Producto(Long id, String nombre, String categoria, String etiqueta,
                    double precio, String descripcion, String imagen) {
        this.id = id;
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
    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
}
