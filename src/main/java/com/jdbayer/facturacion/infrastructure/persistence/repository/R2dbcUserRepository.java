package com.jdbayer.facturacion.infrastructure.persistence.repository;

import com.jdbayer.facturacion.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface  R2dbcUserRepository extends R2dbcRepository<UserEntity, UUID> {
    /**
     * Busca un usuario por email.
     *
     * @param email Email del usuario
     * @return Mono<UserEntity> con el usuario encontrado o vacío
     */
    @Query("SELECT * FROM security.users WHERE email = :email")
    Mono<UserEntity> findByEmail(String email);

    /**
     * Verifica si existe un usuario con el email dado.
     *
     * @param email Email a verificar
     * @return Mono<Boolean> true si existe, false si no
     */
    @Query("SELECT EXISTS(SELECT 1 FROM security.users WHERE email = :email)")
    Mono<Boolean> existsByEmail(String email);

    /**
     * Busca un usuario activo por email.
     * Útil para login y validaciones.
     *
     * @param email Email del usuario
     * @return Mono<UserEntity> con el usuario activo o vacío
     */
    @Query("SELECT * FROM security.users WHERE email = :email AND is_active = true")
    Mono<UserEntity> findActiveByEmail(String email);

    /**
     * Cuenta usuarios activos.
     * Útil para estadísticas o validaciones de negocio.
     *
     * @return Mono<Long> cantidad de usuarios activos
     */
    @Query("SELECT COUNT(*) FROM security.users WHERE is_active = true")
    Mono<Long> countActiveUsers();
}
