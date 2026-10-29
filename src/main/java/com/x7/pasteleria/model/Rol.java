package com.x7.pasteleria.model;

/**
 * Roles del sistema. Se guardan como texto (CLIENTE / ADMINISTRADOR)
 * gracias a @Enumerated(EnumType.STRING) en la entidad Usuario.
 */
public enum Rol {
    CLIENTE("Cliente"),
    ADMINISTRADOR("Administrador");

    private final String etiqueta;

    Rol(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
