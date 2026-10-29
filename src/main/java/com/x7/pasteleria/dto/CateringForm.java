package com.x7.pasteleria.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public class CateringForm {

    @NotBlank(message = "{catering.tipoEvento.obligatorio}")
    private String tipoEvento;

    @NotNull(message = "{catering.invitados.obligatorio}")
    @Min(value = 10, message = "{catering.invitados.minimo}")
    @Max(value = 2000, message = "{catering.invitados.maximo}")
    private Integer invitados;

    @NotNull(message = "{catering.fecha.obligatoria}")
    @Future(message = "{catering.fecha.futura}")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate fechaEvento;

    @NotBlank(message = "{catering.lugar.obligatorio}")
    @Size(min = 5, max = 150, message = "{catering.lugar.tamano}")
    private String lugar;

    @NotBlank(message = "{pedido.telefono.obligatorio}")
    @Pattern(regexp = "^9\\d{8}$", message = "{pedido.telefono.formato}")
    private String telefono;

    @Size(max = 500, message = "{catering.requerimientos.tamano}")
    private String requerimientos;

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }
    public Integer getInvitados() { return invitados; }
    public void setInvitados(Integer invitados) { this.invitados = invitados; }
    public LocalDate getFechaEvento() { return fechaEvento; }
    public void setFechaEvento(LocalDate fechaEvento) { this.fechaEvento = fechaEvento; }
    public String getLugar() { return lugar; }
    public void setLugar(String lugar) { this.lugar = lugar; }
    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }
    public String getRequerimientos() { return requerimientos; }
    public void setRequerimientos(String requerimientos) { this.requerimientos = requerimientos; }
}
