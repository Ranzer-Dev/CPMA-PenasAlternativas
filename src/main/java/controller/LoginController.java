package controller;

import dao.AdminDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Administrador;
import util.HashUtil;
import util.SessaoUsuario;

public class LoginController {

    @FXML
    private Label mensagemErro;
    @FXML
    private TextField campoUsuario;
    @FXML
    private PasswordField campoSenha;
    @FXML
    private Button botaoEntrar;
    @FXML
    private Hyperlink linkRedefinirSenha;

    private final AdminDAO adminDAO = new AdminDAO();
    private Administrador adminAutenticado;

    @FXML
    private void initialize() {
        campoUsuario.setText("12345678900");
        botaoEntrar.setOnAction(this::autenticarUsuario);
        campoUsuario.textProperty().addListener((obs, oldText, newText) -> limparErros());
        linkRedefinirSenha.setOnAction(this::abrirRedefinirSenha);
    }

    private void autenticarUsuario(javafx.event.ActionEvent event) {
        String cpf = campoUsuario.getText();
        System.out.println("CPF: " + cpf);
        String senha = campoSenha.getText();
        System.out.println("Senha: " + senha);

        if (autenticar(cpf, senha)) {
            SessaoUsuario.setAdminLogado(adminAutenticado);
            limparErros();
            trocarCena("/com/mycompany/cpma/buscaCadastroView.fxml", event);
        } else {
            mostrarErroLogin();
        }
    }

    private boolean autenticar(String cpf, String senhaDigitada) {
        Administrador admin = adminDAO.buscarPorCpf(cpf);
        if (admin != null) {
            String senhaHash = HashUtil.gerarHash(senhaDigitada);
            if (senhaHash.equals(admin.getSenha())) {
                this.adminAutenticado = admin;
                return true;
            }
        }
        return false;
    }

    private void abrirRedefinirSenha(javafx.event.ActionEvent event) {
        trocarCena("/com/mycompany/cpma/redefinirSenha.fxml", event);
    }

    private void trocarCena(String fxml, javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent raiz = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            Scene cena = new Scene(raiz);
            cena.getStylesheets().add(getClass().getResource("/com/mycompany/cpma/style.css").toExternalForm());
            stage.setScene(cena);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mostrarErroLogin() {
        mensagemErro.setVisible(true);
        if (!campoUsuario.getStyleClass().contains("erro-login")) {
            campoUsuario.getStyleClass().add("erro-login");
        }
        if (!campoSenha.getStyleClass().contains("erro-login")) {
            campoSenha.getStyleClass().add("erro-login");
        }
    }

    private void limparErros() {
        campoUsuario.getStyleClass().remove("erro-login");
        campoSenha.getStyleClass().remove("erro-login");
        mensagemErro.setVisible(false);
    }

    public Administrador getAdminAutenticado() {
        return adminAutenticado;
    }
}
