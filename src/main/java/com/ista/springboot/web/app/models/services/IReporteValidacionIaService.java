package com.ista.springboot.web.app.models.services;

import java.util.List;
import com.ista.springboot.web.app.models.entity.ReporteValidacionIa;

public interface IReporteValidacionIaService {
    public List<ReporteValidacionIa> findAll();
    public ReporteValidacionIa save(ReporteValidacionIa reporte);
    public ReporteValidacionIa findById(Long id);
    public void delete(Long id);
}
