package com.jdbayer.facturacion.application.usecase.impl;

import com.jdbayer.facturacion.application.dto.request.RefreshTokenRequest;
import com.jdbayer.facturacion.application.dto.response.AuthResponse;
import com.jdbayer.facturacion.application.mapper.UserDomainMapper;
import com.jdbayer.facturacion.application.usecase.RefreshTokenUseCase;
import com.jdbayer.facturacion.domain.exception.UserNotFoundException;
import com.jdbayer.facturacion.domain.repository.UserRepository;
import com.jdbayer.facturacion.infrastructure.security.jwt.JwtService;
import com.jdbayer.facturacion.infrastructure.security.service.TokenManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementación del caso de uso para renovar tokens.
 *
 * Implementa el patrón de rotación de refresh tokens:
 * 1. Valida el refresh token
 * 2. Genera nuevo access token
 * 3. Genera nuevo refresh token
 * 4. Invalida el refresh token anterior
 *
 * Este patrón mejora la seguridad al prevenir la reutilización de refresh tokens.
 */
@Service
@Slf4j
public class RefreshTokenUseCaseImpl implements RefreshTokenUseCase {

    private final TokenManagementService tokenManagementService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final UserDomainMapper userMapper;

    public RefreshTokenUseCaseImpl(
            TokenManagementService tokenManagementService,
            UserRepository userRepository,
            JwtService jwtService,
            UserDomainMapper userMapper
    ) {
        this.tokenManagementService = tokenManagementService;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    @Override
    public Mono<AuthResponse> execute(
            RefreshTokenRequest request,
            String ipAddress,
            String userAgent
    ) {
        log.debug("Procesando solicitud de refresh token");

        return tokenManagementService.validateAndUseRefreshToken(request.refreshToken())
                .flatMap(refreshToken -> {
                    // Buscar el usuario
                    return userRepository.findById(refreshToken.getUserId())
                            .switchIfEmpty(Mono.error(
                                    new UserNotFoundException(refreshToken.getUserId())
                            ))
                            .flatMap(user -> {
                                // Verificar que el usuario esté activo
                                if (!user.isActive()) {
                                    return Mono.error(new IllegalStateException(
                                            "El usuario está inactivo"
                                    ));
                                }

                                // Generar nuevo access token
                                String newAccessToken = jwtService.generateToken(user);

                                // Generar nuevo refresh token (rotación)
                                return tokenManagementService.createRefreshToken(
                                        user,
                                        ipAddress,
                                        userAgent
                                ).flatMap(newRefreshToken -> {
                                    // Invalidar el refresh token anterior
                                    return tokenManagementService.revokeRefreshToken(
                                            request.refreshToken()
                                    ).then(Mono.defer(() -> {
                                        // Crear respuesta
                                        var userResponse = userMapper.toResponse(user);
                                        var authResponse = new AuthResponse(
                                                newAccessToken,
                                                userResponse
                                        );

                                        log.info("Tokens renovados exitosamente para usuario: {}",
                                                user.getEmail().value());

                                        return Mono.just(authResponse);
                                    }));
                                });
                            });
                })
                .doOnError(error -> log.error("Error al renovar tokens: {}", error.getMessage()));
    }
}