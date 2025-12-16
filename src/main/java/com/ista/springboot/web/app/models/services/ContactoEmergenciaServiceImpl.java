package com.ista.springboot.web.app.models.services;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ista.springboot.web.app.models.dao.IContactoEmergencia;
import com.ista.springboot.web.app.models.entity.ContactoEmergencia;

@Service
public class ContactoEmergenciaServiceImpl implements IContactoEmergenciaService {

    @Autowired
    private IContactoEmergencia contactoDao;

    @Override
    public List<ContactoEmergencia> findAll() {
        return (List<ContactoEmergencia>) contactoDao.findAll();
    }

    @Override
    public ContactoEmergencia save(ContactoEmergencia contacto) {
        return contactoDao.save(contacto);
    }

    @Override
    public ContactoEmergencia findById(Long id) {
        return contactoDao.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        contactoDao.deleteById(id);
    }
}
