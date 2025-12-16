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

    // ✅ ESTE ES EL ARREGLO CLAVE
    @Override
    @Transactional(readOnly = true)
    public List<UsuarioCercanoDTO> findNearby(double lat, double lng, double radio, int lastMinutes, int limit) {
        List<Object[]> rows = ubicacionUsuarioDao.findNearbyNative(lat, lng, radio, lastMinutes, limit);

        return rows.stream().map(r -> {
            // r[0]=id (Number), r[1]=name (String), r[2]=lat (Number), r[3]=lng (Number), r[4]=avatarUrl (String)
            Long id = r[0] == null ? null : ((Number) r[0]).longValue();
            String name = r[1] == null ? "Usuario" : r[1].toString();
            Double latVal = r[2] == null ? null : ((Number) r[2]).doubleValue();
            Double lngVal = r[3] == null ? null : ((Number) r[3]).doubleValue();
            String avatarUrl = r[4] == null ? null : r[4].toString();

            return new UsuarioCercanoDTO(id, name, latVal, lngVal, avatarUrl);
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void delete(Long id) {
        ubicacionUsuarioDao.deleteById(id);
    }
}
