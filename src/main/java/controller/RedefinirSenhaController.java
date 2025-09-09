package controller;

import java.io.IOException;

import dao.AdminDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Administrador;

public class RedefinirSenhaController {

    @FXML
    private VBox painelProcurarUsuario;
    @FXML
    private VBox painelPerguntaSecreta;
    @FXML
    private VBox painelNovaSenha;
    @FXML
    private PasswordField campoNovaSenha;
    @FXML
    private Label labelNovaSenha;
    @FXML
    private Label labelUsuario;
    @FXML
    private TextField campoUsuario;
    @FXML
    private Label mensagemErro;
    @FXML
    private Label labelPerguntaSecreta;
    @FXML
    private Text campoPerguntaSecreta;
    @FXML
    private Label labelRespostaSecreta;
    @FXML
    private TextField campoRespostaSecreta;
    @FXML
    private Button procurar;
    @FXML
    private Button telaInicio;

    private Administrador adminAtual;
    private final AdminDAO adminDAO = new AdminDAO();

    @FXML
    private void initialize() {
        procurar.setOnAction(e -> procurarUsuario());
        telaInicio.setOnAction(e -> voltarParaTelaDeLogin());

        campoUsuario.textProperty().addListener((obs, o, n) -> limparEstilosErro());
        campoRespostaSecreta.textProperty().addListener((obs, o, n) -> limparEstilosErro());
        campoNovaSenha.textProperty().addListener((obs, o, n) -> limparEstilosErro());
    }

    private void procurarUsuario() {
        String cpf = campoUsuario.getText();
        Administrador admin = adminDAO.buscarPorCpf(cpf);

        if (admin != null && cpf.equals(admin.getCpf())) {
            prepararPerguntaSecreta(admin);
        } else {
            adicionarEstiloErro(campoUsuario);
            mostrarErro("Usuário não encontrado.");
        }
    }

    private void prepararPerguntaSecreta(Administrador admin) {
        adminAtual = admin;
        campoPerguntaSecreta.setText(admin.getPerguntaSecreta());
        mensagemErro.setVisible(false);

        // Esconde o painel do passo 1
        painelProcurarUsuario.setVisible(false);
        painelProcurarUsuario.setManaged(false); // Libera o espaço do painel

        // Mostra o painel do passo 2
        painelPerguntaSecreta.setVisible(true);
        painelPerguntaSecreta.setManaged(true);

        // Reconfigura o botão para a próxima ação
        procurar.setText("Responder");
        procurar.setOnAction(e -> verificarResposta());
    }

    private void mostrarCampoNovaSenha() {
        mensagemErro.setVisible(false);

        // Esconde o painel do passo 2
        painelPerguntaSecreta.setVisible(false);
        painelPerguntaSecreta.setManaged(false);

        // Mostra o painel do passo 3
        painelNovaSenha.setVisible(true);
        painelNovaSenha.setManaged(true);

        // Reconfigura o botão para a ação final
        procurar.setText("Redefinir");
        procurar.setOnAction(e -> redefinirSenha());
    }

    private void verificarResposta() {
        String resposta = campoRespostaSecreta.getText();

        if (resposta.equals(adminAtual.getRespostaSecreta())) {
            mostrarCampoNovaSenha();
        } else {
            mostrarErro("Resposta incorreta.");
            adicionarEstiloErro(campoRespostaSecreta);
        }
    }

    private void redefinirSenha() {
        String novaSenha = campoNovaSenha.getText();
        if (novaSenha.isEmpty()) {
            mostrarErro("A nova senha não pode estar vazia.");
            adicionarEstiloErro(campoNovaSenha);
            return;
        }

        adminDAO.alterarSenhaPorCpf(adminAtual.getCpf(), novaSenha);
        voltarParaTelaDeLogin();
    }

    private void mostrarErro(String mensagem) {
        mensagemErro.setText(mensagem);
        mensagemErro.setStyle("-fx-text-fill: red;");
        mensagemErro.setVisible(true);
    }

    private void adicionarEstiloErro(Control campo) {
        if (!campo.getStyleClass().contains("erro-login")) {
            campo.getStyleClass().add("erro-login");
        }
    }

    private void limparEstilosErro() {
        campoUsuario.getStyleClass().remove("erro-login");
        campoRespostaSecreta.getStyleClass().remove("erro-login");
        campoNovaSenha.getStyleClass().remove("erro-login");
        mensagemErro.setVisible(false);
    }

    private void voltarParaTelaDeLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/cpma/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) procurar.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/mycompany/cpma/style.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
