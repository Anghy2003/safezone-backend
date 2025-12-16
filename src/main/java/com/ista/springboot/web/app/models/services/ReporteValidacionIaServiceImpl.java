package com.ista.springboot.web.app.models.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ista.springboot.web.app.models.dao.IReporteValidacionIa;
import com.ista.springboot.web.app.models.entity.ReporteValidacionIa;

@Service
public class ReporteValidacionIaServiceImpl implements IReporteValidacionIaService {

    @Autowired
    private IReporteValidacionIa reporteDao;

    @Override
    public List<ReporteValidacionIa> findAll() {
        return (List<ReporteValidacionIa>) reporteDao.findAll();
    }

    @Override
    public ReporteValidacionIa save(ReporteValidacionIa reporte) {
        return reporteDao.save(reporte);
    }

    @Override
    public ReporteValidacionIa findById(Long id) {
        return reporteDao.findById(id).orElse(null);
    }

    @Override
    public void delete(Long id) {
        reporteDao.deleteById(id);
    }
}
