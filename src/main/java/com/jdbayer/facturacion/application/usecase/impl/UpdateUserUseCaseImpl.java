package com.jdbayer.facturacion.application.usecase.impl;

import com.jdbayer.facturacion.application.dto.request.UpdateUserRequest;
import com.jdbayer.facturacion.application.dto.response.UserResponse;
import com.jdbayer.facturacion.application.mapper.UserDomainMapper;
import com.jdbayer.facturacion.application.usecase.UpdateUserUseCase;
import com.jdbayer.facturacion.domain.exception.UserNotFoundException;
import com.jdbayer.facturacion.domain.model.User;
import com.jdbayer.facturacion.domain.model.valueobject.Email;
import com.jdbayer.facturacion.domain.model.valueobject.Name;
import com.jdbayer.facturacion.domain.repository.UserRepository;
import com.jdbayer.facturacion.domain.service.UserDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Implementación del caso de uso para actualizar un usuario.
 *
 * Permite actualización parcial: solo los campos no nulos se actualizan.
 */
@Service
public class UpdateUserUseCaseImpl implements UpdateUserUseCase {

    private static final Logger log = LoggerFactory.getLogger(UpdateUserUseCaseImpl.class);

    private final UserRepository userRepository;
    private final UserDomainService userDomainService;
    private final UserDomainMapper userMapper;

    public UpdateUserUseCaseImpl(
            UserRepository userRepository,
            UserDomainService userDomainService,
            UserDomainMapper userMapper
    ) {
        this.userRepository = userRepository;
        this.userDomainService = userDomainService;
        this.userMapper = userMapper;
    }

    @Override
    public Mono<UserResponse> execute(UUID userId, UpdateUserRequest request) {
        log.debug("Actualizando usuario con ID: {}", userId);

        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
                .flatMap(user -> updateUserFields(user, request))
                .flatMap(userRepository::save)
                .map(userMapper::toResponse)
                .doOnSuccess(response -> log.info("Usuario actualizado exitosamente: {}", response.id()))
                .doOnError(error -> log.error("Error al actualizar usuario: {}", error.getMessage()));
    }

    /**
     * Actualiza los campos del usuario que no sean nulos en el request.
     */
    private Mono<User> updateUserFields(User user, UpdateUserRequest request) {
        return Mono.defer(() -> {
            // Actualizar nombre y apellido si se proporcionan
            if (request.name() != null && request.lastName() != null) {
                Name newName = new Name(request.name());
                Name newLastName = new Name(request.lastName());
                user.changeName(newName, newLastName);
            }

            // Actualizar email si se proporciona
            if (request.email() != null) {
                Email newEmail = new Email(request.email());

                // Validar que el nuevo email no esté en uso por otro usuario
                return userDomainService.canChangeEmail(user, newEmail)
                        .then(Mono.defer(() -> {
                            user.changeEmail(newEmail);
                            return Mono.just(user);
                        }));
            }

            return Mono.just(user);
        });
    }
}