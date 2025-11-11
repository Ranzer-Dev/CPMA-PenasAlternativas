package controller;

import java.io.IOException;

import dao.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.Usuario;

public class BuscarCadastrarController {

    @FXML
    private Button btnEditarUsuario;
    @FXML
    private Button botaoSair;
    @FXML
    private Button btnEditarInst;
    @FXML
    private Button btnEditarPena;
    @FXML
    private Button btnEditarPonto;
    @FXML
    private TextField campoBuscarNomeCpf;
    @FXML
    private Button botaoBuscar;
    @FXML
    private Button btnInst, btnPena, btnPonto, btnUsuario;
    @FXML
    private Button botaoListar;

    @FXML
    private void initialize() {

        botaoBuscar.setOnAction(e -> {
            String termo = campoBuscarNomeCpf.getText().trim();

            if (termo.isEmpty()) {
                alerta("Aviso", "Por favor, digite um nome ou CPF para buscar.");
                return;
            }

            // Mostrar feedback visual
            botaoBuscar.setText("🔍 Buscando...");
            botaoBuscar.setDisable(true);

            Usuario u = buscarUsuario(termo);

            if (u != null) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass()
                            .getResource("/com/mycompany/cpma/detalheApenadoView.fxml"));
                    Parent root = loader.load();

                    DetalheApenadoController det = loader.getController();
                    det.setUsuario(u);

                    Stage st = new Stage();
                    st.setTitle("Detalhes do Apenado");
                    st.setScene(new Scene(root));
                    st.showAndWait();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    alerta("Erro", "Não foi possível abrir a tela de detalhes.");
                }
            } else {
                alerta("Aviso", "Usuário não encontrado. Verifique o nome ou CPF digitado.");
            }

            // Restaurar botão
            botaoBuscar.setText("🔍 Buscar");
            botaoBuscar.setDisable(false);
        });

        botaoSair.setOnAction(e -> voltarParaLogin());
        
        botaoListar.setOnAction(e -> abrir("/com/mycompany/cpma/listarApenadosView.fxml", "Lista de Apenados"));

        btnUsuario.setOnAction(e
                -> abrir("/com/mycompany/cpma/telaCadastroUsuario.fxml", "Cadastro de Usuário"));

        btnInst.setOnAction(e
                -> abrir("/com/mycompany/cpma/cadastroInstituicaoView.fxml", "Cadastro de Instituição"));

        btnPena.setOnAction(e
                -> abrir("/com/mycompany/cpma/cadastroPenaView.fxml", "Cadastro de Pena"));

        btnPonto.setOnAction(e
                -> abrir("/com/mycompany/cpma/cadastroRegistroDeTrabalhoView.fxml", "Cadastro de Ponto"));

        btnEditarUsuario.setOnAction(e
                -> abrir("/com/mycompany/cpma/listarApenadosView.fxml", "Lista de Apenados"));

        btnEditarInst.setOnAction(e
                -> abrir("/com/mycompany/cpma/listarInstituicaoView.fxml", "Lista de Instituições"));

        btnEditarPena.setOnAction(e
                -> abrir("/com/mycompany/cpma/cadastroPenaView.fxml", "Editar Pena", true));

        btnEditarPonto.setOnAction(e
                -> abrir("/com/mycompany/cpma/cadastroRegistroDeTrabalhoView.fxml", "Editar Ponto", true));

    }

    private void abrir(String fxml, String titulo) {
        abrir(fxml, titulo, false);
    }

    private void abrir(String fxml, String titulo, boolean modoEdicao) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();

            if (modoEdicao && fxml.contains("telaCadastroUsuario")) {
                CadastrarUsuarioController c = loader.getController();
                c.ativarModoEdicao();
            }

            if (modoEdicao && fxml.contains("cadastroInstituicaoView")) {
                CadastrarInstituicaoController c = loader.getController();
                c.ativarModoEdicao();
            }

            if (modoEdicao && fxml.contains("cadastroPenaView")) {
                CadastrarPenaController c = loader.getController();
                c.ativarModoEdicao();
            }

            if (modoEdicao && fxml.contains("cadastroRegistroDeTrabalhoView")) {
                // CadastroRegistroDeTrabalhoController c = loader.getController();
                // c.ativarModoEdicao();
            }

            Stage st = new Stage();
            st.setTitle(titulo);
            st.setScene(new Scene(root));
            st.show();
        } catch (Exception ex) {
            ex.printStackTrace();
            alerta("Erro", "Não foi possível abrir a janela: " + titulo);
        }
    }

    private void alerta(String titulo, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private Usuario buscarUsuario(String termo) {
        if (termo == null || termo.isEmpty()) {
            return null;
        }

        UsuarioDAO dao = new UsuarioDAO();

        // Primeiro tenta buscar por CPF
        Usuario usuario = dao.buscarPorCpf(termo);

        // Se não encontrou por CPF, tenta buscar por nome
        if (usuario == null) {
            usuario = UsuarioDAO.buscarPorNome(termo);
        }

        return usuario;
    }

    private void voltarParaLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/cpma/login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/com/mycompany/cpma/style.css").toExternalForm());

            Stage stage = (Stage) botaoSair.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Login");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
