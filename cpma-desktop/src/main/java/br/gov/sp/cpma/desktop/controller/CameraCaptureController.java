package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.ApiResponse;
import br.gov.sp.cpma.desktop.client.CapturaCameraDTO;
import br.gov.sp.cpma.desktop.client.CpmaApiClient;
import br.gov.sp.cpma.desktop.util.FormValidator;
import br.gov.sp.cpma.desktop.util.MjpegStreamReader;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class CameraCaptureController {

    @FXML private Label lblBadgeConexao;
    @FXML private ImageView imgCameraLive;
    @FXML private VBox paneCarregando;
    @FXML private Label lblOverlayStatus;
    @FXML private Label lblFeedbackMensagem;
    @FXML private Button btnCancelar;
    @FXML private Button btnTirarNovamente;
    @FXML private Button btnCapturar;
    @FXML private Button btnConfirmarFoto;

    private final CpmaApiClient apiClient = new CpmaApiClient();
    private MjpegStreamReader streamReader;
    private Thread streamThread;
    private String fotoCapturadaBase64;
    private Consumer<String> onFotoConfirmada;

    public void iniciarCamera(Consumer<String> onFotoConfirmada) {
        this.onFotoConfirmada = onFotoConfirmada;
        iniciarStreamAoVivo();
    }

    private void iniciarStreamAoVivo() {
        pararStream();
        paneCarregando.setVisible(true);
        paneCarregando.setManaged(true);
        lblOverlayStatus.setText("Iniciando sensor da câmera...");
        lblBadgeConexao.setText("● Conectando...");
        lblBadgeConexao.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-border-color: #fde68a; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 3px 10px;");
        lblFeedbackMensagem.setVisible(false);
        lblFeedbackMensagem.setManaged(false);

        String streamUrl = apiClient.getFacialServiceUrl() + "/camera/stream";
        this.streamReader = new MjpegStreamReader(
                streamUrl,
                this::processarFrameRecebido,
                this::processarErroStream
        );

        this.streamThread = new Thread(streamReader, "CameraStreamThread");
        this.streamThread.setDaemon(true);
        this.streamThread.start();
    }

    private void processarFrameRecebido(Image frame) {
        if (paneCarregando.isVisible()) {
            paneCarregando.setVisible(false);
            paneCarregando.setManaged(false);
            lblBadgeConexao.setText("● Transmissão Ativa | Rosto Rastreado");
            lblBadgeConexao.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-background-color: #ecfdf5; -fx-text-fill: #059669; -fx-border-color: #a7f3d0; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 3px 10px;");
        }
        imgCameraLive.setImage(frame);
    }

    private void processarErroStream(Throwable erro) {
        paneCarregando.setVisible(true);
        paneCarregando.setManaged(true);
        lblOverlayStatus.setText("Não foi possível conectar à câmera local.");
        lblBadgeConexao.setText("● Câmera Indisponível");
        lblBadgeConexao.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-border-color: #fca5a5; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 3px 10px;");
        exibirFeedbackErro("Verifique se o serviço biométrico (porta 8001) e a webcam estão ativos.");
    }

    @FXML
    public void handleBaterFoto() {
        btnCapturar.setDisable(true);
        btnCapturar.setText("Capturando...");

        CompletableFuture.supplyAsync(apiClient::capturarFotoWebcam)
                .thenAccept(this::processarRespostaCaptura)
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btnCapturar.setDisable(false);
                        btnCapturar.setText("📸 Bater Foto");
                        exibirFeedbackErro("Falha ao capturar imagem da câmera: " + ex.getMessage());
                    });
                    return null;
                });
    }

    private void processarRespostaCaptura(ApiResponse<CapturaCameraDTO> response) {
        Platform.runLater(() -> {
            btnCapturar.setDisable(false);
            btnCapturar.setText("📸 Bater Foto");

            if (!response.isSuccess() || response.getData() == null || response.getData().getRawBase64() == null) {
                String msg = FormValidator.formatarMensagemErro(response.getError(), response.getStatusCode());
                exibirFeedbackErro("Erro na captura: " + msg);
                return;
            }

            pararStream();

            CapturaCameraDTO data = response.getData();
            this.fotoCapturadaBase64 = data.getRawBase64();

            byte[] bytes = Base64.getDecoder().decode(this.fotoCapturadaBase64);
            imgCameraLive.setImage(new Image(new ByteArrayInputStream(bytes)));

            btnCapturar.setVisible(false);
            btnCapturar.setManaged(false);
            btnTirarNovamente.setVisible(true);
            btnTirarNovamente.setManaged(true);
            btnConfirmarFoto.setVisible(true);
            btnConfirmarFoto.setManaged(true);

            if (data.isFaceDetected()) {
                exibirFeedbackSucesso("✓ Foto capturada com sucesso (" + data.getCount() + " rosto(s) detectado(s)). Clique em 'Usar Esta Foto'.");
            } else {
                exibirFeedbackAlerta("⚠ Foto capturada, porém o enquadramento facial está incerto. Recomenda-se tirar novamente.");
            }
        });
    }

    @FXML
    public void handleTirarNovamente() {
        this.fotoCapturadaBase64 = null;
        btnTirarNovamente.setVisible(false);
        btnTirarNovamente.setManaged(false);
        btnConfirmarFoto.setVisible(false);
        btnConfirmarFoto.setManaged(false);
        btnCapturar.setVisible(true);
        btnCapturar.setManaged(true);

        iniciarStreamAoVivo();
    }

    @FXML
    public void handleConfirmarFoto() {
        pararStream();
        if (onFotoConfirmada != null && fotoCapturadaBase64 != null) {
            onFotoConfirmada.accept(fotoCapturadaBase64);
        }
        fecharJanela();
    }

    @FXML
    public void handleCancelar() {
        pararStream();
        fecharJanela();
    }

    public void encerrarRecursos() {
        pararStream();
    }

    private void pararStream() {
        if (streamReader != null) {
            streamReader.stop();
            streamReader = null;
        }
        if (streamThread != null) {
            streamThread.interrupt();
            streamThread = null;
        }
    }

    private void fecharJanela() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void exibirFeedbackSucesso(String msg) {
        lblFeedbackMensagem.setText(msg);
        lblFeedbackMensagem.setStyle("-fx-text-fill: #059669; -fx-font-weight: 700;");
        lblFeedbackMensagem.setVisible(true);
        lblFeedbackMensagem.setManaged(true);
    }

    private void exibirFeedbackAlerta(String msg) {
        lblFeedbackMensagem.setText(msg);
        lblFeedbackMensagem.setStyle("-fx-text-fill: #d97706; -fx-font-weight: 700;");
        lblFeedbackMensagem.setVisible(true);
        lblFeedbackMensagem.setManaged(true);
    }

    private void exibirFeedbackErro(String msg) {
        lblFeedbackMensagem.setText(msg);
        lblFeedbackMensagem.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: 700;");
        lblFeedbackMensagem.setVisible(true);
        lblFeedbackMensagem.setManaged(true);
    }
}
