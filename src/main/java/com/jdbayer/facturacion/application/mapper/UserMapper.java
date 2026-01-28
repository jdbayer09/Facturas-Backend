package com.jdbayer.facturacion.application.mapper;

import com.jdbayer.facturacion.application.dto.response.UserResponse;
import com.jdbayer.facturacion.domain.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapper para convertir entre entidades del dominio y DTOs de aplicación.
 * MapStruct genera la implementación automáticamente en tiempo de compilación.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Convierte un User del dominio a UserResponse.
     */
    @Mapping(target = "name", expression = "java(user.getName().value())")
    @Mapping(target = "lastName", expression = "java(user.getLastName().value())")
    @Mapping(target = "email", expression = "java(user.getEmail().value())")
    UserResponse toResponse(User user);
}