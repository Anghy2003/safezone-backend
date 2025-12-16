package com.ista.springboot.web.app.models.services;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ista.springboot.web.app.models.dao.IUsuario;
import com.ista.springboot.web.app.models.entity.Usuario;

@Service
public class UsuarioServiceImpl implements IUsuarioService {

    @Autowired
    private IUsuario usuarioDao;

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> findAll() {
        return (List<Usuario>) usuarioDao.findAll();
    }

    @Override
    @Transactional
    public Usuario save(Usuario usuario) {
        return usuarioDao.save(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario findById(Long id) {
        return usuarioDao.findById(id).orElse(null);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        usuarioDao.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Usuario findByEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return usuarioDao.findByEmail(email.trim().toLowerCase());
    }

    // ✅ Google/Firebase: SOLO valida y actualiza si ya existe. NO crea.
    @Override
    @Transactional
    public Usuario loginWithGoogle(String email, String nombre, String fotoUrl) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email es obligatorio para loginWithGoogle");
        }

        Usuario usuario = usuarioDao.findByEmail(email.trim().toLowerCase());
        if (usuario == null) return null;

        OffsetDateTime now = OffsetDateTime.now();

        if ((usuario.getNombre() == null || usuario.getNombre().isBlank())
                && nombre != null && !nombre.isBlank()) {
            usuario.setNombre(nombre.trim());
        }

        if (fotoUrl != null && !fotoUrl.isBlank()) {
            usuario.setFotoUrl(fotoUrl.trim());
        }

        usuario.setUltimoAcceso(now);
        return usuarioDao.save(usuario);
    }

    // Si tu interface todavía tiene estos métodos, déjalos así (no se usan en este modelo):
    @Override
    @Transactional(readOnly = true)
    public Usuario findByFirebaseUid(String uid) {
        return null;
    }

    @Override
    @Transactional
    public Usuario createFromFirebase(String uid, String email, String nombre, String fotoUrl) {
        throw new UnsupportedOperationException(
                "createFromFirebase deshabilitado: el registro se realiza solo en Supabase vía POST /api/usuarios"
        );
    }
}
