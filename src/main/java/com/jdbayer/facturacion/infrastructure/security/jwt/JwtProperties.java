package com.jdbayer.facturacion.infrastructure.security.jwt;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class JwtProperties {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.header-prefix}")
    private String headerPrefix;

    @Value("${jwt.header-name}")
    private String headerName;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    @Value("${jwt.expiration}")
    private long expiration;

}
