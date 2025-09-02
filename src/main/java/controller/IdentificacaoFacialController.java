package controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Optional;

import dao.DadosFaciaisDAO;
import dao.UsuarioDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.DadosFaciais;
import model.Usuario;
import util.ReconhecimentoFacial;

public class IdentificacaoFacialController {

    @FXML
    private ImageView imageView;
    @FXML
    private Button btnCapturar;
    @FXML
    private Button btnCarregar;
    @FXML
    private Button btnIdentificar;
    @FXML
    private Button btnCadastrar;
    @FXML
    private Label lblStatus;
    @FXML
    private Label lblUsuario;
    @FXML
    private Label lblCPF;
    @FXML
    private Label lblEndereco;

    private ReconhecimentoFacial reconhecimentoFacial;
    private DadosFaciaisDAO dadosFaciaisDAO;
    private UsuarioDAO usuarioDAO;
    private BufferedImage imagemAtual;
    private Usuario usuarioIdentificado;

    @FXML
    private void initialize() {
        reconhecimentoFacial = new ReconhecimentoFacial();
        dadosFaciaisDAO = new DadosFaciaisDAO();
        usuarioDAO = new UsuarioDAO();

        configurarBotoes();
        limparInterface();
    }

    private void configurarBotoes() {
        btnCapturar.setOnAction(e -> capturarImagem());
        btnCarregar.setOnAction(e -> carregarImagem());
        btnIdentificar.setOnAction(e -> identificarUsuario());
        btnCadastrar.setOnAction(e -> cadastrarDadosFaciais());
    }

    /**
     * Captura imagem da webcam
     */
    private void capturarImagem() {
        try {
            imagemAtual = reconhecimentoFacial.capturarImagem();

            if (imagemAtual != null) {
                exibirImagem(imagemAtual);
                lblStatus.setText("Imagem capturada com sucesso!");
                btnIdentificar.setDisable(false);
                btnCadastrar.setDisable(false);
            } else {
                lblStatus.setText("Erro ao capturar imagem. Verifique se a webcam está conectada.");
                btnIdentificar.setDisable(true);
                btnCadastrar.setDisable(true);
            }

        } catch (Exception e) {
            mostrarErro("Erro ao capturar imagem", e.getMessage());
        }
    }

