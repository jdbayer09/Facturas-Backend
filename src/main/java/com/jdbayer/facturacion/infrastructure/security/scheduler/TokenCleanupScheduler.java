package com.jdbayer.facturacion.infrastructure.security.scheduler;

import com.jdbayer.facturacion.infrastructure.security.service.TokenManagementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler para tareas periódicas relacionadas con tokens.
 *
 * Ejecuta limpieza automática de tokens expirados en PostgreSQL.
 */
@Component
@Slf4j
public class TokenCleanupScheduler {

    private final TokenManagementService tokenManagementService;

    public TokenCleanupScheduler(TokenManagementService tokenManagementService) {
        this.tokenManagementService = tokenManagementService;
    }

    /**
     * Limpia refresh tokens expirados cada 6 horas.
     *
     * Esto previene que la tabla crezca indefinidamente con tokens antiguos.
     */
    @Scheduled(cron = "0 0 */6 * * *") // Cada 6 horas
    public void cleanupExpiredRefreshTokens() {
        log.info("Iniciando limpieza programada de refresh tokens expirados");

        tokenManagementService.cleanupExpiredRefreshTokens()
                .doOnSuccess(count ->
                        log.info("Limpieza completada: {} refresh tokens eliminados", count)
                )
                .doOnError(error ->
                        log.error("Error en limpieza de refresh tokens: {}", error.getMessage())
                )
                .subscribe();
    }

    /**
     * Limpia tokens expirados de la blacklist cada 12 horas.
     *
     * Los tokens blacklisted solo necesitan estar en la lista hasta que expiren.
     * Después de eso, no tiene sentido mantenerlos en la BD.
     */
    @Scheduled(cron = "0 0 */12 * * *") // Cada 12 horas
    public void cleanupExpiredBlacklistedTokens() {
        log.info("Iniciando limpieza programada de tokens blacklisted expirados");

        tokenManagementService.cleanupExpiredBlacklistedTokens()
                .doOnSuccess(count ->
                        log.info("Limpieza completada: {} tokens blacklisted eliminados", count)
                )
                .doOnError(error ->
                        log.error("Error en limpieza de tokens blacklisted: {}", error.getMessage())
                )
                .subscribe();
    }

    /**
     * Estadísticas de tokens cada día a medianoche.
     *
     * Útil para monitoreo y debugging.
     */
    @Scheduled(cron = "0 0 0 * * *") // Cada día a medianoche
    public void logTokenStatistics() {
        log.info("Generando estadísticas de tokens...");

        // Aquí puedes agregar lógica para generar estadísticas
        // Por ejemplo: cantidad de tokens activos por usuario, etc.
    }
}