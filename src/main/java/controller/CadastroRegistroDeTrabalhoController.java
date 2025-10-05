package controller;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import dao.InstituicaoDAO;
import dao.PenaDAO;
import dao.RegistroDeTrabalhoDAO;
import dao.UsuarioDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Instituicao;
import model.Pena;
import model.RegistroDeTrabalho;
import model.Usuario;

public class CadastroRegistroDeTrabalhoController {

    @FXML private ComboBox<Instituicao> instituicao;
    @FXML private ComboBox<Usuario> comboUsuario;
    @FXML private ComboBox<Pena> comboPena;
    @FXML private DatePicker dataTrabalho;
    @FXML private TextField horasCumpridas;
    @FXML private TextArea atividades;
    @FXML private TextField horarioInicio;
    @FXML private TextField horarioAlmoco;
    @FXML private TextField horarioVolta;
    @FXML private TextField horarioSaida;
    @FXML private Button btnCadastrar;

    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

    @FXML
    public void initialize() {
        carregarUsuarios();
        carregarInstituicoes();
        btnCadastrar.setOnAction(e -> cadastrar());
        comboPena.setDisable(true);

        comboUsuario.setOnAction(e -> {
            Usuario u = comboUsuario.getValue();
            if (u != null) {
                carregarPenasDoUsuario(u.getIdUsuario());
                comboPena.setDisable(false);
            } else {
                comboPena.getItems().clear();
                comboPena.setDisable(true);
            }
        });

        horasCumpridas.setEditable(false);
        horasCumpridas.setFocusTraversable(false);

        horarioInicio.textProperty().addListener((obs, o, n) -> calcularHoras());
        horarioAlmoco.textProperty().addListener((obs, o, n) -> calcularHoras());
        horarioVolta.textProperty().addListener((obs, o, n) -> calcularHoras());
        horarioSaida.textProperty().addListener((obs, o, n) -> calcularHoras());

        aplicarMascaraHora(horarioInicio);
        aplicarMascaraHora(horarioAlmoco);
        aplicarMascaraHora(horarioVolta);
        aplicarMascaraHora(horarioSaida);
    }

    private void carregarInstituicoes() {
        List<Instituicao> lista = InstituicaoDAO.buscarTodasInstituicoes();
        instituicao.setItems(FXCollections.observableArrayList(lista));
    }

    private void carregarUsuarios() {
        comboUsuario.setItems(FXCollections.observableArrayList(UsuarioDAO.buscarTodosUsuarios()));
    }

    private void carregarPenasDoUsuario(int idUsuario) {
        comboPena.setItems(FXCollections.observableArrayList(PenaDAO.buscarPenasPorUsuario(idUsuario)));
    }

    private void cadastrar() {
        try {
            Usuario user = comboUsuario.getValue();
            Pena pena = comboPena.getValue();
            Instituicao inst = instituicao.getValue();

            if (user == null || pena == null || inst == null || dataTrabalho.getValue() == null) {
                alert("Escolha usuário, pena, instituição e data.");
                return;
            }

            double horas = Double.parseDouble(horasCumpridas.getText().trim().replace(",", "."));
            LocalTime ini = converterHora(horarioInicio);
            LocalTime alm = converterHora(horarioAlmoco);
            LocalTime vol = converterHora(horarioVolta);
            LocalTime sai = converterHora(horarioSaida);

            if (horas <= 0) {
                alert("Horas cumpridas inválidas.");
                return;
            }

            RegistroDeTrabalho registro = new RegistroDeTrabalho();
            registro.setFkPenaId(pena.getIdPena());
            registro.setDataTrabalho(Date.valueOf(dataTrabalho.getValue()));
            registro.setHorasCumpridas(horas);
            registro.setAtividades(atividades.getText());
            registro.setHorarioInicio(Time.valueOf(ini + ":00"));
            registro.setHorarioAlmoco(Time.valueOf(alm + ":00"));
            registro.setHorarioVolta(Time.valueOf(vol + ":00"));
            registro.setHorarioSaida(Time.valueOf(sai + ":00"));

            boolean ok = new RegistroDeTrabalhoDAO().inserir(registro);
            alert(ok ? "Registro gravado!" : "Falha ao gravar.");
            if (ok) {
                fecharJanela();
            }
        } catch (Exception ex) {
            alert("Erro: " + ex.getMessage());
        }
    }

    private LocalTime converterHora(TextField campo) {
        String dig = campo.getText().replaceAll("[^0-9]", "");
        if (dig.length() < 4) throw new IllegalArgumentException("Hora incompleta: " + campo.getText());
        if (dig.length() > 4) dig = dig.substring(0, 4);
        String h = dig.substring(0, 2) + ":" + dig.substring(2, 4);
        return LocalTime.parse(h, fmt);
    }

    private void aplicarMascaraHora(TextField campo) {
        campo.textProperty().addListener((obs, oldValue, newValue) -> {
            String valor = newValue.replaceAll("[^0-9]", "");
            if (valor.length() > 4) valor = valor.substring(0, 4);

            StringBuilder f = new StringBuilder();
            for (int i = 0; i < valor.length(); i++) {
                f.append(valor.charAt(i));
                if (i == 1 && valor.length() > 2) f.append(":");
            }

            int caret = campo.getCaretPosition();
            javafx.application.Platform.runLater(() -> {
                campo.setText(f.toString());
                campo.positionCaret(Math.min(caret, campo.getText().length()));
            });
        });
    }

    private void calcularHoras() {
        try {
            LocalTime ini = converterHora(horarioInicio);
            LocalTime alm = converterHora(horarioAlmoco);
            LocalTime vol = converterHora(horarioVolta);
            LocalTime sai = converterHora(horarioSaida);

            long manha = java.time.Duration.between(ini, alm).toMinutes();
            long tarde = java.time.Duration.between(vol, sai).toMinutes();
            long total = manha + tarde;
            if (total < 0) total = 0;

            horasCumpridas.setText(String.format("%.2f", total / 60.0));
        } catch (Exception e) {
            horasCumpridas.setText("0.00");
        }
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    private void fecharJanela() {
        Stage stage = (Stage) btnCadastrar.getScene().getWindow();
        stage.close();
    }
}
