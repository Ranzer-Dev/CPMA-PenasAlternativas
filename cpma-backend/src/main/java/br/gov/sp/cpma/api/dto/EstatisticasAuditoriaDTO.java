package br.gov.sp.cpma.api.dto;

public class EstatisticasAuditoriaDTO {

    private long totalLogs;
    private long totalSucessos;
    private long totalFalhas;
    private long falhasBiometricas;
    private long falhasCodigo;

    public EstatisticasAuditoriaDTO() {}

    public EstatisticasAuditoriaDTO(long totalLogs, long totalSucessos, long totalFalhas, long falhasBiometricas, long falhasCodigo) {
        this.totalLogs = totalLogs;
        this.totalSucessos = totalSucessos;
        this.totalFalhas = totalFalhas;
        this.falhasBiometricas = falhasBiometricas;
        this.falhasCodigo = falhasCodigo;
    }

    public long getTotalLogs() {
        return totalLogs;
    }

    public void setTotalLogs(long totalLogs) {
        this.totalLogs = totalLogs;
    }

    public long getTotalSucessos() {
        return totalSucessos;
    }

    public void setTotalSucessos(long totalSucessos) {
        this.totalSucessos = totalSucessos;
    }

    public long getTotalFalhas() {
        return totalFalhas;
    }

    public void setTotalFalhas(long totalFalhas) {
        this.totalFalhas = totalFalhas;
    }

    public long getFalhasBiometricas() {
        return falhasBiometricas;
    }

    public void setFalhasBiometricas(long falhasBiometricas) {
        this.falhasBiometricas = falhasBiometricas;
    }

    public long getFalhasCodigo() {
        return falhasCodigo;
    }

    public void setFalhasCodigo(long falhasCodigo) {
        this.falhasCodigo = falhasCodigo;
    }
}
