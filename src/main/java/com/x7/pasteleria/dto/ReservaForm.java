package com.x7.pasteleria.dto;

import jakarta.validation.constraints.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class ReservaForm {

    @NotBlank(message = "Selecciona el tipo de evento.")
    @Size(max = 120)
    private String tipoEvento;

    @NotNull(message = "Indica el número de invitados.")
    @Min(value = 10, message = "Para menos de 10 invitados usa el formulario de pedidos.")
    @Max(value = 2000, message = "Para más de 2000 invitados contáctanos directamente.")
    private Integer invitados;

    @NotNull(message = "Indica la fecha del evento.")
    @Future(message = "La fecha del evento debe ser posterior al día de hoy.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaEvento;

    @NotBlank(message = "Indica el lugar o local del evento.")
    @Size(min = 5, max = 150, message = "El lugar debe tener entre 5 y 150 caracteres.")
    private String lugar;

    @Size(max = 300, message = "Los requerimientos no pueden superar los 300 caracteres.")
    private String requerimientos;

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public Integer getInvitados() { return invitados; }
    public void setInvitados(Integer invitados) { this.invitados = invitados; }
    public LocalDate getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDate fechaEvento) { this.fechaEvento = fechaEvento; }
    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }
    public String getRequerimientos() { return requerimientos; }
    public void setRequerimientos(String requerimientos) { this.requerimientos = requerimientos; }
}
