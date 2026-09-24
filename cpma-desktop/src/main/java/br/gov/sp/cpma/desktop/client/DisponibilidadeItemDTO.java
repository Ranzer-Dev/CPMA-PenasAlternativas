package br.gov.sp.cpma.desktop.client;

public class DisponibilidadeItemDTO {

    private String diaSemana;
    private String horaInicio1;
    private String horaFim1;
    private String horaInicio2;
    private String horaFim2;

    public DisponibilidadeItemDTO() {}

    public DisponibilidadeItemDTO(String diaSemana, String horaInicio1, String horaFim1, String horaInicio2, String horaFim2) {
        this.diaSemana = diaSemana;
        this.horaInicio1 = horaInicio1;
        this.horaFim1 = horaFim1;
        this.horaInicio2 = horaInicio2;
        this.horaFim2 = horaFim2;
    }

    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }
    public String getHoraInicio1() { return horaInicio1; }
    public void setHoraInicio1(String horaInicio1) { this.horaInicio1 = horaInicio1; }
    public String getHoraFim1() { return horaFim1; }
    public void setHoraFim1(String horaFim1) { this.horaFim1 = horaFim1; }
    public String getHoraInicio2() { return horaInicio2; }
    public void setHoraInicio2(String horaInicio2) { this.horaInicio2 = horaInicio2; }
    public String getHoraFim2() { return horaFim2; }
    public void setHoraFim2(String horaFim2) { this.horaFim2 = horaFim2; }
}
