package com.jdbayer.facturacion.application.usecase.impl;

import com.jdbayer.facturacion.application.dto.response.UserResponse;
import com.jdbayer.facturacion.application.mapper.UserDomainMapper;
import com.jdbayer.facturacion.application.usecase.GetUserByIdUseCase;
import com.jdbayer.facturacion.domain.exception.UserNotFoundException;
import com.jdbayer.facturacion.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implementación del caso de uso para obtener un usuario por ID.
 */
@Service
public class GetUserByIdUseCaseImpl implements GetUserByIdUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetUserByIdUseCaseImpl.class);

    private final UserRepository userRepository;
    private final UserDomainMapper userMapper;

    public GetUserByIdUseCaseImpl(UserRepository userRepository, UserDomainMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public Mono<UserResponse> execute(UUID userId) {
        log.debug("Buscando usuario con ID: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
                .map(userMapper::toResponse)
                .doOnSuccess(response -> log.debug("Usuario encontrado: {}", response.email()))
                .doOnError(error -> log.error("Error al buscar usuario: {}", error.getMessage()));
    }
}