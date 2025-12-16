package com.ista.springboot.web.app.models.services;

import java.util.List;
import com.ista.springboot.web.app.models.entity.Usuario;

public interface IUsuarioService {
    public List<Usuario> findAll();
    public Usuario save(Usuario usuario);
    public Usuario findById(Long id);
    public void delete(Long id);
    
    // Nuevo método para buscar por email
    public Usuario findByEmail(String email);
    public Usuario loginWithGoogle(String email, String nombre, String fotoUrl);
    Usuario findByFirebaseUid(String uid);
    Usuario createFromFirebase(String uid, String email, String nombre, String fotoUrl);
    
}