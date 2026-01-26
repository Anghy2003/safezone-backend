package com.ista.springboot.web.app.models.services;

import java.time.Duration;
import java.time.OffsetDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ista.springboot.web.app.exceptions.RateLimitBlockedException;
import com.ista.springboot.web.app.models.entity.UsuarioRateLimit;
import com.ista.springboot.web.app.repository.UsuarioRateLimitRepository;

@Service
public class RateLimitService {

    private final UsuarioRateLimitRepository repo;

    // Configurable por env vars en Cloud Run
    @Value("${app.ratelimit.window-seconds:600}")     // 10 min
    private long windowSeconds;

    @Value("${app.ratelimit.max-requests:20}")        // 20 requests/ventana
    private int maxRequests;

    @Value("${app.ratelimit.block-days:15}")          // bloqueo 15 días (nivel máximo)
    private int blockDays;

    // Ajustable (sin cambiar entity): reduce 1 nivel si pasa X tiempo sin “abuso”
    @Value("${app.ratelimit.decay-seconds:86400}")    // 24h
    private long decaySeconds;

    // Cap de niveles (para llegar al máximo: 15 días)
    @Value("${app.ratelimit.max-level:6}")
    private int maxLevel;

    public RateLimitService(UsuarioRateLimitRepository repo) {
        this.repo = repo;
    }

    /**
     * Estrategia "como apps":
     * - Ventana deslizante fija por usuario (windowSeconds / maxRequests)
     * - Penalización escalonada (cooldown) cuando excede el límite
     * - Escala el castigo si reincide (level)
     * - Sin cambiar la entity: guardamos el level en count NEGATIVO
     *
     * Convención en UsuarioRateLimit.count:
     * - count >= 0  => contador de requests en la ventana actual
     * - count < 0   => nivel de reincidencia (strikes), ej: -2 = level 2
     */
    @Transactional
    public void checkAndConsumeOrThrow(Long usuarioId) {
        if (usuarioId == null) return;

        final OffsetDateTime now = OffsetDateTime.now();

        UsuarioRateLimit rl = repo.findByUsuarioIdForUpdate(usuarioId)
                .orElseGet(() -> {
                    UsuarioRateLimit x = new UsuarioRateLimit();
                    x.setUsuarioId(usuarioId);
                    x.setWindowStart(now);
                    x.setCount(0);
                    x.setBlockedUntil(null);
                    x.setUpdatedAt(now);
                    return x;
                });

        // 1) Si está bloqueado (cooldown activo)
        if (rl.getBlockedUntil() != null && rl.getBlockedUntil().isAfter(now)) {
            throw new RateLimitBlockedException("Demasiados intentos. Intenta más tarde.", rl.getBlockedUntil());
        }

        // 2) Leer "level" si count es negativo (strikes)
        int raw = (rl.getCount() == null) ? 0 : rl.getCount();
        int level = (raw < 0) ? Math.abs(raw) : 0;

        // 3) DECAY del level si pasó suficiente tiempo desde el último evento guardado (updatedAt)
        // Nota: como no añadimos columnas, usamos updatedAt como referencia temporal.
        if (level > 0 && rl.getUpdatedAt() != null) {
            long since = Duration.between(rl.getUpdatedAt(), now).getSeconds();
            if (since >= decaySeconds) {
                level = Math.max(0, level - 1);
            }
        }

        // 4) Reset de ventana si expiró (usa el windowSeconds configurado)
        OffsetDateTime wStart = (rl.getWindowStart() != null) ? rl.getWindowStart() : now;
        long elapsed = Duration.between(wStart, now).getSeconds();
        if (elapsed >= windowSeconds) {
            rl.setWindowStart(now);
            raw = 0; // reinicia el contador de ventana (no toca "level" que está en variable)
        }

        // 5) Contar request en ventana (si raw era negativo, empezamos desde 0)
        int windowCount = (raw >= 0) ? raw : 0;
        windowCount++;

        // 6) Si excede el máximo, aplicar penalización escalonada y subir nivel
        if (windowCount > maxRequests) {

            level = Math.min(level + 1, maxLevel);

            long blockSeconds;
            switch (level) {
                case 1:  blockSeconds = 30; break;                 // 30s
                case 2:  blockSeconds = 120; break;                // 2m
                case 3:  blockSeconds = 600; break;                // 10m
                case 4:  blockSeconds = 3600; break;               // 1h
                case 5:  blockSeconds = 43200; break;              // 12h
                default: blockSeconds = (long) blockDays * 24 * 3600; // 15 días (o lo que configures)
            }

            OffsetDateTime until = now.plusSeconds(blockSeconds);
            rl.setBlockedUntil(until);

            // Guardar el level en count NEGATIVO (sin cambiar entity)
            rl.setCount(-level);

            // Reiniciar ventana
            rl.setWindowStart(now);

            // Marcar evento
            rl.setUpdatedAt(now);

            repo.save(rl);
            throw new RateLimitBlockedException("Límite excedido. Penalización aplicada.", until);
        }

        // 7) Si no excedió: persistir
        rl.setUpdatedAt(now);

        // Si hay level activo, mantenlo; si no, guarda el contador de ventana
        if (level > 0) {
            rl.setCount(-level);
        } else {
            rl.setCount(windowCount);
        }

        repo.save(rl);
    }
}
