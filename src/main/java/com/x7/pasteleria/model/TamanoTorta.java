package com.x7.pasteleria.model;

import java.math.BigDecimal;

public enum TamanoTorta {
    PEQUENA("Pequeña (6-8 porciones)", new BigDecimal("1.0")),
    MEDIANA("Mediana (12-15 porciones)", new BigDecimal("1.5")),
    GRANDE("Grande (20-25 porciones)", new BigDecimal("2.0")),
    EXTRA_GRANDE("Extra Grande (30-40 porciones)", new BigDecimal("3.0"));

    private final String etiqueta;
    private final BigDecimal factor;

    TamanoTorta(String etiqueta, BigDecimal factor) {
        this.etiqueta = etiqueta;
        this.factor = factor;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public BigDecimal getFactor() {
        return factor;
    }
}
