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
import utils.FormatacaoUtils;
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
import javafx.util.StringConverter;
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

        // Configura formatação de data brasileira
        configurarFormatacaoData();

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

        // Aplica formatação avançada de hora usando FormatacaoUtils
        FormatacaoUtils.aplicarFormatacaoHora(horarioInicio);
        FormatacaoUtils.aplicarFormatacaoHora(horarioAlmoco);
        FormatacaoUtils.aplicarFormatacaoHora(horarioVolta);
        FormatacaoUtils.aplicarFormatacaoHora(horarioSaida);
    }

    private void carregarInstituicoes() {
        List<Instituicao> lista = InstituicaoDAO.buscarTodasInstituicoes();
        instituicao.setItems(FXCollections.observableArrayList(lista));
    }

    private void carregarUsuarios() {
        comboUsuario.setItems(FXCollections.observableArrayList(UsuarioDAO.buscarTodosUsuarios()));
        
        // Configura o converter para mostrar "CPF - Nome"
        comboUsuario.setConverter(new StringConverter<Usuario>() {
            @Override
            public String toString(Usuario usuario) {
                if (usuario == null) {
                    return "";
                }
                return usuario.getCpf() + " - " + usuario.getNome();
            }

            @Override
            public Usuario fromString(String string) {
                // Não usado para ComboBox de objetos
                return null;
            }
        });
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
            LocalTime ini = FormatacaoUtils.getHoraValue(horarioInicio);
            LocalTime alm = FormatacaoUtils.getHoraValue(horarioAlmoco);
            LocalTime vol = FormatacaoUtils.getHoraValue(horarioVolta);
            LocalTime sai = FormatacaoUtils.getHoraValue(horarioSaida);

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

    /**
     * Configura formatação de data brasileira (dd/MM/yyyy)
     */
    private void configurarFormatacaoData() {
        dataTrabalho.setConverter(new javafx.util.StringConverter<java.time.LocalDate>() {
            private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            
            @Override
            public String toString(java.time.LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public java.time.LocalDate fromString(String string) {
                if (string != null && !string.trim().isEmpty()) {
                    return java.time.LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });
    }

    private void calcularHoras() {
        try {
            LocalTime ini = FormatacaoUtils.getHoraValue(horarioInicio);
            LocalTime alm = FormatacaoUtils.getHoraValue(horarioAlmoco);
            LocalTime vol = FormatacaoUtils.getHoraValue(horarioVolta);
            LocalTime sai = FormatacaoUtils.getHoraValue(horarioSaida);

            if (ini != null && alm != null && vol != null && sai != null) {
                long manha = java.time.Duration.between(ini, alm).toMinutes();
                long tarde = java.time.Duration.between(vol, sai).toMinutes();
                long total = manha + tarde;
                if (total < 0) total = 0;

                horasCumpridas.setText(String.format("%.2f", total / 60.0));
            } else {
                horasCumpridas.setText("0.00");
            }
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
