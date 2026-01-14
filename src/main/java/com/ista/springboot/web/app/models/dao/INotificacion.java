package com.ista.springboot.web.app.models.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ista.springboot.web.app.models.entity.Notificacion;

public interface INotificacion extends JpaRepository<Notificacion, Long> {

    @Query("""
        SELECT n.comunidad.id, COUNT(n)
        FROM Notificacion n
        WHERE n.usuario.id = :userId
          AND n.comunidad.id IS NOT NULL
          AND n.leido = false
        GROUP BY n.comunidad.id
    """)
    List<Object[]> countUnreadByComunidad(@Param("userId") Long userId);

    @Modifying
    @Query("""
        UPDATE Notificacion n
        SET n.leido = true,
            n.fechaLectura = CURRENT_TIMESTAMP
        WHERE n.usuario.id = :userId
          AND n.comunidad.id = :comunidadId
          AND n.leido = false
    """)
    int markReadByComunidad(@Param("userId") Long userId, @Param("comunidadId") Long comunidadId);
}
