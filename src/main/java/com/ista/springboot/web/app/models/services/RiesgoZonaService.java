package com.ista.springboot.web.app.models.services;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.ista.springboot.web.app.dto.ZonaRiesgoResponseDTO;

@Service
public class RiesgoZonaService {

    private final JdbcTemplate jdbcTemplate;

    public RiesgoZonaService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ZonaRiesgoResponseDTO evaluar(double lat, double lng, int radioM, int dias) {

        // ✅ límites razonables (evita abusos y valores absurdos)
        int safeDias = Math.min(Math.max(dias, 1), 365);
        int safeRadio = Math.min(Math.max(radioM, 50), 5000);

        final String sql = """
            SELECT i.tipo, COUNT(*) AS cnt
            FROM incidente i
            WHERE i.ubicacion IS NOT NULL
              AND i.fecha_creacion >= NOW() - (? * INTERVAL '1 day')
              AND ST_DWithin(
                    i.ubicacion::geography,
                    ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography,
                    ?
                  )
            GROUP BY i.tipo
            ORDER BY cnt DESC
        """;

        List<ZonaRiesgoResponseDTO.MotivoDTO> motivos = jdbcTemplate.query(
            sql,
            (rs, rowNum) -> new ZonaRiesgoResponseDTO.MotivoDTO(
                rs.getString("tipo"),
                rs.getLong("cnt")
            ),
            safeDias, // ?1
            lng,      // ?2  (x)
            lat,      // ?3  (y)
            safeRadio // ?4
        );

        long total = motivos.stream().mapToLong(ZonaRiesgoResponseDTO.MotivoDTO::getCount).sum();

        String nivel;
        if (total >= 8) nivel = "ALTO";
        else if (total >= 3) nivel = "MEDIO";
        else nivel = "BAJO";

        double score = Math.min(1.0, total / 10.0);

        return new ZonaRiesgoResponseDTO(nivel, score, safeRadio, safeDias, total, motivos);
    }
}
