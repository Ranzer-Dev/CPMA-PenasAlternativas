package controller;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.function.Consumer;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.DisponibilidadeInstituicao;
import utils.FormatacaoUtils;

public class CadastroDisponibilidadeController {

    @FXML
    private TextField campoDia;
    @FXML
    private TextField campoHoraInicio1, campoHoraFim1, campoHoraInicio2, campoHoraFim2;
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
        configurarFormatacaoHoras();
    }
    
    /**
     * Configura a formatação de campos de hora usando FormatacaoUtils
     */
    private void configurarFormatacaoHoras() {
        // Aplica formatação automática usando a classe utilitária
        FormatacaoUtils.aplicarFormatacaoHora(campoHoraInicio1);
        FormatacaoUtils.aplicarFormatacaoHora(campoHoraFim1);
        FormatacaoUtils.aplicarFormatacaoHora(campoHoraInicio2);
        FormatacaoUtils.aplicarFormatacaoHora(campoHoraFim2);
    }

    private void salvar() {
        if (!validarCampos()) {
            mostrarAlerta("Erro", "Preencha dia da semana, hora início 1 e hora fim 1.");
            return;
        }

        DisponibilidadeInstituicao disponibilidade = new DisponibilidadeInstituicao();
        disponibilidade.setDiaSemana(campoDia.getText().trim());
        disponibilidade.setHoraInicio1(FormatacaoUtils.getHoraValue(campoHoraInicio1));
        disponibilidade.setHoraFim1(FormatacaoUtils.getHoraValue(campoHoraFim1));

        if (!campoHoraInicio2.getText().trim().isEmpty() && !campoHoraFim2.getText().trim().isEmpty()) {
            disponibilidade.setHoraInicio2(FormatacaoUtils.getHoraValue(campoHoraInicio2));
            disponibilidade.setHoraFim2(FormatacaoUtils.getHoraValue(campoHoraFim2));
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
        if (campoHoraInicio1.getText().trim().isEmpty()) {
            mostrarAlerta("Erro", "Hora de início é obrigatória!");
            return false;
        }
        if (campoHoraFim1.getText().trim().isEmpty()) {
            mostrarAlerta("Erro", "Hora de fim é obrigatória!");
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
