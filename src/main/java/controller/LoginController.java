package controller;

import java.io.IOException;

import dao.AdminDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
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
    @FXML
    private TextField campoSenhaVisivel;
    @FXML
    private ImageView iconeOlho;
    @FXML
    private Button botaoConsulta;

    private final AdminDAO adminDAO = new AdminDAO();
    private Administrador adminAutenticado;
    private Image iconeOlhoAberto;
    private Image iconeOlhoFechado;
    private boolean senhaEstaVisivel = false;

    @FXML
    private void initialize() {
        campoUsuario.setText("12345678900");
        botaoEntrar.setOnAction(this::autenticarUsuario);
        campoUsuario.textProperty().addListener((obs, oldText, newText) -> limparErros());
        linkRedefinirSenha.setOnAction(this::abrirRedefinirSenha);
        campoSenha.setOnAction(this::autenticarUsuario);
        campoUsuario.setOnAction(this::autenticarUsuario);
        iconeOlhoAberto = new Image(getClass().getResourceAsStream("/images/eye-open.png"));
        iconeOlhoFechado = new Image(getClass().getResourceAsStream("/images/eye-closed.png"));
        campoSenhaVisivel.textProperty().bindBidirectional(campoSenha.textProperty());
        iconeOlho.addEventHandler(MouseEvent.MOUSE_CLICKED, this::toggleVisibilidadeSenha);
        botaoConsulta.setOnAction(this::abrirConsultaPublica);
    }

    @FXML
    private void autenticarUsuario(javafx.event.ActionEvent event) {
        String cpf = campoUsuario.getText();
        String senha = campoSenha.getText();

        if (cpf == null || cpf.trim().isEmpty()) {
            mostrarErroLogin();
            return;
        }
        
        if (senha == null || senha.trim().isEmpty()) {
            mostrarErroLogin();
            return;
        }

        boolean autenticado = autenticar(cpf, senha);
        
        if (autenticado) {
            SessaoUsuario.setAdminLogado(adminAutenticado);
            limparErros();
            trocarCena("/com/mycompany/cpma/buscaCadastroView.fxml", event);
        } else {
            mostrarErroLogin();
        }
    }

    private boolean autenticar(String cpf, String senhaDigitada) {
        // Busca o admin no banco
        Administrador admin = adminDAO.buscarPorCpf(cpf);
        
        if (admin == null) {
            System.err.println("   ❌ Administrador não encontrado no banco de dados");
            System.err.println("   💡 Verifique se o CPF está correto e se existe um admin cadastrado");
            return false;
        }
        
        System.out.println("   ✅ Administrador encontrado:");
        System.out.println("      ID: " + admin.getIdAdministrador());
        System.out.println("      Nome: " + admin.getNome());
        System.out.println("      CPF: " + admin.getCpf());
        System.out.println("      Senha no banco (hash): " + (admin.getSenha() != null ? admin.getSenha().substring(0, Math.min(20, admin.getSenha().length())) + "..." : "NULL"));
        
        // Gera hash da senha digitada
        System.out.println("   Gerando hash da senha digitada...");
        String senhaHash = HashUtil.gerarHash(senhaDigitada);
        System.out.println("   Hash gerado: " + senhaHash.substring(0, Math.min(20, senhaHash.length())) + "...");
        
        // Compara os hashes
        System.out.println("   Comparando hashes...");
        boolean senhaCorreta = senhaHash.equals(admin.getSenha());
        System.out.println("   Resultado da comparação: " + (senhaCorreta ? "✅ CORRETO" : "❌ INCORRETO"));
        
        if (!senhaCorreta) {
            System.err.println("   ❌ Senha incorreta");
            System.err.println("   💡 Verifique se a senha está correta");
            System.err.println("   💡 Hash esperado: " + (admin.getSenha() != null ? admin.getSenha() : "NULL"));
            System.err.println("   💡 Hash recebido: " + senhaHash);
            return false;
        }
        
        this.adminAutenticado = admin;
        System.out.println("   ✅ Autenticação bem-sucedida!");
        return true;
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
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro de Navegação");
            alert.setHeaderText("Não foi possível carregar a tela.");
            alert.setContentText("Ocorreu um erro ao tentar abrir o arquivo da interface. Por favor, contate o suporte.");
            alert.showAndWait();
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

    private void toggleVisibilidadeSenha(MouseEvent event) {
        senhaEstaVisivel = !senhaEstaVisivel;

        if (senhaEstaVisivel) {
            campoSenha.setVisible(false);
            campoSenhaVisivel.setVisible(true);
            iconeOlho.setImage(iconeOlhoAberto);
        } else {
            campoSenhaVisivel.setVisible(false);
            campoSenha.setVisible(true);
            iconeOlho.setImage(iconeOlhoFechado);
        }
    }
    public Administrador getAdminAutenticado() {
        return adminAutenticado;
    }

    @FXML
    private void abrirConsultaPublica(javafx.event.ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/cpma/consultaApenadoView.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/mycompany/cpma/style.css").toExternalForm());
            stage.setTitle("Consulta de Dados - CPMA");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro");
            alert.setHeaderText("Não foi possível abrir a tela de consulta.");
            alert.setContentText("Ocorreu um erro ao tentar abrir a consulta pública. Por favor, contate o suporte.");
            alert.showAndWait();
        }
    }
}
