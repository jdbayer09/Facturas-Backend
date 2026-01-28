package com.jdbayer.facturacion.infrastructure.security.jwt;

import com.jdbayer.facturacion.domain.model.User;
import io.jsonwebtoken.Claims;

import java.util.UUID;

/**
 * Servicio para manejo de tokens JWT (JSON Web Tokens).
 *
 * JWT es un estándar abierto (RFC 7519) para transmitir información
 * de forma segura entre partes como un objeto JSON.
 *
 * Estructura de un JWT:
 * - Header: Algoritmo y tipo de token
 * - Payload: Claims (datos del usuario)
 * - Signature: Firma digital
 *
 * Ejemplo de token:
 * eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
 */
public interface JwtService {

    /**
     * Genera un token JWT de acceso para un usuario.
     *
     * El token contiene:
     * - sub (subject): ID del usuario
     * - email: Email del usuario
     * - iat (issued at): Fecha de emisión
     * - exp (expiration): Fecha de expiración
     *
     * @param user Usuario para el cual generar el token
     * @return Token JWT firmado
     */
    String generateToken(User user);

    /**
     * Genera un refresh token para renovar el token de acceso.
     *
     * El refresh token tiene una expiración más larga que el token de acceso
     * y se usa para obtener un nuevo token sin volver a autenticarse.
     *
     * @param user Usuario para el cual generar el refresh token
     * @return Refresh token JWT firmado
     */
    String generateRefreshToken(User user);

    /**
     * Valida si un token JWT es válido.
     *
     * Verifica:
     * - Firma correcta
     * - No expirado
     * - Formato válido
     *
     * @param token Token JWT a validar
     * @return true si el token es válido, false en caso contrario
     */
    boolean validateToken(String token);

    /**
     * Extrae el ID del usuario del token JWT.
     *
     * @param token Token JWT
     * @return UUID del usuario
     * @throws io.jsonwebtoken.JwtException si el token es inválido
     */
    UUID extractUserId(String token);

    /**
     * Extrae el email del usuario del token JWT.
     *
     * @param token Token JWT
     * @return Email del usuario
     * @throws io.jsonwebtoken.JwtException si el token es inválido
     */
    String extractEmail(String token);

    /**
     * Extrae todos los claims (datos) del token JWT.
     *
     * @param token Token JWT
     * @return Claims del token
     * @throws io.jsonwebtoken.JwtException si el token es inválido
     */
    Claims extractAllClaims(String token);

    /**
     * Verifica si un token ha expirado.
     *
     * @param token Token JWT
     * @return true si el token ha expirado, false en caso contrario
     */
    boolean isTokenExpired(String token);
}