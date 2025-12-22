package com.ista.springboot.web.app.dto;

import java.util.List;

public class ZonaRiesgoResponseDTO {

    public static class MotivoDTO {
        private String tipo;
        private long count;

        public MotivoDTO() {}
        public MotivoDTO(String tipo, long count) {
            this.tipo = tipo;
            this.count = count;
        }

        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }
    }

    private String nivel; // BAJO | MEDIO | ALTO
    private double score; // 0..1
    private int radioM;
    private int dias;
    private long total;
    private List<MotivoDTO> motivos;

    public ZonaRiesgoResponseDTO() {}

    public ZonaRiesgoResponseDTO(String nivel, double score, int radioM, int dias, long total, List<MotivoDTO> motivos) {
        this.nivel = nivel;
        this.score = score;
        this.radioM = radioM;
        this.dias = dias;
        this.total = total;
        this.motivos = motivos;
    }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    public int getRadioM() { return radioM; }
    public void setRadioM(int radioM) { this.radioM = radioM; }

    public int getDias() { return dias; }
    public void setDias(int dias) { this.dias = dias; }

    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }

    public List<MotivoDTO> getMotivos() { return motivos; }
    public void setMotivos(List<MotivoDTO> motivos) { this.motivos = motivos; }
}
