package controller;

import dao.AdminDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Administrador;

import java.io.IOException;

public class RedefinirSenhaController {

    @FXML private TextField campoNovaSenha;
    @FXML private Label labelNovaSenha;
    @FXML private Label labelUsuario;
    @FXML private TextField campoUsuario;
    @FXML private Label mensagemErro;
    @FXML private Label labelPerguntaSecreta;
    @FXML private Text campoPerguntaSecreta;
    @FXML private Label labelRespostaSecreta;
    @FXML private TextField campoRespostaSecreta;
    @FXML private Button procurar;
    @FXML private Button telaInicio;

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
        campoPerguntaSecreta.setVisible(true);
        campoRespostaSecreta.setVisible(true);
        labelPerguntaSecreta.setVisible(true);
        labelRespostaSecreta.setVisible(true);
        labelUsuario.setVisible(false);
        campoUsuario.setVisible(false);
        mensagemErro.setVisible(false);

        procurar.setText("Responder");
        procurar.setOnAction(e -> verificarResposta());
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

    private void mostrarCampoNovaSenha() {
        campoNovaSenha.setVisible(true);
        labelNovaSenha.setVisible(true);
        campoPerguntaSecreta.setVisible(false);
        campoRespostaSecreta.setVisible(false);
        labelPerguntaSecreta.setVisible(false);
        labelRespostaSecreta.setVisible(false);
        mensagemErro.setVisible(false);

        procurar.setText("Redefinir");
        procurar.setOnAction(e -> redefinirSenha());
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
        if (!campo.getStyleClass().contains("erro-login")) campo.getStyleClass().add("erro-login");
    }

    private void limparEstilosErro() {
        campoUsuario.getStyleClass().remove("erro-login");
        campoRespostaSecreta.getStyleClass().remove("erro-login");
        campoNovaSenha.getStyleClass().remove("erro-login");
        mensagemErro.setVisible(false);
    }

    private void voltarParaTelaDeLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/resources/view/login.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) procurar.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/resources/view/style.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
