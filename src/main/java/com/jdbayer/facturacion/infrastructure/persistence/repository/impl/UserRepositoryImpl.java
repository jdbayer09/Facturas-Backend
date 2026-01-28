package com.jdbayer.facturacion.infrastructure.persistence.repository.impl;

import com.jdbayer.facturacion.domain.model.User;
import com.jdbayer.facturacion.domain.model.valueobject.Email;
import com.jdbayer.facturacion.domain.repository.UserRepository;
import com.jdbayer.facturacion.infrastructure.persistence.mapper.UserMapper;
import com.jdbayer.facturacion.infrastructure.persistence.repository.R2dbcUserRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implementación del puerto UserRepository del dominio.
 *
 * Esta clase es el adaptador que conecta:
 * - Puerto: UserRepository (interfaz del dominio)
 * - Implementación: R2dbcUserRepository (Spring Data)
 *
 * Responsabilidades:
 * - Convertir entre objetos de dominio y entidades de BD
 * - Delegar operaciones a Spring Data R2DBC
 * - Manejar la conversión de Value Objects
 */
@Repository
public class UserRepositoryImpl implements UserRepository {

    private final R2dbcUserRepository r2dbcRepository;
    private final UserMapper mapper;

    public UserRepositoryImpl(R2dbcUserRepository r2dbcRepository, UserMapper mapper) {
        this.r2dbcRepository = r2dbcRepository;
        this.mapper = mapper;
    }

    /**
     * Guarda un usuario en la base de datos.
     *
     * Si el usuario no existe (INSERT), crea uno nuevo.
     * Si el usuario existe (UPDATE), lo actualiza.
     *
     * @param user Usuario del dominio
     * @return Mono<User> con el usuario guardado
     */
    @Override
    public Mono<User> save(User user) {
        return Mono.just(user)
                .map(mapper::toEntity)           // Domain → Entity
                .flatMap(r2dbcRepository::save)  // Guardar en BD
                .map(mapper::toDomain);          // Entity → Domain
    }

    /**
     * Busca un usuario por su ID.
     *
     * @param id UUID del usuario
     * @return Mono<User> con el usuario encontrado o vacío
     */
    @Override
    public Mono<User> findById(UUID id) {
        return r2dbcRepository.findById(id)
                .map(mapper::toDomain);
    }

    /**
     * Busca un usuario por su email.
     *
     * @param email Email del usuario (Value Object)
     * @return Mono<User> con el usuario encontrado o vacío
     */
    @Override
    public Mono<User> findByEmail(Email email) {
        return r2dbcRepository.findByEmail(email.value())
                .map(mapper::toDomain);
    }

    /**
     * Verifica si existe un usuario con el email dado.
     *
     * @param email Email a verificar (Value Object)
     * @return Mono<Boolean> true si existe, false si no
     */
    @Override
    public Mono<Boolean> existsByEmail(Email email) {
        return r2dbcRepository.existsByEmail(email.value());
    }

    /**
     * Elimina un usuario por su ID.
     *
     * @param id UUID del usuario
     * @return Mono<Void> que completa cuando se elimina
     */
    @Override
    public Mono<Void> deleteById(UUID id) {
        return r2dbcRepository.deleteById(id);
    }
}