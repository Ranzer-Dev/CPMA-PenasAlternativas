package controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import util.ReconhecimentoFacial;

public class CameraController {

    @FXML
    private ImageView cameraView;
    @FXML
    private Button btnIniciarCamera;
    @FXML
    private Button btnCapturar;
    @FXML
    private Button btnCancelar;
    @FXML
    private Label lblStatus;

    private FrameGrabber camera;
    private ScheduledExecutorService timer;
    private Mat frameCapturado;
    private OpenCVFrameConverter.ToMat converterParaMat;
    private BufferedImage imagemCapturada; // Armazena a imagem final
    private ReconhecimentoFacial reconhecimentoFacial;
    private final boolean deteccaoAtiva = true;
    private boolean cameraAtiva = false;

    @FXML
    public void initialize() {
        camera = new OpenCVFrameGrabber(0);
        converterParaMat = new OpenCVFrameConverter.ToMat();

        // Inicializa o reconhecimento facial
        reconhecimentoFacial = new ReconhecimentoFacial();
        reconhecimentoFacial.inicializar();

        // Configura modo rigoroso para reduzir falsos positivos
        reconhecimentoFacial.configurarModoRigoroso();

        // Testa a detecção (apenas para debug)
        // reconhecimentoFacial.testarDetecao();
        // Atualiza o status inicial
        if (lblStatus != null) {
            if (reconhecimentoFacial.isInicializado()) {
                lblStatus.setText("Detecção facial ativa - Clique em 'Iniciar Câmera'");
                lblStatus.setStyle("-fx-text-fill: green;");
            } else {
                lblStatus.setText("Detecção facial indisponível");
                lblStatus.setStyle("-fx-text-fill: red;");
            }
        }

        // Configura o listener para quando a janela for fechada
        configurarListenerFechamentoJanela();
    }

    @FXML
    private void iniciarCamera(ActionEvent event) {
        if (!cameraAtiva) {
            iniciarCameraInterno();
        } else {
            pararCamera();
        }
    }

