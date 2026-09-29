package br.gov.sp.cpma.desktop.client;

import javafx.beans.property.*;
import java.time.LocalDate;

public class ItemFolhaPontoMensal {

    private final BooleanProperty presente = new SimpleBooleanProperty(true);
    private final ObjectProperty<LocalDate> data = new SimpleObjectProperty<>();
    private final StringProperty diaSemana = new SimpleStringProperty();
    private final StringProperty horarioInicio = new SimpleStringProperty();
    private final StringProperty horarioAlmoco = new SimpleStringProperty();
    private final StringProperty horarioVolta = new SimpleStringProperty();
    private final StringProperty horarioSaida = new SimpleStringProperty();
    private final DoubleProperty horasCumpridas = new SimpleDoubleProperty();
    private final StringProperty atividades = new SimpleStringProperty();
    private final StringProperty statusTexto = new SimpleStringProperty("PRESENTE");
    private double horasPadraoDia = 4.0;

    public ItemFolhaPontoMensal(LocalDate data, String diaSemana, String inicio, String almoco, String volta, String saida, double horas, String atividadesPadrao) {
        this.data.set(data);
        this.diaSemana.set(diaSemana);
        this.horarioInicio.set(inicio != null ? inicio : "");
        this.horarioAlmoco.set(almoco != null ? almoco : "");
        this.horarioVolta.set(volta != null ? volta : "");
        this.horarioSaida.set(saida != null ? saida : "");
        this.horasPadraoDia = horas > 0 ? horas : 4.0;
        this.horasCumpridas.set(this.horasPadraoDia);
        this.atividades.set(atividadesPadrao != null ? atividadesPadrao : "Prestação de Serviços à Comunidade");

        this.presente.addListener((obs, oldVal, newVal) -> {
            if (Boolean.TRUE.equals(newVal)) {
                this.statusTexto.set("PRESENTE");
                this.horasCumpridas.set(this.horasPadraoDia);
            } else {
                this.statusTexto.set("FALTA");
                this.horasCumpridas.set(0.0);
            }
        });
    }

    public BooleanProperty presenteProperty() { return presente; }
    public boolean isPresente() { return presente.get(); }
    public void setPresente(boolean p) { presente.set(p); }

    public ObjectProperty<LocalDate> dataProperty() { return data; }
    public LocalDate getData() { return data.get(); }
    public void setData(LocalDate d) { data.set(d); }

    public StringProperty diaSemanaProperty() { return diaSemana; }
    public String getDiaSemana() { return diaSemana.get(); }
    public void setDiaSemana(String d) { diaSemana.set(d); }

    public StringProperty horarioInicioProperty() { return horarioInicio; }
    public String getHorarioInicio() { return horarioInicio.get(); }
    public void setHorarioInicio(String s) { horarioInicio.set(s); }

    public StringProperty horarioAlmocoProperty() { return horarioAlmoco; }
    public String getHorarioAlmoco() { return horarioAlmoco.get(); }
    public void setHorarioAlmoco(String s) { horarioAlmoco.set(s); }

    public StringProperty horarioVoltaProperty() { return horarioVolta; }
    public String getHorarioVolta() { return horarioVolta.get(); }
    public void setHorarioVolta(String s) { horarioVolta.set(s); }

    public StringProperty horarioSaidaProperty() { return horarioSaida; }
    public String getHorarioSaida() { return horarioSaida.get(); }
    public void setHorarioSaida(String s) { horarioSaida.set(s); }

    public DoubleProperty horasCumpridasProperty() { return horasCumpridas; }
    public double getHorasCumpridas() { return horasCumpridas.get(); }
    public void setHorasCumpridas(double h) { horasCumpridas.set(h); }

    public StringProperty atividadesProperty() { return atividades; }
    public String getAtividades() { return atividades.get(); }
    public void setAtividades(String a) { atividades.set(a); }

    public StringProperty statusTextoProperty() { return statusTexto; }
    public String getStatusTexto() { return statusTexto.get(); }
    public void setStatusTexto(String s) { statusTexto.set(s); }

    public double getHorasPadraoDia() { return horasPadraoDia; }
}
