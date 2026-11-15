package com.x7.pasteleria.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Respuestas JSON de la API cuando Spring Security rechaza una petición:
 *  - 401 Unauthorized: no hay token o el token no es válido.
 *  - 403 Forbidden: el token es válido, pero el rol no tiene permiso.
 * Usa el mismo formato que el resto de errores de la API (timestamp, status, error, mensaje).
 */
public class ErroresApiSeguridad implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    public ErroresApiSeguridad(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        Object motivo = request.getAttribute(JwtAuthenticationFilter.ATRIBUTO_ERROR);
        String mensaje = motivo != null
                ? motivo.toString()
                : "Debes enviar un token en la cabecera Authorization: Bearer <token>. Obténlo con POST /api/auth/login.";
        escribir(response, HttpStatus.UNAUTHORIZED, mensaje);
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        escribir(response, HttpStatus.FORBIDDEN, "Tu rol no tiene permiso para realizar esta operación.");
    }

    private void escribir(HttpServletResponse response, HttpStatus status, String mensaje) throws IOException {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("timestamp", LocalDateTime.now().withNano(0).toString());
        cuerpo.put("status", status.value());
        cuerpo.put("error", status.getReasonPhrase());
        cuerpo.put("mensaje", mensaje);

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        if (status == HttpStatus.UNAUTHORIZED) {
            response.setHeader("WWW-Authenticate", "Bearer");
        }
        objectMapper.writeValue(response.getWriter(), cuerpo);
    }
}
