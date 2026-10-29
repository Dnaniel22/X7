package com.x7.pasteleria.controller.api;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Arrays;
import java.util.Map;

@RestControllerAdvice(basePackageClasses = ApiExceptionHandler.class)
public class ApiExceptionHandler {

    private final ApiRespuestas respuestas;

    public ApiExceptionHandler(ApiRespuestas respuestas) {
        this.respuestas = respuestas;
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> jsonInvalido(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof InvalidFormatException formato && !formato.getPath().isEmpty()) {
            String campo = formato.getPath().get(formato.getPath().size() - 1).getFieldName();
            Class<?> tipo = formato.getTargetType();
            String detalle = tipo.isEnum()
                    ? "Valores permitidos: " + Arrays.toString(tipo.getEnumConstants()) + "."
                    : "Revisa el tipo de dato.";
            return respuestas.error(HttpStatus.BAD_REQUEST,
                    "El valor de \"" + campo + "\" no es válido. " + detalle);
        }
        return respuestas.error(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es un JSON válido.");
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> parametroInvalido(MethodArgumentTypeMismatchException ex) {
        return respuestas.error(HttpStatus.BAD_REQUEST,
                "El parámetro \"" + ex.getName() + "\" no tiene un valor válido.");
    }
}
