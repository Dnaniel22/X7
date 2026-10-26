package com.x7.pasteleria.dto;

/** Fila de la pestaña "Clientes" del panel administrador (resultado de una consulta JPQL). */
public record ClienteResumen(Long id, String nombre, String email, String telefono,
                             String direccion, long totalPedidos) {
}
