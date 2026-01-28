package com.jdbayer.facturacion.infrastructure.security;

import com.jdbayer.facturacion.domain.security.PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Implementación de PasswordEncoder usando BCrypt.
 *
 * BCrypt es un algoritmo de hash adaptativo diseñado para contraseñas que:
 * - Incluye salt automáticamente (protege contra rainbow tables)
 * - Es deliberadamente lento (protege contra fuerza bruta)
 * - Tiene un factor de trabajo configurable (se puede ajustar con el tiempo)
 *
 * Esta clase pertenece a la capa de infraestructura porque:
 * - Implementa una interfaz del dominio
 * - Usa una dependencia externa (Spring Security BCrypt)
 * - Es un detalle de implementación que puede cambiar
 */
@Component
public class SecurityConfig implements PasswordEncoder {

    /**
     * BCryptPasswordEncoder de Spring Security.
     * Por defecto usa un strength de 10 (2^10 = 1024 rondas).
     *
     * Valores comunes:
     * - 4-6: Rápido, poco seguro (solo para testing)
     * - 10: Balance recomendado (default)
     * - 12-14: Más seguro pero más lento
     */
    private final BCryptPasswordEncoder encoder;

    public SecurityConfig() {
        // Usando el strength por defecto (10)
        this.encoder = new BCryptPasswordEncoder();
    }

    /**
     * Codifica una contraseña en texto plano usando BCrypt.
     * Cada invocación genera un salt único, por lo que la misma contraseña
     * producirá diferentes hashes.
     *
     * @param rawPassword Contraseña en texto plano
     * @return Hash BCrypt de la contraseña (60 caracteres)
     *
     * Ejemplo:
     * encode("password123") → "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     * encode("password123") → "$2a$10$XYZ..." (diferente hash, mismo password)
     */
    @Override
    public String encode(String rawPassword) {
        if (rawPassword == null) {
            throw new IllegalArgumentException("La contraseña no puede ser nula");
        }
        return encoder.encode(rawPassword);
    }

    /**
     * Verifica si una contraseña en texto plano coincide con un hash BCrypt.
     *
     * @param rawPassword Contraseña en texto plano
     * @param encodedPassword Hash BCrypt almacenado
     * @return true si coinciden, false en caso contrario
     *
     * Ejemplo:
     * String hash = encode("password123");
     * matches("password123", hash) → true
     * matches("wrongpassword", hash) → false
     */
    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return encoder.matches(rawPassword, encodedPassword);
    }
}