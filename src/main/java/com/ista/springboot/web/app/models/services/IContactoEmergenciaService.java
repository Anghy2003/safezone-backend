package com.ista.springboot.web.app.models.services;

import java.util.List;
import com.ista.springboot.web.app.models.entity.ContactoEmergencia;

public interface IContactoEmergenciaService {
    public List<ContactoEmergencia> findAll();
    public ContactoEmergencia save(ContactoEmergencia contacto);
    public ContactoEmergencia findById(Long id);
    public void delete(Long id);
}
