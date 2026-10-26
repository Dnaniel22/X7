package com.x7.pasteleria.controller;

import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.InitBinder;

/**
 * Configuración común para todos los formularios.
 * Quita los espacios sobrantes de cada texto y convierte los campos vacíos en null.
 * Así un campo en blanco muestra un solo mensaje ("es obligatorio") y no se guardan
 * cadenas vacías en la base de datos.
 */
@ControllerAdvice
public class FormularioAdvice {

    @InitBinder
    public void limpiarTextos(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }
}
