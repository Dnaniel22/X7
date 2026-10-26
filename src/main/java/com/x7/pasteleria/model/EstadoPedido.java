package com.x7.pasteleria.model;

/**
 * Ciclo de vida de un pedido. Al ser un enum, la base de datos solo admite
 * estos tres valores: no es posible guardar un estado inválido.
 */
public enum EstadoPedido {
    PENDIENTE("PENDIENTE", "bg-warning text-dark"),
    EN_PREPARACION("EN PREPARACIÓN", "bg-info text-dark"),
    ENTREGADO("ENTREGADO", "bg-success");

    /** Texto que se muestra en las vistas. */
    private final String etiqueta;
    /** Clases de Bootstrap de la insignia de color. */
    private final String claseBadge;

    EstadoPedido(String etiqueta, String claseBadge) {
        this.etiqueta = etiqueta;
        this.claseBadge = claseBadge;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getClaseBadge() {
        return claseBadge;
    }
}
