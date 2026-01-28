package com.jdbayer.facturacion.application.usecase.impl;

import com.jdbayer.facturacion.application.usecase.ActivateUserUseCase;
import com.jdbayer.facturacion.domain.exception.UserNotFoundException;
import com.jdbayer.facturacion.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implementación del caso de uso para activar un usuario.
 *
 * Permite reactivar usuarios que fueron desactivados previamente.
 */
@Service
public class ActivateUserUseCaseImpl implements ActivateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(ActivateUserUseCaseImpl.class);

    private final UserRepository userRepository;

    public ActivateUserUseCaseImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<Void> execute(UUID userId) {
        log.debug("Activando usuario con ID: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
                .flatMap(user -> {
                    user.activate();
                    return userRepository.save(user);
                })
                .then()
                .doOnSuccess(v -> log.info("Usuario activado exitosamente: {}", userId))
                .doOnError(error -> log.error("Error al activar usuario: {}", error.getMessage()));
    }
}