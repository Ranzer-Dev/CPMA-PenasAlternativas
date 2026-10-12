package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.ApiResponse;
import br.gov.sp.cpma.desktop.client.CadastroUsuarioDTO;
import br.gov.sp.cpma.desktop.client.CapturaCameraDTO;
import br.gov.sp.cpma.desktop.client.CpmaApiClient;
import br.gov.sp.cpma.desktop.client.UsuarioDTO;
import br.gov.sp.cpma.desktop.util.FormValidator;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class CadastroApenadoController implements Initializable {

    @FXML private Label lblTituloForm;
    @FXML private ImageView imgFotoApenado;
    @FXML private Label lblFotoPlaceholder;
    @FXML private Button btnTirarFoto;
    @FXML private Button btnCarregarFoto;
    @FXML private Button btnRemoverFoto;
    @FXML private Label lblFotoStatus;

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

    private Long idApenadoEmEdicao = null;
    private String fotoBase64 = null;
    private final CpmaApiClient apiClient = new CpmaApiClient();
    private final FormValidator formValidator = new FormValidator();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        formValidator.registerField("nome", txtNome, lblNomeError);
        formValidator.registerField("cpf", txtCpf, lblCpfError);
        formValidator.registerField("codigo", txtCodigo, lblCodigoError);

        lblFeedbackMessage.setVisible(false);
    }

    public void inicializarParaEdicao(UsuarioDTO usuario) {
        if (usuario == null) {
            return;
        }

        this.idApenadoEmEdicao = usuario.getIdUsuario();
        if (lblTituloForm != null) {
            lblTituloForm.setText("Atualizar Cadastro de Apenado (ID: " + usuario.getIdUsuario() + ")");
        }
        if (btnSalvar != null) {
            btnSalvar.setText("Atualizar Apenado");
        }

        if (usuario.getNome() != null) txtNome.setText(usuario.getNome());
        if (usuario.getCpf() != null) txtCpf.setText(usuario.getCpf());
        if (usuario.getCodigo() != null) txtCodigo.setText(usuario.getCodigo());
        if (usuario.getDataNascimento() != null) txtDataNascimento.setText(usuario.getDataNascimento().toString());
        if (usuario.getTelefone() != null) txtTelefone.setText(usuario.getTelefone());
        if (usuario.getEndereco() != null) txtEndereco.setText(usuario.getEndereco());
        if (usuario.getBairro() != null) txtBairro.setText(usuario.getBairro());
        if (usuario.getCidade() != null) txtCidade.setText(usuario.getCidade());
        if (usuario.getCep() != null) txtCep.setText(usuario.getCep());
        if (usuario.getUf() != null) txtUf.setText(usuario.getUf());
        if (usuario.getObservacao() != null) txtObservacao.setText(usuario.getObservacao());

        if (usuario.getFoto() != null && !usuario.getFoto().isBlank()) {
            try {
                String cleanB64 = usuario.getFoto();
                if (cleanB64.contains(",")) {
                    cleanB64 = cleanB64.substring(cleanB64.indexOf(",") + 1);
                }
                byte[] bytes = Base64.getDecoder().decode(cleanB64);
                Image image = new Image(new ByteArrayInputStream(bytes));
                imgFotoApenado.setImage(image);
                if (lblFotoPlaceholder != null) {
                    lblFotoPlaceholder.setVisible(false);
                }
                if (btnRemoverFoto != null) {
                    btnRemoverFoto.setVisible(true);
                }
                this.fotoBase64 = cleanB64;
                lblFotoStatus.setText("✓ Fotografia biométrica vinculada.");
                lblFotoStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #059669; -fx-font-weight: 700;");
            } catch (Exception ignored) {
            }
        }
    }

    @FXML
    public void handleTirarFotoCamera() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/br/gov/sp/cpma/desktop/view/cameraCaptureView.fxml"));
            Parent root = loader.load();
            CameraCaptureController controller = loader.getController();

            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("CPMA - Captura Biométrica Facial (Câmera ao Vivo)");
            dialog.setScene(new Scene(root, 650, 560));
            dialog.setMinWidth(640);
            dialog.setMinHeight(520);
            dialog.setResizable(true);
            dialog.sizeToScene();

            dialog.setOnCloseRequest(event -> controller.encerrarRecursos());

            controller.iniciarCamera(b64 -> {
                this.fotoBase64 = b64;
                try {
                    byte[] bytes = Base64.getDecoder().decode(b64);
                    imgFotoApenado.setImage(new Image(new ByteArrayInputStream(bytes)));
                    if (lblFotoPlaceholder != null) {
                        lblFotoPlaceholder.setVisible(false);
                    }
                    if (btnRemoverFoto != null) {
                        btnRemoverFoto.setVisible(true);
                    }
                    lblFotoStatus.setText("✓ Fotografia biométrica capturada pela câmera ao vivo!");
                    lblFotoStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #059669; -fx-font-weight: 700;");
                } catch (Exception e) {
                    exibirMensagemErro("Erro ao carregar imagem capturada: " + e.getMessage());
                }
            });

            Stage parentStage = (Stage) btnSalvar.getScene().getWindow();
            if (parentStage != null) {
                dialog.initOwner(parentStage);
            }

            dialog.showAndWait();
        } catch (Exception e) {
            exibirMensagemErro("Falha ao abrir visualizador da câmera: " + e.getMessage());
        }
    }

    @FXML
    public void handleSelecionarFoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Fotografia do Apenado");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens (JPG, PNG)", "*.jpg", "*.jpeg", "*.png")
        );

        Stage stage = (Stage) btnSalvar.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                if (bytes.length > 5 * 1024 * 1024) {
                    exibirMensagemErro("A foto selecionada e muito grande. O limite e de 5MB.");
                    return;
                }

                Image image = new Image(new ByteArrayInputStream(bytes));
                imgFotoApenado.setImage(image);
                if (lblFotoPlaceholder != null) {
                    lblFotoPlaceholder.setVisible(false);
                }

                this.fotoBase64 = Base64.getEncoder().encodeToString(bytes);
                btnRemoverFoto.setVisible(true);
                lblFotoStatus.setText("✓ Foto carregada (" + file.getName() + "). Biometria facial habilitada!");
                lblFotoStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #059669; -fx-font-weight: 700;");
            } catch (Exception e) {
                exibirMensagemErro("Falha ao carregar arquivo de imagem: " + e.getMessage());
            }
        }
    }

    @FXML
    public void handleRemoverFoto() {
        this.fotoBase64 = null;
        if (imgFotoApenado != null) {
            imgFotoApenado.setImage(null);
        }
        if (lblFotoPlaceholder != null) {
            lblFotoPlaceholder.setVisible(true);
        }
        if (btnRemoverFoto != null) {
            btnRemoverFoto.setVisible(false);
        }
        if (lblFotoStatus != null) {
            lblFotoStatus.setText("Nenhuma foto anexada (A biometria facial requer uma foto cadastrada)");
            lblFotoStatus.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b; -fx-font-style: italic;");
        }
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
        dto.setFoto(this.fotoBase64);
        dto.setAdminId(1L);

        if (idApenadoEmEdicao != null) {
            CompletableFuture.supplyAsync(() -> apiClient.atualizarUsuario(idApenadoEmEdicao, dto))
                    .thenAccept(this::processarResposta)
                    .exceptionally(ex -> {
                        Platform.runLater(() -> {
                            btnSalvar.setDisable(false);
                            exibirMensagemErro("Falha de conexao com o servidor: " + ex.getMessage());
                        });
                        return null;
                    });
        } else {
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
    }

    private void processarResposta(ApiResponse<UsuarioDTO> response) {
        Platform.runLater(() -> {
            btnSalvar.setDisable(false);

            if (response.isSuccess() && response.getData() != null) {
                if (idApenadoEmEdicao != null) {
                    exibirMensagemSucesso("Apenado atualizado com sucesso! ID: " + response.getData().getIdUsuario());
                } else {
                    exibirMensagemSucesso("Apenado cadastrado com sucesso! ID: " + response.getData().getIdUsuario());
                    limparFormulario();
                }
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
        handleRemoverFoto();
        formValidator.clearAllErrors();
    }
}
