package com.jdbayer.facturacion.application.usecase.impl;

import com.jdbayer.facturacion.application.usecase.DeactivateUserUseCase;
import com.jdbayer.facturacion.domain.exception.UserNotFoundException;
import com.jdbayer.facturacion.domain.repository.UserRepository;
import com.jdbayer.facturacion.domain.service.UserDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implementación del caso de uso para desactivar un usuario.
 *
 * La desactivación es un borrado lógico: el usuario permanece en la BD
 * pero no puede autenticarse ni realizar operaciones.
 */
@Service
public class DeactivateUserUseCaseImpl implements DeactivateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(DeactivateUserUseCaseImpl.class);

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;

    public DeactivateUserUseCaseImpl(
            UserRepository userRepository,
            UserDomainService userDomainService
    ) {
        this.userRepository = userRepository;
        this.userDomainService = userDomainService;
    }

    @Override
    public Mono<Void> execute(UUID userId) {
        log.debug("Desactivando usuario con ID: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
                .flatMap(user ->
                        userDomainService.canDeactivate(user)
                                .then(Mono.defer(() -> {
                                    user.deactivate();
                                    return userRepository.save(user);
                                }))
                )
                .then()
                .doOnSuccess(v -> log.info("Usuario desactivado exitosamente: {}", userId))
                .doOnError(error -> log.error("Error al desactivar usuario: {}", error.getMessage()));
    }
}