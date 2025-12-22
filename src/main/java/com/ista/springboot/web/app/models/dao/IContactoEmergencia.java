package com.ista.springboot.web.app.models.dao;

import java.util.List;
import org.springframework.data.repository.CrudRepository;
import com.ista.springboot.web.app.models.entity.ContactoEmergencia;

public interface IContactoEmergencia extends CrudRepository<ContactoEmergencia, Long> {

    // ✅ SOLO contactos activos del usuario
    List<ContactoEmergencia> findByUsuario_IdAndActivoTrueOrderByPrioridadAsc(Long usuarioId);
}
