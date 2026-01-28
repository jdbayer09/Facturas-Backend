package com.jdbayer.facturacion.application.usecase.impl;

import com.jdbayer.facturacion.application.dto.request.LoginRequest;
import com.jdbayer.facturacion.application.dto.response.AuthResponse;
import com.jdbayer.facturacion.application.mapper.UserMapper;
import com.jdbayer.facturacion.application.usecase.LoginUseCase;
import com.jdbayer.facturacion.domain.model.valueobject.Email;
import com.jdbayer.facturacion.domain.service.UserDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementación del caso de uso de login.
 *
 * Orquesta:
 * 1. Autenticación del usuario (UserDomainService)
 * 2. Generación del token JWT (TODO: JwtService)
 * 3. Mapeo a DTO de respuesta
 */
@Service
public class LoginUseCaseImpl implements LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUseCaseImpl.class);

    private final UserDomainService userDomainService;
    private final UserMapper userMapper;
    // TODO: Inyectar JwtService cuando lo implementemos
    // private final JwtService jwtService;

    public LoginUseCaseImpl(
            UserDomainService userDomainService,
            UserMapper userMapper
    ) {
        this.userDomainService = userDomainService;
        this.userMapper = userMapper;
    }

    @Override
    public Mono<AuthResponse> execute(LoginRequest request) {
        log.debug("Intentando autenticar usuario: {}", request.email());

        return Mono.fromCallable(() -> new Email(request.email()))
                .flatMap(email -> userDomainService.authenticateUser(email, request.password()))
                .flatMap(user -> {
                    // TODO: Generar token JWT
                    // String token = jwtService.generateToken(user);
                    String token = "TEMPORAL_TOKEN_" + user.getId(); // Token temporal

                    var userResponse = userMapper.toResponse(user);
                    return Mono.just(new AuthResponse(token, userResponse));
                })
                .doOnSuccess(response -> log.info("Usuario autenticado exitosamente: {}", response.user().email()))
                .doOnError(error -> log.error("Error al autenticar usuario: {}", error.getMessage()));
    }
}