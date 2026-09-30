package com.x7.pasteleria.model;

public enum EstadoPedido {
    PENDIENTE("Pendiente", "bg-warning text-dark"),
    EN_PREPARACION("En preparación", "bg-info text-dark"),
    ENTREGADO("Entregado", "bg-success");

    private final String etiqueta;
    private final String badge;

    EstadoPedido(String etiqueta, String badge) {
        this.etiqueta = etiqueta;
        this.badge = badge;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getBadge() {
        return badge;
    }

    public EstadoPedido getSiguiente() {
        return switch (this) {
            case PENDIENTE -> EN_PREPARACION;
            case EN_PREPARACION, ENTREGADO -> ENTREGADO;
        };
    }

    public boolean isFinal() {
        return this == ENTREGADO;
    }
}
