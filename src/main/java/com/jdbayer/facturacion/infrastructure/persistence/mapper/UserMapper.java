package com.jdbayer.facturacion.infrastructure.persistence.mapper;

import com.jdbayer.facturacion.domain.model.User;
import com.jdbayer.facturacion.domain.model.valueobject.Email;
import com.jdbayer.facturacion.domain.model.valueobject.Name;
import com.jdbayer.facturacion.domain.security.valueobject.PasswordHash;
import com.jdbayer.facturacion.infrastructure.persistence.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * Mapper para convertir entre User (dominio) y UserEntity (persistencia).
 *
 * MapStruct genera automáticamente la implementación en tiempo de compilación.
 *
 * Responsabilidades:
 * - Convertir Value Objects a tipos primitivos (y viceversa)
 * - Mapear campos con diferentes nombres
 * - Mantener la separación dominio/infraestructura
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Convierte de Entity (BD) a Domain (negocio)
     *
     * @param entity UserEntity de la base de datos
     * @return User del dominio
     */
    @Mapping(target = "name", source = "name", qualifiedByName = "stringToName")
    @Mapping(target = "lastName", source = "lastName", qualifiedByName = "stringToName")
    @Mapping(target = "email", source = "email", qualifiedByName = "stringToEmail")
    @Mapping(target = "passwordHash", source = "password", qualifiedByName = "stringToPasswordHash")
    User toDomain(UserEntity entity);

    /**
     * Convierte de Domain (negocio) a Entity (BD)
     *
     * @param user User del dominio
     * @return UserEntity para la base de datos
     */
    @Mapping(target = "name", source = "name", qualifiedByName = "nameToString")
    @Mapping(target = "lastName", source = "lastName", qualifiedByName = "nameToString")
    @Mapping(target = "email", source = "email", qualifiedByName = "emailToString")
    @Mapping(target = "password", source = "passwordHash", qualifiedByName = "passwordHashToString")
    UserEntity toEntity(User user);

    // ============= MAPPERS DE VALUE OBJECTS =============

    /**
     * Convierte String a Name (Value Object)
     */
    @Named("stringToName")
    default Name stringToName(String value) {
        return value != null ? new Name(value) : null;
    }

    /**
     * Convierte Name (Value Object) a String
     */
    @Named("nameToString")
    default String nameToString(Name name) {
        return name != null ? name.value() : null;
    }

    /**
     * Convierte String a Email (Value Object)
     */
    @Named("stringToEmail")
    default Email stringToEmail(String value) {
        return value != null ? new Email(value) : null;
    }

    /**
     * Convierte Email (Value Object) a String
     */
    @Named("emailToString")
    default String emailToString(Email email) {
        return email != null ? email.value() : null;
    }

    /**
     * Convierte String a PasswordHash (Value Object)
     */
    @Named("stringToPasswordHash")
    default PasswordHash stringToPasswordHash(String value) {
        return value != null ? PasswordHash.fromHash(value) : null;
    }

    /**
     * Convierte PasswordHash (Value Object) a String
     */
    @Named("passwordHashToString")
    default String passwordHashToString(PasswordHash passwordHash) {
        return passwordHash != null ? passwordHash.value() : null;
    }
}