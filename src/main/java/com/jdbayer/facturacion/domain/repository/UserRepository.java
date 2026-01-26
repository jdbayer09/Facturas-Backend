package com.jdbayer.facturacion.domain.repository;

import com.jdbayer.facturacion.domain.model.User;
import com.jdbayer.facturacion.domain.model.valueobject.Email;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository {

    Mono<User> save(User user);

    Mono<User> findById(UUID id);

    Mono<User> findByEmail(Email email);

    Mono<Boolean> existsByEmail(Email email);

    Mono<Void> deleteById(UUID id);
}
