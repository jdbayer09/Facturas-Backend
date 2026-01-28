package com.jdbayer.facturacion.infrastructure.security.jwt.impl;

import com.jdbayer.facturacion.domain.model.User;
import com.jdbayer.facturacion.infrastructure.security.jwt.JwtProperties;
import com.jdbayer.facturacion.infrastructure.security.jwt.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

/**
 * Implementación del servicio JWT usando jjwt (io.jsonwebtoken).
 *
 * Usa HMAC-SHA256 (HS256) para firmar los tokens.
 * La clave secreta debe tener al menos 256 bits.
 */
@Service
public class JwtServiceImpl implements JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtServiceImpl.class);

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtServiceImpl(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // Generar la clave secreta a partir del string de configuración
        this.secretKey = Keys.hmacShaKeyFor(
                jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8)
        );
    }

    @Override
    public String generateToken(User user) {
        log.debug("Generando token JWT para usuario: {}", user.getEmail().value());

        Map<String, Object> claims = new HashMap<>();
        claims.put("email", user.getEmail().value());
        claims.put("name", user.getName().value());
        claims.put("active", user.isActive());

        return createToken(claims, user.getId().toString(), jwtProperties.getExpiration());
    }

    @Override
    public String generateRefreshToken(User user) {
        log.debug("Generando refresh token para usuario: {}", user.getEmail().value());

        Map<String, Object> claims = new HashMap<>();
        claims.put("type", "refresh");

        return createToken(claims, user.getId().toString(), jwtProperties.getRefreshExpiration());
    }

    /**
     * Crea un token JWT con los claims y expiración especificados.
     */
    private String createToken(Map<String, Object> claims, String subject, long expirationTime) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationTime);

        return Jwts.builder()
                    .claims(claims)
                    .subject(subject)
                    .issuedAt(now)
                    .expiration(expiryDate)
                    .signWith(secretKey)
                    .compact();
    }

    @Override
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);

            log.debug("Token JWT válido");
            return true;
        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.error("Firma JWT inválida: {}", e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.error("Token JWT mal formado: {}", e.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("Token JWT expirado: {}", e.getMessage());
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.error("Token JWT no soportado: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("Claims JWT vacíos: {}", e.getMessage());
        }
        return false;
    }

    @Override
    public UUID extractUserId(String token) {
        String userId = extractClaim(token, Claims::getSubject);
        return UUID.fromString(userId);
    }

    @Override
    public String extractEmail(String token) {
        return extractClaim(token, claims -> claims.get("email", String.class));
    }

    @Override
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = extractClaim(token, Claims::getExpiration);
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Error al verificar expiración del token: {}", e.getMessage());
            return true;
        }
    }

    /**
     * Extrae un claim específico del token usando una función.
     */
    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
}