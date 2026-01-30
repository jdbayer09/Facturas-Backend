package com.jdbayer.facturacion.application.usecase.impl;

import com.jdbayer.facturacion.application.dto.request.LoginRequest;
import com.jdbayer.facturacion.application.dto.response.AuthResponse;
import com.jdbayer.facturacion.application.mapper.UserDomainMapper;
import com.jdbayer.facturacion.application.usecase.LoginUseCase;
import com.jdbayer.facturacion.domain.model.valueobject.Email;
import com.jdbayer.facturacion.domain.service.UserDomainService;
import com.jdbayer.facturacion.infrastructure.security.jwt.JwtService;
import com.jdbayer.facturacion.infrastructure.security.service.TokenManagementService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementación del caso de uso de login.
 *
 * Orquesta:
 * 1. Autenticación del usuario (UserDomainService)
 * 2. Generación del token JWT (JwtService)
 * 3. Generación del refresh token (TokenManagementService)
 * 4. Mapeo a DTO de respuesta
 */
@Service
public class LoginUseCaseImpl implements LoginUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoginUseCaseImpl.class);

    private final UserDomainService userDomainService;
    private final UserDomainMapper userMapper;
    private final JwtService jwtService;
    private final TokenManagementService tokenManagementService;

    public LoginUseCaseImpl(
            UserDomainService userDomainService,
            UserDomainMapper userMapper,
            JwtService jwtService,
            TokenManagementService tokenManagementService
    ) {
        this.userDomainService = userDomainService;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
        this.tokenManagementService = tokenManagementService;
    }

    @Override
    public Mono<AuthResponse> execute(LoginRequest request) {
        log.debug("Intentando autenticar usuario: {}", request.email());

        return Mono.fromCallable(() -> new Email(request.email()))
                .flatMap(email -> userDomainService.authenticateUser(email, request.password()))
                .flatMap(user -> {
                    // Generar access token JWT real
                    String token = jwtService.generateToken(user);

                    // Generar refresh token
                    // TODO: Obtener IP y User-Agent del request
                    return tokenManagementService.createRefreshToken(
                            user,
                            "unknown", // IP address - se obtendrá del controller
                            "unknown"  // User agent - se obtendrá del controller
                    ).flatMap(refreshToken -> {
                        var userResponse = userMapper.toResponse(user);
                        var authResponse = new AuthResponse(token, refreshToken, userResponse);
                        return Mono.just(authResponse);
                    });
                })
                .doOnSuccess(response -> log.info("Usuario autenticado exitosamente: {}", response.user().email()))
                .doOnError(error -> log.error("Error al autenticar usuario: {}", error.getMessage()));
    }
}