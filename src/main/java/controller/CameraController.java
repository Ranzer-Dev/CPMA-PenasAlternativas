package controller;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
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
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
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
    @FXML
    private Ellipse guiaFace;
    @FXML
    private Label lblOrientacao;

    private FrameGrabber camera;
    private ScheduledExecutorService timer;
    private Mat frameCapturado;
    private OpenCVFrameConverter.ToMat converterParaMat;
    private BufferedImage imagemCapturada; // Armazena a imagem final
    private ReconhecimentoFacial reconhecimentoFacial;
    private final boolean deteccaoAtiva = true;
    private boolean cameraAtiva = false;
    private int frameCounter = 0; // Contador para otimização
    private static final int SKIP_FRAMES = 1; // Processar detecção a cada 2 frames
    private boolean ultimaFaceDetectada = false; // Para manter estado entre frames
    private List<Rectangle> ultimasFacesDetectadas = new ArrayList<>(); // Para manter coordenadas das faces

    // Largura/altura do frame e parâmetros do guia oval (em pixels da imagem 640x480)
    private static final int FRAME_W = 640;
    private static final int FRAME_H = 480;
    // Tamanho relativo aceitável da face em relação à área do frame
    private static final double AREA_MIN = 0.08; // muito pequena → aproxime-se
    private static final double AREA_MAX = 0.45; // muito grande → afaste-se
    // Tolerância de centralização em pixels
    private static final int TOLERANCIA_X = 90;
    private static final int TOLERANCIA_Y = 70;
    // Quantidade de frames consecutivos com posicionamento OK para liberar captura
    private static final int FRAMES_PARA_OK = 5;
    private int framesAlinhados = 0;

    private enum EstadoPosicionamento {
        SEM_FACE, LONGE, PERTO, DESCENTRADO, OK
    }

    @FXML
    public void initialize() {
        camera = new OpenCVFrameGrabber(0);
        // Configurar resolução menor para melhor performance
        camera.setImageWidth(640);
        camera.setImageHeight(480);
        converterParaMat = new OpenCVFrameConverter.ToMat();

        // Inicializa o reconhecimento facial
        reconhecimentoFacial = new ReconhecimentoFacial();
        reconhecimentoFacial.inicializar();

        // Configura modo sensível para melhor detecção no macOS
        reconhecimentoFacial.configurarModoSensivel();

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
            framesAlinhados = 0;

            // Mostra o botão de capturar e atualiza o status
            btnIniciarCamera.setText("Parar Câmera");
            btnCapturar.setVisible(true);
            btnCapturar.setDisable(true);
            if (lblStatus != null) {
                lblStatus.setText("Câmera ativa - Posicione-se na frente da câmera");
                lblStatus.setStyle("-fx-text-fill: green;");
            }
            atualizarOverlay(EstadoPosicionamento.SEM_FACE);

            Runnable frameGrabber = () -> {
                try {
                    Frame frame = camera.grab();
                    if (frame != null) {
                        frameCapturado = converterParaMat.convert(frame);
                        frameCounter++;

                        // Aplica detecção facial se estiver ativa
                        Image imageToShow;
                        if (deteccaoAtiva && reconhecimentoFacial.isInicializado()) {
                            // Processar detecção apenas a cada SKIP_FRAMES + 1 frames para melhor performance
                            if (frameCounter % (SKIP_FRAMES + 1) == 0) {
                                BufferedImage bufferedImage = matToBufferedImage(frameCapturado);
                                if (bufferedImage != null) {
                                    // Detecta faces e salva coordenadas
                                    ultimasFacesDetectadas = reconhecimentoFacial.detectarFacesComCoordenadas(bufferedImage);
                                    ultimaFaceDetectada = !ultimasFacesDetectadas.isEmpty();
                                    
                                    // Desenha retângulos com as coordenadas reais
                                    BufferedImage imagemComDetecao = reconhecimentoFacial.desenharRetangulosFaces(bufferedImage);
                                    imageToShow = bufferedImageToImage(imagemComDetecao);

                                    EstadoPosicionamento estado = avaliarPosicionamento(ultimasFacesDetectadas);
                                    Platform.runLater(() -> atualizarOverlay(estado));
                                } else {
                                    imageToShow = matToImage(frameCapturado);
                                }
                            } else {
                                // Mostrar frame com detecção visual baseada no último resultado
                                BufferedImage bufferedImage = matToBufferedImage(frameCapturado);
                                if (bufferedImage != null) {
                                    BufferedImage imagemComDetecao = reconhecimentoFacial.desenharRetangulosComCoordenadas(bufferedImage, ultimasFacesDetectadas, ultimaFaceDetectada);
                                    imageToShow = bufferedImageToImage(imagemComDetecao);
                                } else {
                                    imageToShow = matToImage(frameCapturado);
                                }
                            }
                        } else {
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
            timer.scheduleAtFixedRate(frameGrabber, 0, 50, TimeUnit.MILLISECONDS);

        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
            // Lidar com erro de câmera
        }
    }

    @FXML
    void capturarFoto(ActionEvent event) {
        
        if (frameCapturado != null && !frameCapturado.empty()) {
            System.out.println("Dimensões do frame: " + frameCapturado.rows() + "x" + frameCapturado.cols());
            
            // Captura a imagem diretamente do frame atual
            BufferedImage imagemBruta = matToBufferedImage(frameCapturado);

            // Recorta a região facial para reduzir ruído de fundo antes de armazenar
            this.imagemCapturada = recortarRegiaoFacial(imagemBruta, ultimasFacesDetectadas);
            System.out.println("Imagem capturada: " + (this.imagemCapturada != null ? "OK" : "NULL"));
            
            if (this.imagemCapturada != null) {
                System.out.println("✅ Dimensões da imagem: " + this.imagemCapturada.getWidth() + "x" + this.imagemCapturada.getHeight());
                System.out.println("✅ Tipo da imagem: " + this.imagemCapturada.getType());
                System.out.println("✅ Imagem armazenada em CameraController.imagemCapturada");
            } else {
                System.err.println("❌ Falha ao converter frame para BufferedImage!");
            }

            // Para a câmera após capturar (mas mantém a imagem na memória)
            pararCamera();

            // Atualiza o status
            if (lblStatus != null) {
                lblStatus.setText("Foto capturada com sucesso!");
                lblStatus.setStyle("-fx-text-fill: green;");
            }

            // Fecha a janela após um pequeno delay para o usuário ver a confirmação
            // IMPORTANTE: A imagem já foi capturada e armazenada antes de fechar
            System.out.println("Agendando fechamento da janela após delay...");
            Platform.runLater(() -> {
                javafx.concurrent.Task<Void> delayTask = new javafx.concurrent.Task<Void>() {
                    @Override
                    protected Void call() throws Exception {
                        System.out.println("[Task] Iniciando delay de 300ms...");
                        Thread.sleep(300); // 300ms de delay (reduzido para resposta mais rápida)
                        return null;
                    }

                    @Override
                    protected void succeeded() {
                        System.out.println("\n" + "=".repeat(80));
                        System.out.println("🔔 [CameraController] Fechando janela da câmera...");
                        System.out.println("=".repeat(80));
                        System.out.println("  - Verificando se imagem ainda existe antes de fechar: " + 
                                         (imagemCapturada != null ? "SIM (" + imagemCapturada.getWidth() + "x" + imagemCapturada.getHeight() + ")" : "NÃO"));
                        if (imagemCapturada != null) {
                            System.out.println("  - Tipo da imagem: " + imagemCapturada.getType());
                            System.out.println("  - ✅ Imagem será mantida para retorno ao controller pai");
                        } else {
                            System.err.println("  - ❌ ERRO: Imagem é NULL - não será possível retornar ao controller pai");
                        }
                        // NÃO limpa a imagem aqui - ela precisa ser retornada ao controller pai
                        fecharJanelaSemLimparImagem();
                    }
                };
                new Thread(delayTask).start();
            });
        } else {
            System.err.println("❌ Frame capturado é NULL ou vazio!");
            this.imagemCapturada = null; // Garante que está NULL se não houve captura
        }
    }
    
    /**
     * Fecha a janela sem limpar a imagem capturada
     * A imagem deve ser mantida para ser retornada ao controller pai
     */
    private void fecharJanelaSemLimparImagem() {
        // Para a câmera mas NÃO limpa a imagem
        if (cameraAtiva) {
            cameraAtiva = false;
            framesAlinhados = 0;
            btnIniciarCamera.setText("Iniciar Câmera");
            btnCapturar.setVisible(false);
            btnCapturar.setDisable(true);
            
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
        
        // Fecha a janela
        if (btnCancelar != null && btnCancelar.getScene() != null) {
            Stage stage = (Stage) btnCancelar.getScene().getWindow();
            if (stage != null) {
                System.out.println("  - Fechando Stage da câmera...");
                System.out.println("  - Imagem antes de fechar: " + (imagemCapturada != null ? 
                                 "SIM (" + imagemCapturada.getWidth() + "x" + imagemCapturada.getHeight() + ")" : "NÃO"));
                stage.close();
                System.out.println("  - Stage fechado.");
                System.out.println("  - Imagem após fechar: " + (imagemCapturada != null ? 
                                 "SIM (" + imagemCapturada.getWidth() + "x" + imagemCapturada.getHeight() + ")" : "NÃO"));
                System.out.println("  - ✅ Imagem mantida para retorno ao controller pai");
            } else {
                System.err.println("  - ❌ ERRO: Stage é NULL - não foi possível fechar a janela");
            }
        } else {
            System.err.println("  - ❌ ERRO: btnCancelar ou Scene é NULL - não foi possível fechar a janela");
        }
    }

    @FXML
    void cancelar(ActionEvent event) {
        this.imagemCapturada = null; // Garante que nenhuma imagem seja retornada
        limparRecursos(); // Limpa recursos antes de fechar
        fecharJanela();
    }

    public BufferedImage getImagemCapturada() {
        System.out.println("[CameraController.getImagemCapturada()] Chamado");
        System.out.println("  - imagemCapturada é: " + (this.imagemCapturada != null ? "NÃO NULL" : "NULL"));
        if (this.imagemCapturada != null) {
            System.out.println("  - Dimensões: " + this.imagemCapturada.getWidth() + "x" + this.imagemCapturada.getHeight());
            System.out.println("  - Tipo: " + this.imagemCapturada.getType());
        }
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
                    // NÃO limpa a imagem aqui - ela precisa ser retornada ao controller pai
                    stage.setOnCloseRequest(event -> {
                        System.out.println("Janela da câmera sendo fechada - liberando recursos da câmera (mantendo imagem)...");
                        // Limpa apenas os recursos da câmera, mas mantém a imagem
                        if (cameraAtiva) {
                            cameraAtiva = false;
                            if (timer != null && !timer.isShutdown()) {
                                try {
                                    timer.shutdown();
                                } catch (Exception e) {
                                    System.err.println("Erro ao parar timer: " + e.getMessage());
                                }
                            }
                            if (camera != null) {
                                try {
                                    camera.stop();
                                    camera.release();
                                } catch (FrameGrabber.Exception e) {
                                    System.err.println("Erro ao parar câmera: " + e.getMessage());
                                }
                            }
                        }
                        System.out.println("  - Imagem ainda disponível após fechar: " + (imagemCapturada != null ? "SIM" : "NÃO"));
                    });

                    // Listener para quando a janela for ocultada
                    // NÃO limpa a imagem aqui também
                    stage.setOnHidden(event -> {
                        System.out.println("Janela da câmera ocultada - recursos já liberados (imagem mantida)");
                        System.out.println("  - Imagem ainda disponível após ocultar: " + (imagemCapturada != null ? "SIM" : "NÃO"));
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
        framesAlinhados = 0;
        btnIniciarCamera.setText("Iniciar Câmera");
        btnCapturar.setVisible(false);
        btnCapturar.setDisable(true);

        if (lblStatus != null) {
            lblStatus.setText("Câmera parada - Clique em 'Iniciar Câmera' para começar");
            lblStatus.setStyle("-fx-text-fill: orange;");
        }
        if (lblOrientacao != null) {
            lblOrientacao.setText("Posicione seu rosto dentro do oval");
        }
        if (guiaFace != null) {
            guiaFace.setStroke(Color.web("#facc15"));
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

    /**
     * Avalia se a face está bem posicionada dentro do oval guia.
     * Atualiza contador de frames alinhados para liberar a captura.
     */
    private EstadoPosicionamento avaliarPosicionamento(List<Rectangle> faces) {
        if (faces == null || faces.isEmpty()) {
            framesAlinhados = 0;
            return EstadoPosicionamento.SEM_FACE;
        }

        // Usa a maior face detectada (mais próxima/relevante)
        Rectangle face = faces.get(0);
        int areaMaior = face.width * face.height;
        for (Rectangle r : faces) {
            int a = r.width * r.height;
            if (a > areaMaior) {
                areaMaior = a;
                face = r;
            }
        }

        double areaFrame = (double) FRAME_W * FRAME_H;
        double razaoArea = (face.width * face.height) / areaFrame;

        int centroFx = face.x + face.width / 2;
        int centroFy = face.y + face.height / 2;
        int dx = centroFx - FRAME_W / 2;
        int dy = centroFy - FRAME_H / 2;

        if (razaoArea < AREA_MIN) {
            framesAlinhados = 0;
            return EstadoPosicionamento.LONGE;
        }
        if (razaoArea > AREA_MAX) {
            framesAlinhados = 0;
            return EstadoPosicionamento.PERTO;
        }
        if (Math.abs(dx) > TOLERANCIA_X || Math.abs(dy) > TOLERANCIA_Y) {
            framesAlinhados = 0;
            return EstadoPosicionamento.DESCENTRADO;
        }

        framesAlinhados++;
        return EstadoPosicionamento.OK;
    }

    /**
     * Atualiza cor do oval guia, mensagem de orientação e estado do botão de captura.
     */
    private void atualizarOverlay(EstadoPosicionamento estado) {
        String texto;
        Color cor;
        switch (estado) {
            case OK:
                texto = "Pronto! Mantenha-se parado e clique em Capturar";
                cor = Color.web("#22c55e");
                break;
            case LONGE:
                texto = "Aproxime-se um pouco da câmera";
                cor = Color.web("#fb923c");
                break;
            case PERTO:
                texto = "Afaste-se um pouco da câmera";
                cor = Color.web("#fb923c");
                break;
            case DESCENTRADO:
                texto = "Centralize seu rosto dentro do oval";
                cor = Color.web("#fb923c");
                break;
            case SEM_FACE:
            default:
                texto = "Posicione seu rosto dentro do oval";
                cor = Color.web("#facc15");
                break;
        }

        if (guiaFace != null) {
            guiaFace.setStroke(cor);
        }
        if (lblOrientacao != null) {
            lblOrientacao.setText(texto);
        }
        if (lblStatus != null) {
            lblStatus.setText(texto);
            lblStatus.setStyle(estado == EstadoPosicionamento.OK
                    ? "-fx-text-fill: green;"
                    : (estado == EstadoPosicionamento.SEM_FACE
                            ? "-fx-text-fill: #b45309;"
                            : "-fx-text-fill: orange;"));
        }

        boolean libera = estado == EstadoPosicionamento.OK && framesAlinhados >= FRAMES_PARA_OK;
        if (btnCapturar != null) {
            btnCapturar.setDisable(!libera);
        }
    }

    /**
     * Recorta a região facial central com padding, eliminando o fundo desnecessário.
     * Caso não exista face detectada, faz um crop central padrão (zona do oval guia).
     */
    private BufferedImage recortarRegiaoFacial(BufferedImage src, List<Rectangle> faces) {
        if (src == null) return null;
        int w = src.getWidth();
        int h = src.getHeight();

        Rectangle alvo = null;
        if (faces != null && !faces.isEmpty()) {
            alvo = faces.get(0);
            int areaMaior = alvo.width * alvo.height;
            for (Rectangle r : faces) {
                int a = r.width * r.height;
                if (a > areaMaior) {
                    areaMaior = a;
                    alvo = r;
                }
            }
        }

        int x1, y1, x2, y2;
        if (alvo != null) {
            // Padding generoso para incluir testa, queixo e laterais (importante para descritores)
            int padX = (int) (alvo.width * 0.30);
            int padY = (int) (alvo.height * 0.40);
            x1 = Math.max(0, alvo.x - padX);
            y1 = Math.max(0, alvo.y - (int) (alvo.height * 0.50)); // mais espaço acima (cabelo)
            x2 = Math.min(w, alvo.x + alvo.width + padX);
            y2 = Math.min(h, alvo.y + alvo.height + padY);
        } else {
            // Fallback: crop central proporcional ao oval guia
            int cropW = (int) (w * 0.55);
            int cropH = (int) (h * 0.85);
            x1 = (w - cropW) / 2;
            y1 = (h - cropH) / 2;
            x2 = x1 + cropW;
            y2 = y1 + cropH;
        }

        int subW = Math.max(1, x2 - x1);
        int subH = Math.max(1, y2 - y1);
        try {
            return src.getSubimage(x1, y1, subW, subH);
        } catch (Exception e) {
            System.err.println("Falha ao recortar região facial, usando imagem original: " + e.getMessage());
            return src;
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
        try {
            System.out.println("Convertendo Mat para BufferedImage...");
            
            BytePointer bytePointer = new BytePointer();
            boolean encoded = opencv_imgcodecs.imencode(".png", frame, bytePointer);
            
            if (!encoded) {
                System.err.println("❌ Falha ao codificar Mat para PNG");
                bytePointer.close();
                return null;
            }
            
            byte[] bytes = new byte[(int) bytePointer.limit()];
            bytePointer.get(bytes);
            bytePointer.close();
            
            System.out.println("Mat codificado em " + bytes.length + " bytes");
            
            BufferedImage result = ImageIO.read(new ByteArrayInputStream(bytes));
            if (result != null) {
                System.out.println("BufferedImage criado: " + result.getWidth() + "x" + result.getHeight() + " tipo: " + result.getType());
            } else {
                System.err.println("❌ Falha ao criar BufferedImage do stream");
            }
            
            return result;
        } catch (Exception e) {
            System.err.println("❌ Erro na conversão Mat -> BufferedImage: " + e.getMessage());
            e.printStackTrace();
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