    private void iniciarCameraInterno() {
        try {
            camera.start();
            cameraAtiva = true;

            // Mostra o botão de capturar e atualiza o status
            btnIniciarCamera.setText("Parar Câmera");
            btnCapturar.setVisible(true);
            if (lblStatus != null) {
                lblStatus.setText("Câmera ativa - Posicione-se na frente da câmera");
                lblStatus.setStyle("-fx-text-fill: green;");
            }

            Runnable frameGrabber = () -> {
                try {
                    Frame frame = camera.grab();
                    if (frame != null) {
                        frameCapturado = converterParaMat.convert(frame);

                        // Aplica detecção facial se estiver ativa
                        Image imageToShow;
                        if (deteccaoAtiva && reconhecimentoFacial.isInicializado()) {
                            BufferedImage bufferedImage = matToBufferedImage(frameCapturado);
                            if (bufferedImage != null) {
                                // System.out.println("Processando frame da câmera: " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight());

                                // Detecta faces e desenha retângulos
                                BufferedImage imagemComDetecao = reconhecimentoFacial.desenharRetangulosFaces(bufferedImage);
                                imageToShow = bufferedImageToImage(imagemComDetecao);

                                // Atualiza status da detecção
                                boolean faceDetectada = reconhecimentoFacial.detectarFace(bufferedImage);
                                Platform.runLater(() -> {
                                    if (lblStatus != null) {
                                        if (faceDetectada) {
                                            lblStatus.setText("Face detectada ✓");
                                            lblStatus.setStyle("-fx-text-fill: green;");
                                        } else {
                                            lblStatus.setText("Procurando faces...");
                                            lblStatus.setStyle("-fx-text-fill: orange;");
                                        }
                                    }
                                });
                            } else {
                                // System.out.println("Erro ao converter frame para BufferedImage");
                                imageToShow = matToImage(frameCapturado);
                            }
                        } else {
                            // if (!deteccaoAtiva) {
                            //     System.out.println("Detecção facial desabilitada");
                            // } else {
                            //     System.out.println("Reconhecimento facial não inicializado");
                            // }
                            imageToShow = matToImage(frameCapturado);
                        }

                        if (imageToShow != null) {
                            Platform.runLater(() -> cameraView.setImage(imageToShow));
                        }
                    }
                } catch (FrameGrabber.Exception e) {
                    e.printStackTrace();
                }
            };

            timer = Executors.newSingleThreadScheduledExecutor();
            timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);

        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            // Lidar com erro de câmera
        }
    }

    @FXML
    void capturarFoto(ActionEvent event) {
        if (frameCapturado != null && !frameCapturado.empty()) {
            this.imagemCapturada = matToBufferedImage(frameCapturado);

            // Para a câmera após capturar
            pararCamera();

            // Atualiza o status
            if (lblStatus != null) {
                lblStatus.setText("Foto capturada com sucesso!");
                lblStatus.setStyle("-fx-text-fill: green;");
            }

            // Fecha a janela após um pequeno delay para o usuário ver a confirmação
            javafx.concurrent.Task<Void> delayTask = new javafx.concurrent.Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    Thread.sleep(1000); // 1 segundo de delay
                    return null;
                }

                @Override
                protected void succeeded() {
                    fecharJanela();
                }
            };
            new Thread(delayTask).start();
        }
    }

    @FXML
    void cancelar(ActionEvent event) {
        this.imagemCapturada = null; // Garante que nenhuma imagem seja retornada
        limparRecursos(); // Limpa recursos antes de fechar
        fecharJanela();
    }

    public BufferedImage getImagemCapturada() {
        return this.imagemCapturada;
    }

    public Image getImagemCapturadaAsImage() {
        if (this.imagemCapturada != null) {
            return bufferedImageToImage(this.imagemCapturada);
        }
        return null;
    }

    /**
     * Configura o listener para quando a janela for fechada
     */
    private void configurarListenerFechamentoJanela() {
        // Aguarda a cena estar disponível
        Platform.runLater(() -> {
            if (cameraView != null && cameraView.getScene() != null) {
                Stage stage = (Stage) cameraView.getScene().getWindow();
                if (stage != null) {
                    // Listener para quando a janela for fechada
                    stage.setOnCloseRequest(event -> {
                        System.out.println("Janela da câmera sendo fechada - liberando recursos...");
                        limparRecursos();
                    });

                    // Listener para quando a janela for ocultada
                    stage.setOnHidden(event -> {
                        System.out.println("Janela da câmera ocultada - liberando recursos...");
                        limparRecursos();
                    });
                }
            }
        });
    }

    /**
     * Limpa todos os recursos da câmera
     */
    private void limparRecursos() {
        if (cameraAtiva) {
            System.out.println("Liberando recursos da câmera...");
            pararCamera();
        }
    }

    private void pararCamera() {
        cameraAtiva = false;
        btnIniciarCamera.setText("Iniciar Câmera");
        btnCapturar.setVisible(false);

        if (lblStatus != null) {
            lblStatus.setText("Câmera parada - Clique em 'Iniciar Câmera' para começar");
            lblStatus.setStyle("-fx-text-fill: orange;");
        }

        // Para o timer
        if (timer != null && !timer.isShutdown()) {
            try {
                timer.shutdown();
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Erro ao parar a captura de frames: " + e.getMessage());
            }
        }

        // Para a câmera
        if (camera != null) {
            try {
                camera.stop();
                camera.release();
            } catch (FrameGrabber.Exception e) {
                System.err.println("Erro ao parar a câmera: " + e.getMessage());
            }
        }
    }

    public void fecharJanela() {
        // Limpa todos os recursos antes de fechar
        limparRecursos();

        // Fecha a janela
        if (btnCancelar != null && btnCancelar.getScene() != null) {
            Stage stage = (Stage) btnCancelar.getScene().getWindow();
            stage.close();
        }
    }

    // MÉTODOS AUXILIARES DE CONVERSÃO
    private Image matToImage(Mat frame) {
        try (BytePointer bytePointer = new BytePointer()) {
            opencv_imgcodecs.imencode(".png", frame, bytePointer);
            byte[] bytes = new byte[(int) bytePointer.limit()];
            bytePointer.get(bytes);
            return new Image(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            return null;
        }
    }

    private BufferedImage matToBufferedImage(Mat frame) {
        try (BytePointer bytePointer = new BytePointer()) {
            opencv_imgcodecs.imencode(".png", frame, bytePointer);
            byte[] bytes = new byte[(int) bytePointer.limit()];
            bytePointer.get(bytes);
            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            return null;
        }
    }

    public Image bufferedImageToImage(BufferedImage bufferedImage) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "png", baos);
            byte[] bytes = baos.toByteArray();
            return new Image(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            return null;
        }
    }
}
