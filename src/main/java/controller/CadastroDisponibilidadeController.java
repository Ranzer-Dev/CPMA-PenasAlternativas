package controller;

import dao.DisponibilidadeInstituicaoDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.DisponibilidadeInstituicao;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

public class CadastroDisponibilidadeController {

    @FXML
    private TextField campoDia, campoHoraInicio1, campoHoraFim1, campoHoraInicio2, campoHoraFim2;
    @FXML
    private Button btnSalvar;
    @FXML
    private Button btnCancelar;
    private Consumer<DisponibilidadeInstituicao> callback;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
    private int instituicaoId;

    public void setInstituicaoId(int id) {
        this.instituicaoId = id;
    }

    @FXML
    public void initialize() {
        btnSalvar.setOnAction(e -> salvar());
        btnCancelar.setOnAction(e -> fecharJanela());
    }

    private void salvar() {
        if (!validarCampos()) {
            mostrarAlerta("Erro", "Preencha dia da semana, hora início 1 e hora fim 1.");
            return;
        }

        DisponibilidadeInstituicao disponibilidade = new DisponibilidadeInstituicao();
        disponibilidade.setDiaSemana(campoDia.getText().trim());
        disponibilidade.setHoraInicio1(LocalTime.parse(campoHoraInicio1.getText(), formatter));
        disponibilidade.setHoraFim1(LocalTime.parse(campoHoraFim1.getText(), formatter));

        if (!campoHoraInicio2.getText().isEmpty() && !campoHoraFim2.getText().isEmpty()) {
            disponibilidade.setHoraInicio2(LocalTime.parse(campoHoraInicio2.getText(), formatter));
            disponibilidade.setHoraFim2(LocalTime.parse(campoHoraFim2.getText(), formatter));
        }

        if (callback != null) {
            callback.accept(disponibilidade); // Adiciona à lista temporária
        }

        fecharJanela();
    }

    public void setCallback(Consumer<DisponibilidadeInstituicao> callback) {
        this.callback = callback;
    }

    private LocalTime parseHora(String texto, String campo) {
        try {
            return LocalTime.parse(texto, formatter);
        } catch (DateTimeParseException e) {
            mostrarAlerta("Formato inválido", campo + " deve estar no formato HH:mm.");
            return null;
        }
    }

    private LocalTime parseHoraOpcional(String texto, String campo) {
        if (texto.trim().isEmpty()) {
            return null;
        }
        return parseHora(texto, campo);
    }

    private boolean validarCampos() {
        if (campoDia.getText().trim().isEmpty()) {
            return false;
        }
        try {
            LocalTime.parse(campoHoraInicio1.getText(), formatter);
            LocalTime.parse(campoHoraFim1.getText(), formatter);
        } catch (DateTimeParseException e) {
            mostrarAlerta("Formato inválido", "Hora Início 1 e Hora Fim 1 devem estar no formato HH:mm");
            return false;
        }
        return true;
    }

    private void mostrarAlerta(String titulo, String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK) {
            {
                setTitle(titulo);
                setHeaderText(null);
                showAndWait();
            }
        };
    }

    private void fecharJanela() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
