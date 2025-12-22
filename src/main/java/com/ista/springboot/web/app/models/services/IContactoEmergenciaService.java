package com.ista.springboot.web.app.models.services;

import java.util.List;
import com.ista.springboot.web.app.models.entity.ContactoEmergencia;

public interface IContactoEmergenciaService {
    List<ContactoEmergencia> findAll();
    ContactoEmergencia save(ContactoEmergencia contacto);
    ContactoEmergencia findById(Long id);
    void delete(Long id);

    // ✅ NUEVO
    List<ContactoEmergencia> findActivosByUsuarioId(Long usuarioId);
}
