package com.jdbayer.facturacion.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jdbayer.facturacion.infrastructure.security.token.BlacklistedToken;
import com.jdbayer.facturacion.infrastructure.security.token.RefreshToken;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Configuración de Redis para soporte reactivo.
 *
 * Spring Data Redis no soporta ReactiveCrudRepository,
 * por lo que configuramos ReactiveRedisTemplate directamente.
 *
 * Configuramos templates específicos para cada tipo de entidad
 * con serialización JSON.
 */
@Configuration
public class RedisConfig {

    /**
     * Configura ReactiveRedisTemplate para BlacklistedToken.
     *
     * Usa Jackson2JsonRedisSerializer para convertir objetos a JSON.
     */
    @Bean
    public ReactiveRedisTemplate<String, BlacklistedToken> blacklistedTokenRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<BlacklistedToken> valueSerializer =
                new Jackson2JsonRedisSerializer<>(BlacklistedToken.class);
        valueSerializer.setObjectMapper(objectMapper);

        RedisSerializationContext<String, BlacklistedToken> context =
                RedisSerializationContext.<String, BlacklistedToken>newSerializationContext(
                                new StringRedisSerializer()
                        )
                        .value(valueSerializer)
                        .hashValue(valueSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    /**
     * Configura ReactiveRedisTemplate para RefreshToken.
     *
     * Usa Jackson2JsonRedisSerializer para convertir objetos a JSON.
     */
    @Bean
    public ReactiveRedisTemplate<String, RefreshToken> refreshTokenRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        Jackson2JsonRedisSerializer<RefreshToken> valueSerializer =
                new Jackson2JsonRedisSerializer<>(RefreshToken.class);
        valueSerializer.setObjectMapper(objectMapper);

        RedisSerializationContext<String, RefreshToken> context =
                RedisSerializationContext.<String, RefreshToken>newSerializationContext(
                                new StringRedisSerializer()
                        )
                        .value(valueSerializer)
                        .hashValue(valueSerializer)
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }

    /**
     * Template genérico para otros usos de Redis.
     * Útil para operaciones simples key-value.
     */
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(
            ReactiveRedisConnectionFactory connectionFactory
    ) {
        StringRedisSerializer serializer = new StringRedisSerializer();

        RedisSerializationContext<String, String> context =
                RedisSerializationContext.<String, String>newSerializationContext()
                        .key(serializer)
                        .value(serializer)
                        .hashKey(serializer)
                        .hashValue(serializer)
                        .build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}