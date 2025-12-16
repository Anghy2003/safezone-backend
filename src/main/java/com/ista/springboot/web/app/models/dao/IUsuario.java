package com.ista.springboot.web.app.models.dao;

import org.springframework.data.repository.CrudRepository;


import com.ista.springboot.web.app.models.entity.Usuario;

public interface IUsuario extends CrudRepository<Usuario, Long> {
    // Spring Data JPA creará automáticamente la implementación de este método
    Usuario findByEmail(String email);
 
    Usuario findByFirebaseUid(String firebaseUid);
}