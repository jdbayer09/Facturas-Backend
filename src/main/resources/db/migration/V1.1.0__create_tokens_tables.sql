-- Crear tabla para refresh tokens
CREATE TABLE security.refresh_tokens (
    token VARCHAR(500) NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    email VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    used BOOLEAN DEFAULT FALSE NOT NULL,
    last_used_at TIMESTAMP,
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES security.users(id) ON DELETE CASCADE
);

-- Índices para mejorar búsquedas
CREATE INDEX idx_refresh_tokens_user_id ON security.refresh_tokens(user_id);
CREATE INDEX idx_refresh_tokens_email ON security.refresh_tokens(email);
CREATE INDEX idx_refresh_tokens_expires_at ON security.refresh_tokens(expires_at);

-- Crear tabla para tokens en blacklist
CREATE TABLE security.blacklisted_tokens (
    token VARCHAR(500) NOT NULL PRIMARY KEY,
    user_id UUID NOT NULL,
    email VARCHAR(100) NOT NULL,
    blacklisted_at TIMESTAMP NOT NULL DEFAULT NOW(),
    reason VARCHAR(100) NOT NULL,
    ip_address VARCHAR(45),
    expires_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_blacklisted_token_user FOREIGN KEY (user_id) REFERENCES security.users(id) ON DELETE CASCADE
);

-- Índices para mejorar búsquedas
CREATE INDEX idx_blacklisted_tokens_user_id ON security.blacklisted_tokens(user_id);
CREATE INDEX idx_blacklisted_tokens_expires_at ON security.blacklisted_tokens(expires_at);

-- Comentarios para documentación
COMMENT ON TABLE security.refresh_tokens IS 'Tabla para almacenar refresh tokens JWT';
COMMENT ON TABLE security.blacklisted_tokens IS 'Tabla para tokens JWT invalidados (logout)';

COMMENT ON COLUMN security.refresh_tokens.token IS 'Token JWT de refresh';
COMMENT ON COLUMN security.refresh_tokens.user_id IS 'ID del usuario propietario del token';
COMMENT ON COLUMN security.refresh_tokens.used IS 'Indica si el token ya fue usado (para prevenir reutilización)';

COMMENT ON COLUMN security.blacklisted_tokens.token IS 'Token JWT invalidado';
COMMENT ON COLUMN security.blacklisted_tokens.reason IS 'Razón de invalidación: logout, password_change, security_breach, etc.';