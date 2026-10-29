package com.x7.pasteleria.dto;

public record ClienteResumen(Long id, String nombre, String email, String telefono,
                             String direccion, long totalPedidos) {
}
