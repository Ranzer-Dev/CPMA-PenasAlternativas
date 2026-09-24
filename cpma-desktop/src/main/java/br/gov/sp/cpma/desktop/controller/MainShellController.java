package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.ApiResponse;
import br.gov.sp.cpma.desktop.client.CpmaApiClient;
import br.gov.sp.cpma.desktop.client.LoginResponseDTO;
import br.gov.sp.cpma.desktop.client.UsuarioDTO;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.List;

public class MainShellController {

    private static MainShellController instance;

    public static MainShellController getInstance() {
        return instance;
    }

    @FXML
    private Label lblAdminNome;

    @FXML
    private Label lblAdminCpf;

    @FXML
    private Label lblSectionTitle;

    @FXML
    private Label lblApiStatus;

    @FXML
    private TextField txtBuscaGlobal;

    @FXML
    private Button btnNavDashboard;

    @FXML
    private Button btnNavApenados;

    @FXML
    private Button btnNavPenas;

    @FXML
    private Button btnNavInstituicoes;

    @FXML
    private Button btnNavFrequencia;

    @FXML
    private Button btnNavTotem;

    @FXML
    private Button btnSair;

    @FXML
    private StackPane contentArea;

    private final CpmaApiClient apiClient = new CpmaApiClient();

    @FXML
    public void initialize() {
        instance = this;

        LoginResponseDTO admin = SessaoAdmin.getAdminLogado();
        if (admin != null) {
            lblAdminNome.setText(admin.getNome() != null ? admin.getNome() : "Admin");
            lblAdminCpf.setText(admin.getCpf() != null ? formatarCpf(admin.getCpf()) : "");
        }

        verificarConexaoBackend();
        navegarParaDashboard();
    }

    private void verificarConexaoBackend() {
        new Thread(() -> {
            ApiResponse<List<UsuarioDTO>> resp = apiClient.listarUsuarios();
            Platform.runLater(() -> {
                if (resp.isSuccess()) {
                    lblApiStatus.setText("● Servidor Conectado");
                    lblApiStatus.getStyleClass().setAll("status-badge-online");
                } else {
                    lblApiStatus.setText("● Servidor Desconectado");
                    lblApiStatus.getStyleClass().setAll("status-badge-offline");
                }
            });
        }).start();
    }

    @FXML
    public void navegarParaDashboard() {
        ativarBotao(btnNavDashboard, "Visao Geral do Sistema");
        carregarConteudo("/br/gov/sp/cpma/desktop/view/dashboardContentView.fxml");
    }

    @FXML
    public void navegarParaApenados() {
        ativarBotao(btnNavApenados, "Gestao de Apenados");
        carregarConteudo("/br/gov/sp/cpma/desktop/view/apenadosView.fxml");
    }

    @FXML
    public void navegarParaPenas() {
        ativarBotao(btnNavPenas, "Gestao de Penas Alternativas");
        carregarConteudo("/br/gov/sp/cpma/desktop/view/penasView.fxml");
    }

    @FXML
    public void navegarParaInstituicoes() {
        ativarBotao(btnNavInstituicoes, "Instituicoes Parceiras");
        carregarConteudo("/br/gov/sp/cpma/desktop/view/instituicoesView.fxml");
    }

    @FXML
    public void navegarParaFrequencia() {
        ativarBotao(btnNavFrequencia, "Frequencia e Cumprimento de Trabalho");
        carregarConteudo("/br/gov/sp/cpma/desktop/view/frequenciaView.fxml");
    }

    @FXML
    public void navegarParaTotem() {
        ativarBotao(btnNavTotem, "Totem de Presenca Facial (Kiosk)");
        carregarConteudo("/br/gov/sp/cpma/desktop/view/totemKioskView.fxml");
    }

    @FXML
    public void handleLogout(ActionEvent event) {
        SessaoAdmin.logout();
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/br/gov/sp/cpma/desktop/view/loginView.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 960, 680));
            stage.setTitle("CPMA - Acesso Administrativo");
            stage.centerOnScreen();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleBuscaGlobal() {
        String termo = txtBuscaGlobal.getText().trim();
        if (!termo.isEmpty()) {
            navegarParaApenados();
        }
    }

    private void ativarBotao(Button ativo, String tituloSecao) {
        lblSectionTitle.setText(tituloSecao);

        Button[] botoes = {btnNavDashboard, btnNavApenados, btnNavPenas, btnNavInstituicoes, btnNavFrequencia, btnNavTotem};
        for (Button b : botoes) {
            b.getStyleClass().remove("nav-button-active");
            if (!b.getStyleClass().contains("nav-button")) {
                b.getStyleClass().add("nav-button");
            }
        }

        ativo.getStyleClass().remove("nav-button");
        ativo.getStyleClass().add("nav-button-active");
    }

    private void carregarConteudo(String fxmlPath) {
        try {
            Parent view = FXMLLoader.load(getClass().getResource(fxmlPath));
            contentArea.getChildren().setAll(view);
        } catch (Exception e) {
            Label err = new Label("Erro ao carregar secao: " + e.getMessage());
            err.getStyleClass().setAll("banner-error");
            contentArea.getChildren().setAll(err);
        }
    }

    private String formatarCpf(String cpf) {
        String num = cpf.replaceAll("\\D", "");
        if (num.length() == 11) {
            return num.substring(0, 3) + "." + num.substring(3, 6) + "." + num.substring(6, 9) + "-" + num.substring(9);
        }
        return cpf;
    }
}
