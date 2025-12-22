package com.ista.springboot.web.app.models.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ista.springboot.web.app.dto.UsuarioCercanoDTO;
import com.ista.springboot.web.app.models.dao.IUbicacionUsuario;
import com.ista.springboot.web.app.models.entity.UbicacionUsuario;

@Service
public class UbicacionUsuarioServiceImpl implements IUbicacionUsuarioService {

    @Autowired
    private IUbicacionUsuario ubicacionUsuarioDao;

    @Override
    @Transactional(readOnly = true)
    public List<UbicacionUsuario> findAll() {
        return (List<UbicacionUsuario>) ubicacionUsuarioDao.findAll();
    }

    @Override
    @Transactional
    public UbicacionUsuario save(UbicacionUsuario ubicacion) {
        return ubicacionUsuarioDao.save(ubicacion);
    }

    @Override
    @Transactional(readOnly = true)
    public UbicacionUsuario findById(Long id) {
        return ubicacionUsuarioDao.findById(id).orElse(null);
    }

    @Override
    @Transactional(readOnly = true)
    public UbicacionUsuario findByUsuarioId(Long usuarioId) {
        return ubicacionUsuarioDao.findByUsuarioId(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioCercanoDTO> findNearby(double lat, double lng, double radio, int lastMinutes, int limit) {
        List<Object[]> rows = ubicacionUsuarioDao.findNearbyNative(lat, lng, radio, lastMinutes, limit);

        return rows.stream().map(r -> {
            Long id = asLong(r, 0);
            String name = asString(r, 1, "Usuario");
            Double latVal = asDouble(r, 2);
            Double lngVal = asDouble(r, 3);
            String avatarUrl = asString(r, 4, null);

            return new UsuarioCercanoDTO(id, name, latVal, lngVal, avatarUrl);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ubicacionUsuarioDao.deleteById(id);
    }

    // ===================== HELPERS (robustos) =====================

    private static Long asLong(Object[] r, int idx) {
        Object v = (r == null || r.length <= idx) ? null : r[idx];
        if (v == null) return null;
        if (v instanceof Number n) return n.longValue();
        return Long.valueOf(v.toString());
    }

    private static Double asDouble(Object[] r, int idx) {
        Object v = (r == null || r.length <= idx) ? null : r[idx];
        if (v == null) return null;
        if (v instanceof Number n) return n.doubleValue();
        return Double.valueOf(v.toString());
    }

    private static String asString(Object[] r, int idx, String def) {
        Object v = (r == null || r.length <= idx) ? null : r[idx];
        if (v == null) return def;
        String s = v.toString();
        return s.isBlank() ? def : s;
    }
}
