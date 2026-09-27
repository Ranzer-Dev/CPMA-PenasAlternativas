package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.*;
import br.gov.sp.cpma.desktop.util.FormValidator;
import br.gov.sp.cpma.desktop.util.RelatorioImpressaoService;
import br.gov.sp.cpma.desktop.util.SessaoAdmin;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class TotemKioskController implements Initializable {

    private static String terminalToken = "";
    private static final String DEFAULT_TERMINAL_ID = "TOTEM_EXTERNO_01";

    @FXML private Label lblTerminalInfo;
    @FXML private Label lblStatusToken;
    @FXML private Button btnConfigurarToken;
    @FXML private TabPane tabTotem;

    @FXML private ImageView imgCameraPreview;
    @FXML private Label lblCameraPlaceholder;
    @FXML private Button btnCarregarFoto;
    @FXML private Button btnFotoExemplo;
    @FXML private Label lblBiometriaFeedback;
    @FXML private Button btnIdentificarFacial;

    @FXML private TextField txtCodigoAcesso;
    @FXML private Button btnConfirmarCodigo;

    @FXML private VBox cardResultado;
    @FXML private Label lblResultadoTitulo;
    @FXML private Label lblResultadoDetalhe;

    @FXML private VBox boxResumoTotem;
    @FXML private Label lblTotemHorasCumpridas;
    @FXML private Label lblTotemHorasRestantes;
    @FXML private Label lblTotemHorasTotais;
    @FXML private Label lblTotemPercentual;
    @FXML private HBox boxAcoesTotem;
    @FXML private Button btnImprimirComprovante;

    private final CpmaApiClient apiClient = new CpmaApiClient();
    private String fotoCapturadaBase64;
    private Long usuarioLogadoId;
    private Long penaLogadaId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cardResultado.setVisible(false);
        cardResultado.setManaged(false);
        if (boxResumoTotem != null) {
            boxResumoTotem.setVisible(false);
            boxResumoTotem.setManaged(false);
        }
        if (boxAcoesTotem != null) {
            boxAcoesTotem.setVisible(false);
            boxAcoesTotem.setManaged(false);
        }

        lblTerminalInfo.setText("Terminal: " + DEFAULT_TERMINAL_ID);
        atualizarIndicadorToken();

        if ((terminalToken == null || terminalToken.isBlank()) && SessaoAdmin.getAdminLogado() != null) {
            obterTokenAutomatico();
        }
    }

    private void atualizarIndicadorToken() {
        if (terminalToken != null && !terminalToken.isBlank()) {
            lblStatusToken.setText("● Terminal Autorizado");
            lblStatusToken.setStyle("-fx-background-color: rgba(16, 185, 129, 0.15); -fx-text-fill: #059669; -fx-font-weight: 700; -fx-padding: 4px 10px; -fx-background-radius: 12px;");
        } else {
            lblStatusToken.setText("⚠ Token Ausente");
            lblStatusToken.setStyle("-fx-background-color: rgba(239, 68, 68, 0.15); -fx-text-fill: #dc2626; -fx-font-weight: 700; -fx-padding: 4px 10px; -fx-background-radius: 12px;");
        }
    }

    private void obterTokenAutomatico() {
        LoginResponseDTO admin = SessaoAdmin.getAdminLogado();
        if (admin == null || admin.getAdminId() == null) {
            return;
        }

        new Thread(() -> {
            ApiResponse<TokenTotemDTO> resp = apiClient.gerarTokenTotem(DEFAULT_TERMINAL_ID, admin.getAdminId());
            if (resp.isSuccess() && resp.getData() != null && resp.getData().getToken() != null) {
                terminalToken = resp.getData().getToken();
                Platform.runLater(this::atualizarIndicadorToken);
            }
        }).start();
    }

    @FXML
    public void handleConfigurarToken() {
        TextInputDialog dialog = new TextInputDialog(terminalToken);
        dialog.setTitle("Token de Acesso do Terminal");
        dialog.setHeaderText("Configurar Token de Autorizacao do Totem");
        dialog.setContentText("Informe o JWT Token do Terminal:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(novoToken -> {
            terminalToken = novoToken.trim();
            atualizarIndicadorToken();
        });
    }

    @FXML
    public void handleCarregarFoto() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Imagem da Face do Apenado");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens (JPG, PNG)", "*.jpg", "*.jpeg", "*.png")
        );

        File file = fileChooser.showOpenDialog(btnCarregarFoto.getScene().getWindow());
        if (file != null) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                fotoCapturadaBase64 = Base64.getEncoder().encodeToString(bytes);
                Image img = new Image(new ByteArrayInputStream(bytes));
                imgCameraPreview.setImage(img);
                lblCameraPlaceholder.setVisible(false);
                lblBiometriaFeedback.setVisible(false);
            } catch (Exception e) {
                lblBiometriaFeedback.setText("Falha ao carregar imagem: " + e.getMessage());
                lblBiometriaFeedback.getStyleClass().setAll("banner-error");
                lblBiometriaFeedback.setVisible(true);
            }
        }
    }

    @FXML
    public void handleCarregarFotoExemplo() {
        byte[] minimalPng = Base64.getDecoder().decode("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkWPjfDwAEeQHzrX7sugAAAABJRU5ErkJggg==");
        fotoCapturadaBase64 = Base64.getEncoder().encodeToString(minimalPng);
        Image img = new Image(new ByteArrayInputStream(minimalPng));
        imgCameraPreview.setImage(img);
        lblCameraPlaceholder.setVisible(false);
        lblBiometriaFeedback.setText("Foto padrao carregada para simulacao de captura.");
        lblBiometriaFeedback.getStyleClass().setAll("banner-success");
        lblBiometriaFeedback.setVisible(true);
    }

    @FXML
    public void handleIdentificarFacial() {
        if (terminalToken == null || terminalToken.isBlank()) {
            lblBiometriaFeedback.setText("Token de autorizacao do terminal nao configurado. Clique em 'Token de Acesso' para informar.");
            lblBiometriaFeedback.getStyleClass().setAll("banner-error");
            lblBiometriaFeedback.setVisible(true);
            return;
        }

        if (fotoCapturadaBase64 == null || fotoCapturadaBase64.isBlank()) {
            lblBiometriaFeedback.setText("Nenhuma foto capturada ou selecionada. Selecione a imagem da face.");
            lblBiometriaFeedback.getStyleClass().setAll("banner-error");
            lblBiometriaFeedback.setVisible(true);
            return;
        }

        btnIdentificarFacial.setDisable(true);
        btnIdentificarFacial.setText("Processando biometria facial...");
        lblBiometriaFeedback.setVisible(false);

        CompletableFuture.supplyAsync(() -> apiClient.reconhecerFacial(fotoCapturadaBase64, terminalToken, 0.65))
                .thenAccept(this::processarRespostaFacial)
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btnIdentificarFacial.setDisable(false);
                        btnIdentificarFacial.setText("🔍 Confirmar Presenca por Reconhecimento Facial");
                        exibirResultado(false, "FALHA DE REDE", "Nao foi possivel conectar com o servico biometrico: " + ex.getMessage());
                    });
                    return null;
                });
    }

    private void processarRespostaFacial(ApiResponse<ReconhecimentoFacialDTO> response) {
        Platform.runLater(() -> {
            btnIdentificarFacial.setDisable(false);
            btnIdentificarFacial.setText("🔍 Confirmar Presenca por Reconhecimento Facial");

            if (response.isSuccess() && response.getData() != null && response.getData().isSucesso()) {
                ReconhecimentoFacialDTO data = response.getData();
                this.usuarioLogadoId = data.getUsuarioId();
                String confianca = data.getConfidence() != null ? String.format("%.1f%%", data.getConfidence() * 100) : "100%";
                exibirResultado(true, "ACESSO CONFIRMADO (BIOMETRIA FACIAL)",
                        "Apenado: " + data.getUsuarioNome()
                                + "\nMatricula: " + (data.getUsuarioCodigo() != null ? data.getUsuarioCodigo() : "-")
                                + "\nConfiabilidade Biometrica: " + confianca);
                carregarResumoPenaTotem(data.getUsuarioId());
            } else {
                String detalhe = FormValidator.formatarMensagemErro(response.getError(), response.getStatusCode());
                exibirResultado(false, "BIOMETRIA NAO RECONHECIDA", detalhe);
            }
        });
    }

    @FXML
    public void handleValidarCodigo() {
        String codigo = txtCodigoAcesso.getText().trim();
        if (codigo.isEmpty()) {
            exibirResultado(false, "CODIGO OBRIGATORIO", "Por favor, digite o codigo numerico de 6 digitos.");
            return;
        }

        btnConfirmarCodigo.setDisable(true);

        CompletableFuture.supplyAsync(() -> apiClient.validarCodigoAcesso(codigo, DEFAULT_TERMINAL_ID))
                .thenAccept(this::processarRespostaCodigo)
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btnConfirmarCodigo.setDisable(false);
                        exibirResultado(false, "FALHA DE REDE", "Nao foi possivel conectar com o servidor: " + ex.getMessage());
                    });
                    return null;
                });
    }

    private void processarRespostaCodigo(ApiResponse<ValidarAcessoDTO> response) {
        Platform.runLater(() -> {
            btnConfirmarCodigo.setDisable(false);

            if (response.isSuccess() && response.getData() != null && response.getData().isSucesso()) {
                ValidarAcessoDTO data = response.getData();
                this.usuarioLogadoId = data.getUsuarioId();
                exibirResultado(true, "ACESSO CONFIRMADO (CODIGO DE ACESSO)",
                        "Bem-vindo(a), " + data.getUsuarioNome() + "!\nMatricula: " + (data.getUsuarioCodigo() != null ? data.getUsuarioCodigo() : "-"));
                txtCodigoAcesso.clear();
                carregarResumoPenaTotem(data.getUsuarioId());
            } else {
                String detalhe = FormValidator.formatarMensagemErro(response.getError(), response.getStatusCode());
                exibirResultado(false, "ACESSO NEGADO", detalhe);
            }
        });
    }

    private void carregarResumoPenaTotem(Long usuarioId) {
        new Thread(() -> {
            ApiResponse<List<PenaDTO>> respPenas = apiClient.listarPenasPorUsuario(usuarioId);
            if (respPenas.isSuccess() && respPenas.getData() != null && !respPenas.getData().isEmpty()) {
                PenaDTO pena = respPenas.getData().get(0);
                this.penaLogadaId = pena.getIdPena();
                ApiResponse<ResumoCumprimentoDTO> respResumo = apiClient.obterResumoCumprimento(pena.getIdPena());
                Platform.runLater(() -> {
                    if (respResumo.isSuccess() && respResumo.getData() != null) {
                        ResumoCumprimentoDTO r = respResumo.getData();
                        lblTotemHorasCumpridas.setText("Cumpridas: " + String.format("%.1fh", r.getHorasCumpridas()));
                        lblTotemHorasRestantes.setText("Restantes: " + String.format("%.1fh", r.getHorasRestantes()));
                        lblTotemHorasTotais.setText("Total Pena: " + r.getHorasTotais() + "h");
                        lblTotemPercentual.setText("Progresso: " + String.format("%.1f%%", r.getPercentualConcluido()));
                    } else {
                        lblTotemHorasCumpridas.setText("Cumpridas: 0h");
                        lblTotemHorasRestantes.setText("Restantes: " + pena.getHorasTotais() + "h");
                        lblTotemHorasTotais.setText("Total Pena: " + pena.getHorasTotais() + "h");
                        lblTotemPercentual.setText("Progresso: 0%");
                    }
                    if (boxResumoTotem != null) {
                        boxResumoTotem.setVisible(true);
                        boxResumoTotem.setManaged(true);
                    }
                    if (boxAcoesTotem != null) {
                        boxAcoesTotem.setVisible(true);
                        boxAcoesTotem.setManaged(true);
                    }
                });
            } else {
                Platform.runLater(() -> {
                    if (boxAcoesTotem != null) {
                        boxAcoesTotem.setVisible(true);
                        boxAcoesTotem.setManaged(true);
                    }
                });
            }
        }).start();
    }

    @FXML
    public void handleImprimirComprovante() {
        if (usuarioLogadoId == null) {
            return;
        }

        new Thread(() -> {
            ApiResponse<UsuarioDTO> respUser = apiClient.buscarUsuarioPorId(usuarioLogadoId);
            if (respUser.isSuccess() && respUser.getData() != null) {
                Platform.runLater(() -> {
                    RelatorioImpressaoService.imprimirFichaCompleta(
                            btnConfirmarCodigo.getScene().getWindow(),
                            apiClient,
                            respUser.getData(),
                            penaLogadaId
                    );
                });
            }
        }).start();
    }

    @FXML
    public void handleNovoAcesso() {
        usuarioLogadoId = null;
        penaLogadaId = null;
        fotoCapturadaBase64 = null;
        imgCameraPreview.setImage(null);
        lblCameraPlaceholder.setVisible(true);
        lblBiometriaFeedback.setVisible(false);
        txtCodigoAcesso.clear();
        cardResultado.setVisible(false);
        cardResultado.setManaged(false);
        if (boxResumoTotem != null) {
            boxResumoTotem.setVisible(false);
            boxResumoTotem.setManaged(false);
        }
        if (boxAcoesTotem != null) {
            boxAcoesTotem.setVisible(false);
            boxAcoesTotem.setManaged(false);
        }
        btnConfirmarCodigo.setDisable(false);
        btnIdentificarFacial.setDisable(false);
    }

    private void exibirResultado(boolean sucesso, String titulo, String detalhe) {
        cardResultado.setVisible(true);
        cardResultado.setManaged(true);
        lblResultadoTitulo.setText(titulo);
        lblResultadoDetalhe.setText(detalhe);

        if (sucesso) {
            cardResultado.setStyle("-fx-background-color: rgba(34, 197, 94, 0.15); -fx-border-color: #22c55e; -fx-border-radius: 8px; -fx-padding: 16px;");
            lblResultadoTitulo.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 16px; -fx-font-weight: 800;");
        } else {
            cardResultado.setStyle("-fx-background-color: rgba(239, 68, 68, 0.15); -fx-border-color: #ef4444; -fx-border-radius: 8px; -fx-padding: 16px;");
            lblResultadoTitulo.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 16px; -fx-font-weight: 800;");
            if (boxResumoTotem != null) {
                boxResumoTotem.setVisible(false);
                boxResumoTotem.setManaged(false);
            }
            if (boxAcoesTotem != null) {
                boxAcoesTotem.setVisible(false);
                boxAcoesTotem.setManaged(false);
            }
        }
    }
}