    /**
     * Carrega imagem de arquivo
     */
    private void carregarImagem() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Imagem");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.jpeg", "*.png", "*.bmp")
        );

        File arquivo = fileChooser.showOpenDialog(imageView.getScene().getWindow());

        if (arquivo != null) {
            try {
                imagemAtual = javax.imageio.ImageIO.read(arquivo);
                exibirImagem(imagemAtual);
                lblStatus.setText("Imagem carregada com sucesso!");
                btnIdentificar.setDisable(false);
                btnCadastrar.setDisable(false);

            } catch (IOException e) {
                mostrarErro("Erro ao carregar imagem", e.getMessage());
            }
        }
    }

    /**
     * Identifica usuário pela imagem facial
     */
    private void identificarUsuario() {
        if (imagemAtual == null) {
            mostrarErro("Erro", "Nenhuma imagem selecionada");
            return;
        }

        try {
            // Extrai descritores faciais da imagem
            String descritores = reconhecimentoFacial.extrairDescritoresFaciais(imagemAtual);

            // Busca usuário por similaridade facial
            usuarioIdentificado = dadosFaciaisDAO.buscarPorSimilaridadeFacial(descritores, 0.7);

            if (usuarioIdentificado != null) {
                exibirDadosUsuario(usuarioIdentificado);
                lblStatus.setText("Usuário identificado com sucesso!");
                btnCadastrar.setDisable(true); // Usuário já cadastrado
            } else {
                lblStatus.setText("Usuário não encontrado. Considere cadastrar novos dados faciais.");
                limparDadosUsuario();
                btnCadastrar.setDisable(false);
            }

        } catch (Exception e) {
            mostrarErro("Erro na identificação", e.getMessage());
        }
    }

    /**
     * Cadastra novos dados faciais para um usuário
     */
    private void cadastrarDadosFaciais() {
        if (imagemAtual == null) {
            mostrarErro("Erro", "Nenhuma imagem selecionada");
            return;
        }

        // Solicita CPF do usuário para cadastro
        String cpf = solicitarCPF();
        if (cpf == null || cpf.trim().isEmpty()) {
            return;
        }

        try {
            // Busca usuário pelo CPF
            Usuario usuario = usuarioDAO.buscarPorCpf(cpf);

            if (usuario == null) {
                mostrarErro("Usuário não encontrado", "CPF não cadastrado no sistema");
                return;
            }

            // Verifica se já existem dados faciais para este usuário
            DadosFaciais dadosExistentes = dadosFaciaisDAO.buscarPorUsuario(usuario.getIdUsuario());

            if (dadosExistentes != null) {
                // Pergunta se deseja atualizar
                boolean atualizar = confirmarAtualizacao();
                if (atualizar) {
                    atualizarDadosFaciais(dadosExistentes, imagemAtual);
                }
            } else {
                // Cadastra novos dados
                cadastrarNovosDadosFaciais(usuario, imagemAtual);
            }

        } catch (Exception e) {
            mostrarErro("Erro ao cadastrar dados faciais", e.getMessage());
        }
    }

    /**
     * Cadastra novos dados faciais
     */
    private void cadastrarNovosDadosFaciais(Usuario usuario, BufferedImage imagem) throws SQLException {
        // Extrai descritores faciais
        String descritores = reconhecimentoFacial.extrairDescritoresFaciais(imagem);

        // Cria objeto DadosFaciais
        DadosFaciais dadosFaciais = new DadosFaciais();
        dadosFaciais.setFkUsuarioIdUsuario(usuario.getIdUsuario());
        dadosFaciais.setDescritoresFaciais(descritores);

        // Salva no banco
        if (dadosFaciaisDAO.cadastrar(dadosFaciais)) {
            mostrarSucesso("Dados faciais cadastrados com sucesso!");
            usuarioIdentificado = usuario;
            exibirDadosUsuario(usuario);
        } else {
            mostrarErro("Erro", "Falha ao cadastrar dados faciais");
        }
    }

    /**
     * Atualiza dados faciais existentes
     */
    private void atualizarDadosFaciais(DadosFaciais dadosExistentes, BufferedImage imagem) throws SQLException {
        // Extrai novos descritores faciais
        String novosDescritores = reconhecimentoFacial.extrairDescritoresFaciais(imagem);

        // Atualiza o objeto
        dadosExistentes.setDescritoresFaciais(novosDescritores);

        // Salva no banco
        if (dadosFaciaisDAO.atualizar(dadosExistentes)) {
            mostrarSucesso("Dados faciais atualizados com sucesso!");
        } else {
            mostrarErro("Erro", "Falha ao atualizar dados faciais");
        }
    }

    /**
     * Exibe imagem na interface
     */
    private void exibirImagem(BufferedImage imagem) {
        try {
            // Converte BufferedImage para arquivo temporário
            File tempFile = File.createTempFile("temp_image", ".jpg");
            javax.imageio.ImageIO.write(imagem, "jpg", tempFile);

            // Carrega no ImageView
            Image image = new Image(tempFile.toURI().toString());
            imageView.setImage(image);

            // Remove arquivo temporário
            tempFile.delete();

        } catch (IOException e) {
            mostrarErro("Erro ao exibir imagem", e.getMessage());
        }
    }

    /**
     * Exibe dados do usuário identificado
     */
    private void exibirDadosUsuario(Usuario usuario) {
        lblUsuario.setText("Nome: " + usuario.getNome());
        lblCPF.setText("CPF: " + usuario.getCpf());
        lblEndereco.setText("Endereço: " + usuario.getEndereco() + ", " + usuario.getCidade() + "/" + usuario.getUf());
    }

    /**
     * Limpa dados do usuário
     */
    private void limparDadosUsuario() {
        lblUsuario.setText("Nome: ");
        lblCPF.setText("CPF: ");
        lblEndereco.setText("Endereço: ");
    }

    /**
     * Limpa interface
     */
    private void limparInterface() {
        imageView.setImage(null);
        limparDadosUsuario();
        lblStatus.setText("Selecione ou capture uma imagem para começar");
        btnIdentificar.setDisable(true);
        btnCadastrar.setDisable(true);
        imagemAtual = null;
        usuarioIdentificado = null;
    }

    /**
     * Solicita CPF do usuário
     */
    private String solicitarCPF() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Cadastro de Dados Faciais");
        dialog.setHeaderText("Informe o CPF do usuário");
        dialog.setContentText("CPF:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    /**
     * Confirma atualização de dados existentes
     */
    private boolean confirmarAtualizacao() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Dados Existentes");
        alert.setHeaderText("Usuário já possui dados faciais cadastrados");
        alert.setContentText("Deseja atualizar os dados faciais existentes?");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Mostra mensagem de erro
     */
    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    /**
     * Mostra mensagem de sucesso
     */
    private void mostrarSucesso(String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sucesso");
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    /**
     * Navega para outra tela
     */
    private void navegarPara(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
            Parent root = loader.load();
            Stage stage = (Stage) imageView.getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            mostrarErro("Erro de Navegação", e.getMessage());
        }
    }
}
