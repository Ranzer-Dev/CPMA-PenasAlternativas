package br.gov.sp.cpma.desktop.client;

public class EstatisticasAuditoriaDTO {

    private long totalLogs;
    private long totalSucessos;
    private long totalFalhas;
    private long falhasBiometricas;
    private long falhasCodigo;

    public EstatisticasAuditoriaDTO() {}

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
