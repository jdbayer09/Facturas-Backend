package com.jdbayer.facturacion.application.usecase.impl;

import com.jdbayer.facturacion.application.dto.request.RegisterUserRequest;
import com.jdbayer.facturacion.application.dto.response.UserResponse;
import com.jdbayer.facturacion.application.mapper.UserMapper;
import com.jdbayer.facturacion.application.usecase.RegisterUserUseCase;
import com.jdbayer.facturacion.domain.model.User;
import com.jdbayer.facturacion.domain.model.valueobject.Email;
import com.jdbayer.facturacion.domain.model.valueobject.Name;
import com.jdbayer.facturacion.domain.repository.UserRepository;
import com.jdbayer.facturacion.domain.service.UserDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Implementación del caso de uso de registro de usuario.
 *
 * Orquesta:
 * 1. Validación de email único (UserDomainService)
 * 2. Creación del hash de contraseña (UserDomainService)
 * 3. Creación de la entidad User
 * 4. Persistencia (UserRepository)
 * 5. Mapeo a DTO de respuesta
 */
@Service
public class RegisterUserUseCaseImpl implements RegisterUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegisterUserUseCaseImpl.class);

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final UserMapper userMapper;

    public RegisterUserUseCaseImpl(
            UserRepository userRepository,
            UserDomainService userDomainService,
            UserMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.userDomainService = userDomainService;
        this.userMapper = userMapper;
    }

    @Override
    public Mono<UserResponse> execute(RegisterUserRequest request) {
        log.debug("Registrando usuario con email: {}", request.email());

        return Mono.fromCallable(() -> {
                    // Crear value objects del dominio
                    Email email = new Email(request.email());
                    Name name = new Name(request.name());
                    Name lastName = new Name(request.lastName());
                    return new Object[]{email, name, lastName};
                })
                .flatMap(objects -> {
                    Email email = (Email) objects[0];
                    Name name = (Name) objects[1];
                    Name lastName = (Name) objects[2];

                    // Validar que el email sea único
                    return userDomainService.ensureEmailIsUnique(email)
                            .then(Mono.defer(() -> {
                                // Crear hash de contraseña
                                var passwordHash = userDomainService.createPasswordHash(request.password());

                                // Crear entidad User
                                var user = User.create(name, lastName, email, passwordHash);

                                // Persistir
                                return userRepository.save(user);
                            }));
                })
                .map(userMapper::toResponse)
                .doOnSuccess(response -> log.info("Usuario registrado exitosamente con ID: {}", response.id()))
                .doOnError(error -> log.error("Error al registrar usuario: {}", error.getMessage()));
    }
}