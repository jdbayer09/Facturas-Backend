package com.jdbayer.facturacion.application.usecase.impl;

import com.jdbayer.facturacion.application.usecase.LogoutUseCase;
import com.jdbayer.facturacion.domain.exception.UserNotFoundException;
import com.jdbayer.facturacion.domain.repository.UserRepository;
import com.jdbayer.facturacion.infrastructure.security.service.TokenManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implementación del caso de uso de logout.
 *
 * Invalida el token actual agregándolo a la blacklist
 * para que no pueda ser usado hasta que expire naturalmente.
 */
@Service
@Slf4j
public class LogoutUseCaseImpl implements LogoutUseCase {

    private final TokenManagementService tokenManagementService;
    private final UserRepository userRepository;

    public LogoutUseCaseImpl(
            TokenManagementService tokenManagementService,
            UserRepository userRepository
    ) {
        this.tokenManagementService = tokenManagementService;
        this.userRepository = userRepository;
    }

    @Override
    public Mono<Void> execute(String token, UUID userId, String ipAddress) {
        log.debug("Procesando logout para usuario: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
                .flatMap(user -> {
                    // Agregar token a blacklist
                    return tokenManagementService.blacklistToken(
                            token,
                            user.getId(),
                            user.getEmail().value(),
                            "logout",
                            ipAddress
                    );
                })
                .doOnSuccess(v -> log.info("Logout exitoso para usuario: {}", userId))
                .doOnError(error -> log.error("Error en logout: {}", error.getMessage()));
    }
}