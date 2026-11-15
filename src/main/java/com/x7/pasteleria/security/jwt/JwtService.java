package com.x7.pasteleria.security.jwt;

import com.x7.pasteleria.security.UsuarioPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

/**
 * Genera y valida los JSON Web Tokens de la API REST.
 *
 * Un token tiene tres partes separadas por puntos: cabecera.datos.firma
 *  - datos (claims): correo (sub), id, nombre, rol, emisor (iss), fecha de emisión (iat) y de expiración (exp).
 *  - firma: HMAC-SHA con la clave secreta jwt.secreto. Si alguien cambia los datos, la firma deja de coincidir.
 */
@Service
public class JwtService {

    private static final String EMISOR = "pasteleria-x7";

    private final SecretKey clave;
    private final Duration duracion;

    public JwtService(@Value("${jwt.secreto}") String secretoBase64,
                      @Value("${jwt.expiracion-minutos:60}") long minutos) {
        // La clave debe tener al menos 256 bits (32 bytes) para HMAC-SHA256.
        this.clave = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretoBase64));
        this.duracion = Duration.ofMinutes(minutos);
    }

    public String generarToken(UsuarioPrincipal usuario) {
        Instant ahora = Instant.now();
        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("id", usuario.getId())
                .claim("nombre", usuario.getNombre())
                .claim("rol", usuario.getRol().name())
                .issuer(EMISOR)
                .issuedAt(Date.from(ahora))
                .expiration(Date.from(ahora.plus(duracion)))
                .signWith(clave)
                .compact();
    }

    /**
     * Comprueba la firma, el emisor y la fecha de expiración, y devuelve los datos del token.
     * Si algo no cuadra lanza JwtException (ExpiredJwtException si ya venció).
     */
    public Claims validar(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(clave)
                .requireIssuer(EMISOR)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getDuracionSegundos() {
        return duracion.toSeconds();
    }
}
