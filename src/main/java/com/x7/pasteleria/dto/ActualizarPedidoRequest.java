package com.x7.pasteleria.dto;

import com.x7.pasteleria.model.EstadoPedido;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Cuerpo JSON de PUT /api/pedidos/{id}: el administrador actualiza el estado
 * y, si corresponde, confirma el monto de una cotización.
 */
public record ActualizarPedidoRequest(
        @NotNull(message = "{pedido.estado.obligatorio}")
        EstadoPedido estado,

        @DecimalMin(value = "0.01", message = "{pedido.monto.minimo}")
        @Digits(integer = 8, fraction = 2, message = "{pedido.monto.formato}")
        BigDecimal monto) {
}
