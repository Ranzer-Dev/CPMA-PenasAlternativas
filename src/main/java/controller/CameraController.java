package controller;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.OpenCVFrameConverter;
import org.bytedeco.javacv.OpenCVFrameGrabber;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import util.ReconhecimentoFacial;

public class CameraController {

    @FXML
    private ImageView cameraImageView;
    @FXML
    private Button btnCapturarFoto;
    @FXML
    private Button btnCancelar;
    @FXML
    private Label lblStatusDetecao;

    private FrameGrabber camera;
    private ScheduledExecutorService timer;
    private Mat frameCapturado;
    private OpenCVFrameConverter.ToMat converterParaMat;
    private BufferedImage imagemCapturada; // Armazena a imagem final
    private ReconhecimentoFacial reconhecimentoFacial;
    private final boolean deteccaoAtiva = true;

    @FXML
    public void initialize() {
        camera = new OpenCVFrameGrabber(0);
        converterParaMat = new OpenCVFrameConverter.ToMat();

        // Inicializa o reconhecimento facial
        reconhecimentoFacial = new ReconhecimentoFacial();
        reconhecimentoFacial.inicializar();

        // Testa a detecção (apenas para debug)
        // reconhecimentoFacial.testarDetecao();
        // Atualiza o status inicial
        if (lblStatusDetecao != null) {
            if (reconhecimentoFacial.isInicializado()) {
                lblStatusDetecao.setText("Detecção facial ativa");
                lblStatusDetecao.setStyle("-fx-text-fill: green;");
            } else {
                lblStatusDetecao.setText("Detecção facial indisponível");
                lblStatusDetecao.setStyle("-fx-text-fill: red;");
            }
        }

        iniciarCamera();
    }

    private void iniciarCamera() {
        try {
            camera.start();
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
                                    if (lblStatusDetecao != null) {
                                        if (faceDetectada) {
                                            lblStatusDetecao.setText("Face detectada ✓");
                                            lblStatusDetecao.setStyle("-fx-text-fill: green;");
                                        } else {
                                            lblStatusDetecao.setText("Procurando faces...");
                                            lblStatusDetecao.setStyle("-fx-text-fill: orange;");
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
                            Platform.runLater(() -> cameraImageView.setImage(imageToShow));
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
            fecharJanela();
        }
    }

    @FXML
    void cancelar(ActionEvent event) {
        this.imagemCapturada = null; // Garante que nenhuma imagem seja retornada
        fecharJanela();
    }

    public BufferedImage getImagemCapturada() {
        return this.imagemCapturada;
    }

    public void fecharJanela() {
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
        // Fecha a janela
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
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

    private Image bufferedImageToImage(BufferedImage bufferedImage) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(bufferedImage, "png", baos);
            byte[] bytes = baos.toByteArray();
            return new Image(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            return null;
        }
    }
}
