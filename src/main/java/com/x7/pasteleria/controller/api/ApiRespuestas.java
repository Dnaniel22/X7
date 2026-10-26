package com.x7.pasteleria.controller.api;

import com.x7.pasteleria.controller.SesionUsuario;
import com.x7.pasteleria.model.Rol;
import com.x7.pasteleria.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Arma las respuestas JSON de error de la API con un formato único:
 * { "timestamp", "status", "error", "mensaje", "errores": { "campo": "mensaje" } }
 */
@Component
public class ApiRespuestas {

    /** Si un campo tiene varios errores, se informa primero el de "obligatorio". */
    private static final Set<String> PRIORITARIOS = Set.of("NotBlank", "NotNull");

    private final MessageSource messageSource;

    public ApiRespuestas(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public ResponseEntity<Map<String, Object>> error(HttpStatus status, String mensaje) {
        return ResponseEntity.status(status).body(cuerpo(status, mensaje));
    }

    /** 400 Bad Request con el mensaje de cada campo inválido. */
    public ResponseEntity<Map<String, Object>> errorValidacion(BindingResult result) {
        Map<String, String> errores = new LinkedHashMap<>();
        result.getFieldErrors().stream()
                .sorted(Comparator.comparing((FieldError e) -> PRIORITARIOS.contains(e.getCode()) ? 0 : 1))
                .forEach(e -> errores.putIfAbsent(e.getField(), mensaje(e)));
        for (ObjectError global : result.getGlobalErrors()) {
            errores.putIfAbsent(global.getObjectName(), mensaje(global));
        }

        Map<String, Object> cuerpo = cuerpo(HttpStatus.BAD_REQUEST, "Los datos enviados no son válidos.");
        cuerpo.put("errores", errores);
        return ResponseEntity.badRequest().body(cuerpo);
    }

    /**
     * Comprueba la sesión antes de una operación de la API.
     * @return null si el usuario puede continuar; si no, la respuesta 401 o 403.
     */
    public ResponseEntity<Map<String, Object>> verificarRol(HttpSession session, Rol rolRequerido) {
        Usuario usuario = SesionUsuario.actual(session);
        if (usuario == null) {
            return error(HttpStatus.UNAUTHORIZED, "Debes iniciar sesión (POST /api/auth/login).");
        }
        if (rolRequerido != null && usuario.getRol() != rolRequerido) {
            return error(HttpStatus.FORBIDDEN, "Tu rol no tiene permiso para realizar esta operación.");
        }
        return null;
    }

    private String mensaje(ObjectError error) {
        return messageSource.getMessage(error, LocaleContextHolder.getLocale());
    }

    private Map<String, Object> cuerpo(HttpStatus status, String mensaje) {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now().withNano(0).toString());
        cuerpo.put("status", status.value());
        cuerpo.put("error", status.getReasonPhrase());
        cuerpo.put("mensaje", mensaje);
        return cuerpo;
    }
}
