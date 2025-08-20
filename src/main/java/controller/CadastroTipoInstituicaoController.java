package controller;

import dao.TipoInstituicaoDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class CadastroTipoInstituicaoController {

    @FXML private TextField campoTipo;
    @FXML private Button btnSalvar;
    @FXML private Button btnCancelar;

    @FXML
    public void initialize() {
        btnSalvar.setOnAction(e -> salvar());
        btnCancelar.setOnAction(e -> fecharJanela());
    }

    private void salvar() {
        String tipo = campoTipo.getText().trim();
        if (tipo.isEmpty()) {
            mostrarAlerta("Atenção", "O campo 'Tipo' não pode estar vazio.");
            return;
        }

        boolean sucesso = TipoInstituicaoDAO.inserir(tipo);
        mostrarAlerta(sucesso ? "Sucesso" : "Erro", sucesso ?
                "Tipo de Instituição cadastrado com sucesso." :
                "Erro ao salvar o tipo.");
        if (sucesso) fecharJanela();
    }

    private void mostrarAlerta(String titulo, String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK) {{
            setTitle(titulo); setHeaderText(null); showAndWait();
        }};
    }

    private void fecharJanela() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }
}
