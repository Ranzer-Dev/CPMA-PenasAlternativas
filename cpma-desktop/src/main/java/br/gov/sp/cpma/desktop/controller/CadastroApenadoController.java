package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.ApiResponse;
import br.gov.sp.cpma.desktop.client.CadastroUsuarioDTO;
import br.gov.sp.cpma.desktop.client.CpmaApiClient;
import br.gov.sp.cpma.desktop.client.UsuarioDTO;
import br.gov.sp.cpma.desktop.util.FormValidator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class CadastroApenadoController implements Initializable {

    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;
    @FXML private TextField txtCodigo;
    @FXML private TextField txtDataNascimento;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtEndereco;
    @FXML private TextField txtBairro;
    @FXML private TextField txtCidade;
    @FXML private TextField txtCep;
    @FXML private TextField txtUf;
    @FXML private TextArea txtObservacao;

    @FXML private Label lblNomeError;
    @FXML private Label lblCpfError;
    @FXML private Label lblCodigoError;

    @FXML private Button btnSalvar;
    @FXML private Label lblFeedbackMessage;

    private final CpmaApiClient apiClient = new CpmaApiClient();
    private final FormValidator formValidator = new FormValidator();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        formValidator.registerField("nome", txtNome, lblNomeError);
        formValidator.registerField("cpf", txtCpf, lblCpfError);
        formValidator.registerField("codigo", txtCodigo, lblCodigoError);

        lblFeedbackMessage.setVisible(false);
    }

    @FXML
    public void handleSalvar() {
        btnSalvar.setDisable(true);
        lblFeedbackMessage.setVisible(false);
        formValidator.clearAllErrors();

        CadastroUsuarioDTO dto = new CadastroUsuarioDTO();
        dto.setNome(txtNome.getText());
        dto.setCpf(txtCpf.getText());
        dto.setCodigo(txtCodigo.getText());
        dto.setDataNascimento(txtDataNascimento.getText());
        dto.setTelefone(txtTelefone.getText());
        dto.setEndereco(txtEndereco.getText());
        dto.setBairro(txtBairro.getText());
        dto.setCidade(txtCidade.getText());
        dto.setCep(txtCep.getText());
        dto.setUf(txtUf.getText());
        dto.setObservacao(txtObservacao.getText());
        dto.setAdminId(1L);

        CompletableFuture.supplyAsync(() -> apiClient.cadastrarUsuario(dto))
                .thenAccept(this::processarResposta)
                .exceptionally(ex -> {
                    Platform.runLater(() -> exibirMensagemErro("Erro inesperado ao conectar com a API: " + ex.getMessage()));
                    return null;
                });
    }

    private void processarResposta(ApiResponse<UsuarioDTO> response) {
        Platform.runLater(() -> {
            btnSalvar.setDisable(false);

            if (response.isSuccess()) {
                exibirMensagemSucesso("Apenado cadastrado com sucesso! ID: " + response.getData().getIdUsuario());
                limparFormulario();
            } else if (response.getStatusCode() == 422) {
                formValidator.applyErrors(response.getError());
                exibirMensagemErro("Existem campos obrigatorios nao preenchidos ou invalidos.");
            } else if (response.getStatusCode() == 409) {
                exibirMensagemErro("Conflito: " + response.getError().getMessage());
            } else {
                exibirMensagemErro("Falha: " + (response.getError() != null ? response.getError().getMessage() : "Erro desconhecido"));
            }
        });
    }

    private void exibirMensagemSucesso(String msg) {
        lblFeedbackMessage.setText(msg);
        lblFeedbackMessage.getStyleClass().setAll("banner-success");
        lblFeedbackMessage.setVisible(true);
    }

    private void exibirMensagemErro(String msg) {
        lblFeedbackMessage.setText(msg);
        lblFeedbackMessage.getStyleClass().setAll("banner-error");
        lblFeedbackMessage.setVisible(true);
    }

    private void limparFormulario() {
        txtNome.clear();
        txtCpf.clear();
        txtCodigo.clear();
        txtDataNascimento.clear();
        txtTelefone.clear();
        txtEndereco.clear();
        txtBairro.clear();
        txtCidade.clear();
        txtCep.clear();
        txtUf.clear();
        txtObservacao.clear();
        formValidator.clearAllErrors();
    }
}
