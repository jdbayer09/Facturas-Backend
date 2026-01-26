package com.jdbayer.facturacion.domain.service;

import com.jdbayer.facturacion.domain.exception.DuplicateEmailException;
import com.jdbayer.facturacion.domain.exception.InvalidCredentialsException;
import com.jdbayer.facturacion.domain.exception.UserNotFoundException;
import com.jdbayer.facturacion.domain.model.User;
import com.jdbayer.facturacion.domain.model.valueobject.Email;
import com.jdbayer.facturacion.domain.repository.UserRepository;
import com.jdbayer.facturacion.domain.security.PasswordEncoder;
import com.jdbayer.facturacion.domain.security.valueobject.PasswordHash;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Servicio de dominio para User.
 * Contiene lógica de negocio que:
 * - No pertenece a una entidad específica
 * - Involucra múltiples objetos del dominio
 * - Requiere acceso a repositorios o servicios externos
 */
public class UserDomainService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserDomainService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Valida que el email no esté duplicado.
     * Regla de negocio: No pueden existir dos usuarios con el mismo email.
     *
     * @param email Email a validar
     * @return Mono<Void> que emite error si el email ya existe
     */
    public Mono<Void> ensureEmailIsUnique(Email email) {
        return userRepository.existsByEmail(email)
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new DuplicateEmailException(email.value()));
                    }
                    return Mono.empty();
                });
    }

    /**
     * Valida que el email no esté duplicado, excluyendo al usuario actual.
     * Útil para cuando un usuario quiere cambiar su email a uno nuevo.
     *
     * @param email Email a validar
     * @param currentUserId ID del usuario actual (se excluye de la validación)
     * @return Mono<Void> que emite error si el email ya existe en otro usuario
     */
    public Mono<Void> ensureEmailIsUniqueExcludingUser(Email email, UUID currentUserId) {
        return userRepository.findByEmail(email)
                .flatMap(existingUser -> {
                    if (!existingUser.getId().equals(currentUserId)) {
                        return Mono.error(new DuplicateEmailException(email.value()));
                    }
                    return Mono.empty();
                })
                .then();
    }

    /**
     * Autentica un usuario validando sus credenciales.
     * Reglas de negocio:
     * - El usuario debe existir
     * - El usuario debe estar activo
     * - La contraseña debe coincidir
     *
     * @param email Email del usuario
     * @param rawPassword Contraseña en texto plano
     * @return Mono<User> con el usuario autenticado
     * @throws InvalidCredentialsException si las credenciales son inválidas
     */
    public Mono<User> authenticateUser(Email email, String rawPassword) {
        return userRepository.findByEmail(email)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                .flatMap(user -> {
                    // Validar que el usuario esté activo
                    if (!user.isActive()) {
                        return Mono.error(new InvalidCredentialsException());
                    }

                    // Validar contraseña
                    boolean passwordMatches = passwordEncoder.matches(
                            rawPassword,
                            user.getPasswordHash().value()
                    );

                    if (!passwordMatches) {
                        return Mono.error(new InvalidCredentialsException());
                    }

                    return Mono.just(user);
                });
    }

    /**
     * Crea un hash seguro de contraseña.
     * Encapsula la lógica de dominio de cómo se procesan las contraseñas.
     *
     * @param rawPassword Contraseña en texto plano
     * @return PasswordHash con la contraseña hasheada
     */
    public PasswordHash createPasswordHash(String rawPassword) {
        String encodedPassword = passwordEncoder.encode(rawPassword);
        return PasswordHash.fromHash(encodedPassword);
    }

    /**
     * Verifica si una contraseña en texto plano coincide con el hash almacenado.
     *
     * @param rawPassword Contraseña en texto plano
     * @param passwordHash Hash almacenado
     * @return true si coinciden, false en caso contrario
     */
    public boolean verifyPassword(String rawPassword, PasswordHash passwordHash) {
        return passwordEncoder.matches(rawPassword, passwordHash.value());
    }

    /**
     * Valida que un usuario exista y esté activo.
     * Útil para operaciones que requieren un usuario válido.
     *
     * @param userId ID del usuario
     * @return Mono<User> con el usuario encontrado
     * @throws UserNotFoundException si el usuario no existe o está inactivo
     */
    public Mono<User> ensureUserExistsAndActive(UUID userId) {
        return userRepository.findById(userId)
                .switchIfEmpty(Mono.error(new UserNotFoundException(userId)))
                .flatMap(user -> {
                    if (!user.isActive()) {
                        return Mono.error(new UserNotFoundException(userId));
                    }
                    return Mono.just(user);
                });
    }

    /**
     * Valida si un usuario puede cambiar su email.
     * Regla de negocio: El usuario debe estar activo.
     *
     * @param user Usuario que quiere cambiar el email
     * @param newEmail Nuevo email deseado
     * @return Mono<Void> que emite error si no puede cambiar el email
     */
    public Mono<Void> canChangeEmail(User user, Email newEmail) {
        if (!user.isActive()) {
            return Mono.error(new IllegalStateException("Usuario inactivo no puede cambiar el email"));
        }

        // Validar que el nuevo email no esté en uso por otro usuario
        return ensureEmailIsUniqueExcludingUser(newEmail, user.getId());
    }

    /**
     * Valida si un usuario puede ser desactivado.
     * Aquí puedes agregar reglas de negocio complejas, por ejemplo:
     * - No tiene facturas pendientes
     * - No tiene transacciones en proceso
     * - No es el único administrador
     *
     * @param user Usuario a desactivar
     * @return Mono<Void> que emite error si no puede ser desactivado
     */
    public Mono<Void> canDeactivate(User user) {
        // Por ahora solo validamos que esté activo
        if (!user.isActive()) {
            return Mono.error(new IllegalStateException("El usuario ya está inactivo"));
        }

        // Aquí puedes agregar más validaciones según tu lógica de negocio:
        // - return facturaRepository.countPendingByUser(user.getId())
        //     .flatMap(count -> count > 0
        //         ? Mono.error(new IllegalStateException("Tiene facturas pendientes"))
        //         : Mono.empty());

        return Mono.empty();
    }
}