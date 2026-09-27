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
        formValidator.clearAllErrors();
        lblFeedbackMessage.setVisible(false);

        boolean nomeValido = FormValidator.validarCampoObrigatorio(txtNome, lblNomeError, "Nome completo e obrigatorio");
        boolean cpfValido = FormValidator.validarCpf(txtCpf, lblCpfError, "CPF invalido (informe 11 digitos numericos)");
        boolean codigoValido = FormValidator.validarCampoObrigatorio(txtCodigo, lblCodigoError, "Codigo do apenado e obrigatorio");

        if (!nomeValido || !cpfValido || !codigoValido) {
            exibirMensagemErro("Preencha todos os campos obrigatorios destacados em vermelho.");
            return;
        }

        btnSalvar.setDisable(true);

        CadastroUsuarioDTO dto = new CadastroUsuarioDTO();
        dto.setNome(txtNome.getText().trim());
        dto.setCpf(txtCpf.getText().trim().replaceAll("\\D", ""));
        dto.setCodigo(txtCodigo.getText().trim());
        dto.setDataNascimento(txtDataNascimento.getText() != null ? txtDataNascimento.getText().trim() : null);
        dto.setTelefone(txtTelefone.getText() != null ? txtTelefone.getText().trim() : null);
        dto.setEndereco(txtEndereco.getText() != null ? txtEndereco.getText().trim() : null);
        dto.setBairro(txtBairro.getText() != null ? txtBairro.getText().trim() : null);
        dto.setCidade(txtCidade.getText() != null ? txtCidade.getText().trim() : null);
        dto.setCep(txtCep.getText() != null ? txtCep.getText().trim() : null);
        dto.setUf(txtUf.getText() != null ? txtUf.getText().trim() : null);
        dto.setObservacao(txtObservacao.getText() != null ? txtObservacao.getText().trim() : null);
        dto.setAdminId(1L);

        CompletableFuture.supplyAsync(() -> apiClient.cadastrarUsuario(dto))
                .thenAccept(this::processarResposta)
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btnSalvar.setDisable(false);
                        exibirMensagemErro("Falha de conexao com o servidor: " + ex.getMessage());
                    });
                    return null;
                });
    }

    private void processarResposta(ApiResponse<UsuarioDTO> response) {
        Platform.runLater(() -> {
            btnSalvar.setDisable(false);

            if (response.isSuccess() && response.getData() != null) {
                exibirMensagemSucesso("Apenado cadastrado com sucesso! ID: " + response.getData().getIdUsuario());
                limparFormulario();
            } else {
                if (response.getStatusCode() == 422) {
                    formValidator.applyErrors(response.getError());
                }
                String mensagemErro = FormValidator.formatarMensagemErro(response.getError(), response.getStatusCode());
                exibirMensagemErro(mensagemErro);
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
