package com.ista.springboot.web.app.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.ista.springboot.web.app.models.entity.UbicacionUsuario;

public interface IUbicacionUsuario extends CrudRepository<UbicacionUsuario, Long> {

    UbicacionUsuario findByUsuarioId(Long usuarioId);

    @Query(value = """
        SELECT
            u.id AS id,
            trim(concat(u.nombre, ' ', coalesce(u.apellido, ''))) AS name,
            ST_Y(CAST(uu.ubicacion AS geometry)) AS lat,
            ST_X(CAST(uu.ubicacion AS geometry)) AS lng,
            u.foto_url AS avatarUrl
        FROM public.ubicacion_usuario uu
        JOIN public.usuario u 
            ON u.id = uu.usuario_id
        WHERE u.activo = true
          AND uu.ubicacion IS NOT NULL
          AND uu.ultima_actualizacion >=
              NOW() - make_interval(mins => CAST(:lastMinutes AS int))
          AND ST_DWithin(
              CAST(uu.ubicacion AS geography),
              CAST(ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) AS geography),
              :radio
          )
        ORDER BY
          ST_Distance(
              CAST(uu.ubicacion AS geography),
              CAST(ST_SetSRID(ST_MakePoint(:lng, :lat), 4326) AS geography)
          ) ASC
        LIMIT :limit
        """,
        nativeQuery = true)
    List<Object[]> findNearbyNative(
            @Param("lat") double lat,
            @Param("lng") double lng,
            @Param("radio") double radio,
            @Param("lastMinutes") int lastMinutes,
            @Param("limit") int limit
    );
}
