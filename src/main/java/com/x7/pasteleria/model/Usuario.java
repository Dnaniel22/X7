package com.x7.pasteleria.model;

public class Usuario {
    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String direccion;
    private String password;
    private String rol;

    public Usuario() {}

    public Usuario(Long id, String nombre, String email, String telefono,
                   String direccion, String password, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.password = password;
        this.rol = rol;
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
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
