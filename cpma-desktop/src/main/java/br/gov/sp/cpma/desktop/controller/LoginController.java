package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.ApiResponse;
import br.gov.sp.cpma.desktop.client.CpmaApiClient;
import br.gov.sp.cpma.desktop.client.LoginResponseDTO;
import br.gov.sp.cpma.desktop.util.FormValidator;
import br.gov.sp.cpma.desktop.util.SessaoAdmin;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class LoginController {

    @FXML
    private TextField txtCpf;

    @FXML
    private PasswordField txtSenha;

    @FXML
    private Label lblError;

    @FXML
    private Label lblCpfError;

    @FXML
    private Label lblSenhaError;

    @FXML
    private Button btnEntrar;

    @FXML
    private Button btnTotem;

    private final CpmaApiClient apiClient = new CpmaApiClient();

    @FXML
    public void initialize() {
        FormValidator.limparAoDigitar(txtCpf, lblCpfError);
        FormValidator.limparAoDigitar(txtSenha, lblSenhaError);
        txtCpf.setText("12345678900");
    }

    @FXML
    public void handleLogin(ActionEvent event) {
        boolean cpfValido = FormValidator.validarCampoObrigatorio(txtCpf, lblCpfError, "CPF e obrigatorio");
        boolean senhaValida = FormValidator.validarCampoObrigatorio(txtSenha, lblSenhaError, "Senha e obrigatoria");

        if (!cpfValido || !senhaValida) {
            return;
        }

        btnEntrar.setDisable(true);
        btnEntrar.setText("Autenticando...");
        lblError.setVisible(false);

        String cpfLimpo = txtCpf.getText().trim().replaceAll("\\D", "");
        String senha = txtSenha.getText();

        new Thread(() -> {
            ApiResponse<LoginResponseDTO> response = apiClient.login(cpfLimpo, senha);

            Platform.runLater(() -> {
                btnEntrar.setDisable(false);
                btnEntrar.setText("Entrar no Sistema");

                if (response.isSuccess() && response.getData() != null) {
                    SessaoAdmin.setAdminLogado(response.getData());
                    abrirMainShell(event);
                } else {
                    String mensagemErro = response.getError() != null
                            ? response.getError().getMessage()
                            : "Falha ao autenticar. Verifique credenciais.";
                    FormValidator.marcarErro(txtCpf, lblCpfError, "");
                    FormValidator.marcarErro(txtSenha, lblSenhaError, "");
                    lblError.setText(mensagemErro);
                    lblError.getStyleClass().setAll("banner-error");
                    lblError.setVisible(true);
                }
            });
        }).start();
    }

    @FXML
    public void handleAbrirTotem(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/br/gov/sp/cpma/desktop/view/totemKioskView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1024, 720));
            stage.setTitle("CPMA - Totem de Presenca Facial (Kiosk)");
        } catch (Exception e) {
            lblError.setText("Erro ao carregar Totem: " + e.getMessage());
            lblError.getStyleClass().setAll("banner-error");
            lblError.setVisible(true);
        }
    }

    private void abrirMainShell(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/br/gov/sp/cpma/desktop/view/mainShellView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 1280, 800));
            stage.setTitle("CPMA - Painel Administrativo de Penas Alternativas");
            stage.centerOnScreen();
        } catch (Exception e) {
            lblError.setText("Erro ao carregar Painel Principal: " + e.getMessage());
            lblError.getStyleClass().setAll("banner-error");
            lblError.setVisible(true);
        }
    }
}
