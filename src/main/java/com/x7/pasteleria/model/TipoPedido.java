package com.x7.pasteleria.model;

/** Un registro de la tabla pedido puede ser un pedido de producto o una cotización de catering. */
public enum TipoPedido {
    PEDIDO("Pedido"),
    CATERING("Catering");

    private final String etiqueta;

    TipoPedido(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
