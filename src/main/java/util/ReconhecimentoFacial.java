package util;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.opencv.global.opencv_core;
import org.bytedeco.opencv.global.opencv_dnn;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Scalar;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_dnn.Net;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

/**
 * Classe para reconhecimento facial usando OpenCV Implementa detecção de faces
 * em tempo real
 */
public class ReconhecimentoFacial {

    private CascadeClassifier faceDetector; // Fallback: Haar Cascade
    private CascadeClassifier eyeDetector; // Para detecção de olhos (landmarks)
    private Net faceRecognitionNet; // DNN para reconhecimento (embeddings)
    private Net faceDetectionNet; // DNN para detecção de faces (YuNet/MTCNN)
    private boolean inicializado = false;
    private boolean dnnInicializado = false;
    private boolean dnnDetectionInicializado = false; // Detector DNN de faces

    // Parâmetros mais permissivos para melhor detecção
    private double scaleFactor = 1.05; // Mais sensível (menor = mais detecções)
    private int minNeighbors = 2; // Menos confirmações necessárias (mais permissivo)
    private int minSize = 20; // Faces menores aceitas
    private int maxSize = 600; // Faces maiores aceitas
    
    // Dimensões esperadas pelo modelo de reconhecimento facial
    private static final int FACE_INPUT_SIZE = 160; // Tamanho de entrada para FaceNet Inception
    private static final int EMBEDDING_SIZE = 128; // Tamanho do embedding de saída
    
    // Configurações do modelo DNN de reconhecimento (embeddings)
    private static final String[] RECOGNITION_MODEL_PATHS = {
        "target/classes/models/faceNet.onnx",
        "/models/faceNet.onnx",
        "models/faceNet.onnx",
    };
    
    // Configurações do modelo DNN de detecção de faces (YuNet, MTCNN, etc.)
    private static final String[] DETECTION_MODEL_PATHS = {
        "target/classes/models/faceNet.onnx",
        "/models/faceNet.onnx",
        "models/faceNet.onnx",
        "/models/yunet.onnx",
        "/models/face_detection_yunet_2023mar.onnx",
        "/models/opencv_face_detector_uint8.onnx",
        "/models/face_detection_yunet_2022mar.onnx",
        "models/yunet.onnx",
        "models/face_detection_yunet_2023mar.onnx",
        "models/opencv_face_detector_uint8.onnx",
        "models/face_detection_yunet_2022mar.onnx"
    };
    
    // Tamanho de entrada para detector DNN de faces (YuNet usa 320x320 ou 640x640)
    private static final int DETECTION_INPUT_SIZE = 320;
    
    // Nomes das camadas de entrada e saída (detectados em runtime;
    // null = deixar o OpenCV usar a entrada/saída padrão do grafo)
    private String inputLayerName = null;
    private String outputLayerName = null;

    /**
     * Inicializa o detector de faces, detector de olhos e o modelo DNN de reconhecimento facial
     */
    public void inicializar() {
        inicializarDetectorFacesDNN(); // Tenta DNN primeiro (YuNet)
        inicializarDetectorFaces(); // Fallback: Haar Cascade
        inicializarDetectorOlhos();
        inicializarModeloDNN();
    }
    
    /**
     * Inicializa o detector de faces DNN (YuNet/MTCNN) - mais robusto que Haar Cascade
     */
    private void inicializarDetectorFacesDNN() {
        System.out.println("\n[INICIALIZAÇÃO DNN DETECÇÃO] Tentando carregar detector DNN de faces (YuNet)...");
        
        for (String modelPath : DETECTION_MODEL_PATHS) {
            try {
                System.out.println("   Tentando carregar detector: " + modelPath);
                
                java.net.URL modelUrl = getClass().getResource(modelPath);
                String finalPath = null;
                
                if (modelUrl != null) {
                    finalPath = modelUrl.getPath();
                    if (finalPath.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("win")) {
                        finalPath = finalPath.substring(1);
                    }
                    finalPath = corrigirCaminhoArquivo(finalPath);
                } else {
                    java.io.File modelFile = new java.io.File(modelPath);
                    if (modelFile.exists()) {
                        finalPath = modelFile.getAbsolutePath();
                    }
                }
                
                if (finalPath != null && new java.io.File(finalPath).exists()) {
                    System.out.println("   ✅ Arquivo encontrado: " + finalPath);
                    
                    // Carrega modelo ONNX para detecção de faces
                    faceDetectionNet = opencv_dnn.readNetFromONNX(finalPath);
                    
                    if (faceDetectionNet != null && !faceDetectionNet.empty()) {
                        // Configura backend e target
                        faceDetectionNet.setPreferableBackend(opencv_dnn.DNN_BACKEND_OPENCV);
                        faceDetectionNet.setPreferableTarget(opencv_dnn.DNN_TARGET_CPU);
                        
                        dnnDetectionInicializado = true;
                        System.out.println("   ✅✅✅ DETECTOR DNN DE FACES CARREGADO COM SUCESSO! ✅✅✅");
                        System.out.println("      Modelo: " + modelPath);
                        System.out.println("      Backend: OpenCV DNN");
                        System.out.println("      Target: CPU");
                        System.out.println("      Tamanho de entrada: " + DETECTION_INPUT_SIZE + "x" + DETECTION_INPUT_SIZE);
                        return;
                    }
                } else {
                    System.out.println("   ⚠️ Arquivo não encontrado: " + modelPath);
                }
            } catch (Exception e) {
                System.out.println("   ⚠️ Erro ao carregar " + modelPath + ": " + e.getMessage());
            }
        }
        
        // Se chegou aqui, nenhum modelo DNN foi carregado
        System.out.println("   ⚠️ Nenhum detector DNN de faces encontrado");
        System.out.println("   💡 Usando detector Haar Cascade (fallback)");
        System.out.println("   📝 Para usar detector DNN:");
        System.out.println("      1. Baixe um modelo YuNet ONNX");
        System.out.println("      2. Coloque em: src/main/resources/models/yunet.onnx");
        System.out.println("      3. Reinicie a aplicação");
        dnnDetectionInicializado = false;
    }
    
    /**
     * Inicializa apenas o detector de faces (Haar Cascade) - usado como fallback
     */
    private void inicializarDetectorFaces() {
        try {
            // Carrega o classificador Haar Cascade para detecção de faces
            String cascadePath = getClass().getResource("/opencv/haarcascades/haarcascade_frontalface_default.xml").getPath();

            // Corrige o caminho para diferentes sistemas operacionais
            cascadePath = corrigirCaminhoArquivo(cascadePath);

            faceDetector = new CascadeClassifier(cascadePath);

            if (faceDetector.empty()) {
                System.err.println("Erro ao carregar o classificador de faces - tentando arquivo alternativo");
                String altCascadePath = getClass().getResource("/opencv/haarcascades/haarcascade_frontalface_alt.xml").getPath();

                altCascadePath = corrigirCaminhoArquivo(altCascadePath);

                faceDetector = new CascadeClassifier(altCascadePath);

                if (faceDetector.empty()) {
                    System.err.println("Erro ao carregar ambos os classificadores de faces");
                    inicializado = false;
                } else {
                    inicializado = true;
                    System.out.println("Detector de faces inicializado com sucesso (arquivo alternativo)");
                }
            } else {
                inicializado = true;
                System.out.println("Detector de faces inicializado com sucesso");
            }
        } catch (Exception e) {
            System.err.println("Erro ao inicializar detector de faces: " + e.getMessage());
            e.printStackTrace();
            inicializado = false;
        }
    }
    
    /**
     * Inicializa o detector de olhos para alinhamento facial baseado em landmarks
     */
    private void inicializarDetectorOlhos() {
        try {
            // Tenta carregar detector de olhos com validação de recurso para evitar NPE
            java.net.URL eyeCascadeUrl = getClass().getResource("/opencv/haarcascades/haarcascade_eye.xml");
            if (eyeCascadeUrl == null) {
                System.out.println("⚠️ Arquivo haarcascade_eye.xml não encontrado em /opencv/haarcascades/");
                eyeDetector = null;
                return;
            }

            String eyeCascadePath = corrigirCaminhoArquivo(eyeCascadeUrl.getPath());
            eyeDetector = new CascadeClassifier(eyeCascadePath);
            
            if (eyeDetector.empty()) {
                // Tenta arquivo alternativo
                java.net.URL altEyeUrl = getClass().getResource("/opencv/haarcascades/haarcascade_eye_tree_eyeglasses.xml");
                if (altEyeUrl != null) {
                    String altEyePath = corrigirCaminhoArquivo(altEyeUrl.getPath());
                    eyeDetector = new CascadeClassifier(altEyePath);
                }
                
                if (eyeDetector.empty()) {
                    System.out.println("⚠️ Detector de olhos não disponível - alinhamento será baseado em estimativas");
                    eyeDetector = null;
                } else {
                    System.out.println("✅ Detector de olhos inicializado (arquivo alternativo)");
                }
            } else {
                System.out.println("✅ Detector de olhos inicializado");
            }
        } catch (Exception e) {
            System.out.println("⚠️ Não foi possível inicializar detector de olhos: " + e.getMessage());
            System.out.println("   Alinhamento será baseado em estimativas de proporções faciais");
            eyeDetector = null;
        }
    }
    
    /**
     * Inicializa o modelo DNN de reconhecimento facial
     * Tenta carregar modelos ONNX (OpenFace, FaceNet, ArcFace) ou TensorFlow
     * Se nenhum modelo estiver disponível, usa método baseado em características
     */
    private void inicializarModeloDNN() {
        System.out.println("\n[INICIALIZAÇÃO DNN] Tentando carregar modelo de reconhecimento facial...");
        
        // Tenta carregar modelo ONNX primeiro
        for (String modelPath : RECOGNITION_MODEL_PATHS) {
            try {
                System.out.println("   Tentando carregar modelo: " + modelPath);
                
                // Tenta como recurso do JAR primeiro
                java.net.URL modelUrl = getClass().getResource(modelPath);
                String finalPath = null;
                
                if (modelUrl != null) {
                    finalPath = modelUrl.getPath();
                    // Corrige caminho para Windows
                    if (finalPath.startsWith("/") && System.getProperty("os.name").toLowerCase().contains("win")) {
                        finalPath = finalPath.substring(1);
                    }
                    finalPath = corrigirCaminhoArquivo(finalPath);
                } else {
                    // Tenta caminho relativo/absoluto
                    java.io.File modelFile = new java.io.File(modelPath);
                    if (modelFile.exists()) {
                        finalPath = modelFile.getAbsolutePath();
                    }
                }
                
                if (finalPath != null && new java.io.File(finalPath).exists()) {
                    System.out.println("   ✅ Arquivo encontrado: " + finalPath);
                    
                    // Usa estritamente FaceNet no reconhecimento
                    if (modelPath.toLowerCase().contains("facenet")) {
                        // Carrega modelo ONNX
                        faceRecognitionNet = opencv_dnn.readNetFromONNX(finalPath);
                        
                        if (faceRecognitionNet != null && !faceRecognitionNet.empty()) {
                            // Configura backend e target (CPU por padrão, pode usar GPU se disponível)
                            faceRecognitionNet.setPreferableBackend(opencv_dnn.DNN_BACKEND_OPENCV);
                            faceRecognitionNet.setPreferableTarget(opencv_dnn.DNN_TARGET_CPU);
                            
                            // Detecta nomes das camadas baseado no modelo
                            detectarNomesCamadas(modelPath);
                            
                            dnnInicializado = true;
                            System.out.println("   ✅✅✅ MODELO DNN CARREGADO COM SUCESSO! ✅✅✅");
                            System.out.println("      Modelo: " + modelPath);
                            System.out.println("      Backend: OpenCV DNN");
                            System.out.println("      Target: CPU");
                            System.out.println("      Camada de entrada: " + inputLayerName);
                            System.out.println("      Camada de saída: " + outputLayerName);
                            return;
                        }
                    }
                } else {
                    System.out.println("   ⚠️ Arquivo não encontrado: " + modelPath);
                }
            } catch (Exception e) {
                System.out.println("   ⚠️ Erro ao carregar " + modelPath + ": " + e.getMessage());
                // Continua tentando outros modelos
            }
        }
        
        // Se chegou aqui, nenhum modelo DNN foi carregado
        System.out.println("   ⚠️ Nenhum modelo FaceNet compatível encontrado.");
        System.out.println("   📝 Caminho esperado: target/classes/models/faceNet.onnx");
        dnnInicializado = false;
    }
    
    /**
     * Detecta os nomes reais das camadas de entrada e saída do modelo carregado.
     * Em vez de adivinhar pelo nome do arquivo (que pode ter caixa diferente ou
     * variar entre exports), consulta o grafo via {@code getLayerNames()} e
     * {@code getUnconnectedOutLayersNames()}. Se a consulta falhar, deixa
     * {@code inputLayerName}/{@code outputLayerName} como {@code null}, o que faz
     * o {@link #extrairEmbeddingComDNN(Mat)} cair para {@code setInput(blob)} e
     * {@code forward()} sem nome de camada — usando a entrada/saída padrão.
     */
    private void detectarNomesCamadas(String modelPath) {
        inputLayerName = null;
        outputLayerName = null;

        if (faceRecognitionNet == null || faceRecognitionNet.empty()) {
            return;
        }

        try {
            org.bytedeco.opencv.opencv_core.StringVector outNames =
                    faceRecognitionNet.getUnconnectedOutLayersNames();
            if (outNames != null && outNames.size() > 0) {
                BytePointer first = outNames.get(0);
                if (first != null && !first.isNull()) {
                    String detected = first.getString();
                    if (detected != null && !detected.isEmpty()) {
                        outputLayerName = detected;
                    }
                }
            }
        } catch (Throwable t) {
            // Mantém outputLayerName = null; forward() usará a saída padrão.
        }

        try {
            org.bytedeco.opencv.opencv_core.StringVector layerNames =
                    faceRecognitionNet.getLayerNames();
            if (layerNames != null && layerNames.size() > 0) {
                BytePointer first = layerNames.get(0);
                if (first != null && !first.isNull()) {
                    String detected = first.getString();
                    if (detected != null && !detected.isEmpty()) {
                        inputLayerName = detected;
                    }
                }
            }
        } catch (Throwable t) {
            // Mantém inputLayerName = null; setInput(blob) usará a entrada padrão.
        }

        System.out.println("   Nomes das camadas detectados (modelo: " + modelPath + "):");
        System.out.println("      Entrada: " + (inputLayerName != null ? inputLayerName : "<padrão>"));
        System.out.println("      Saída:   " + (outputLayerName != null ? outputLayerName : "<padrão>"));
    }

    /**
     * Verifica se o detector foi inicializado corretamente
     */
    public boolean isInicializado() {
        return inicializado;
    }

    /**
     * Configura parâmetros mais rigorosos para reduzir falsos positivos
     */
    public void configurarModoRigoroso() {
        this.scaleFactor = 1.2;  // Mais conservador
        this.minNeighbors = 6;   // Mais confirmações necessárias
        this.minSize = 40;       // Faces maiores
        this.maxSize = 300;      // Limite menor
    }

    /**
     * Configura parâmetros mais sensíveis para detectar mais faces
     */
    public void configurarModoSensivel() {
        this.scaleFactor = 1.2; // Muito mais sensível
        this.minNeighbors = 4;   // Mínimo de confirmações
        this.minSize = 20;        // Faces muito pequenas
        this.maxSize = 700;     // Limite muito maior
    }

    /**
     * Restaura parâmetros padrão balanceados
     */
    public void configurarModoPadrao() {
        this.scaleFactor = 1.1;
        this.minNeighbors = 4;
        this.minSize = 30;
        this.maxSize = 400;
    }

    /**
     * Corrige o caminho do arquivo para funcionar em diferentes sistemas
     * operacionais
     */
    private String corrigirCaminhoArquivo(String caminho) {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("windows")) {
            if (caminho.startsWith("/")) {
                caminho = caminho.substring(1);
            }
            caminho = caminho.replace("/", "\\");
        } else if (osName.contains("linux") || osName.contains("unix") || osName.contains("mac")) {

        }

        return caminho;
    }

    /**
     * Captura imagem da webcam usando JavaFX Retorna null se não conseguir
     * capturar
     */
    public BufferedImage capturarImagem() {
        // Implementação básica - retorna null por enquanto
        // Pode ser expandida com JavaFX WebView ou outras bibliotecas
        System.out.println("Funcionalidade de captura de imagem não implementada ainda");
        return null;
    }

    /**
     * Detecta faces em uma imagem usando DNN (YuNet) ou Haar Cascade (fallback)
     */
    public boolean detectarFace(BufferedImage imagem) {
        if (imagem == null) {
            return false;
        }

        // Tenta usar detector DNN primeiro (mais robusto)
        if (dnnDetectionInicializado && faceDetectionNet != null && !faceDetectionNet.empty()) {
            List<Rectangle> faces = detectarFacesComDNN(imagem);
            return !faces.isEmpty();
        }
        
        // Fallback para Haar Cascade
        if (!inicializado) {
            return false;
        }

        try (Mat matImagem = bufferedImageToMat(imagem); Mat grayImage = new Mat(); RectVector faces = new RectVector()) {

            opencv_imgproc.cvtColor(matImagem, grayImage, opencv_imgproc.COLOR_BGR2GRAY);

            opencv_imgproc.equalizeHist(grayImage, grayImage);

            faceDetector.detectMultiScale(grayImage, faces, scaleFactor, minNeighbors, 0,
                    new org.bytedeco.opencv.opencv_core.Size(minSize, minSize),
                    new org.bytedeco.opencv.opencv_core.Size(maxSize, maxSize));

            int numFaces = (int) faces.size();

            // Filtra faces por qualidade e proporção
            if (numFaces > 0) {
                return validarFaces(faces, imagem.getWidth(), imagem.getHeight());
            }

            return false;

        } catch (Exception e) {
            System.err.println("Erro ao detectar faces: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Detecta faces usando modelo DNN (YuNet/MTCNN) - mais robusto e preciso que Haar Cascade
     * 
     * @param imagem BufferedImage contendo a imagem para detecção
     * @return Lista de Rectangle com as faces detectadas
     */
    private List<Rectangle> detectarFacesComDNN(BufferedImage imagem) {
        List<Rectangle> faces = new ArrayList<>();
        
        try {
            System.out.println("   [DNN Detecção] Detectando faces usando modelo DNN...");
            
            if (faceDetectionNet == null || faceDetectionNet.empty()) {
                System.out.println("   ⚠️ Detector DNN não disponível, usando fallback");
                return faces;
            }
            
            // Converte BufferedImage para Mat
            Mat matImagem = bufferedImageToMat(imagem);
            int originalWidth = matImagem.cols();
            int originalHeight = matImagem.rows();
            
            // Redimensiona para o tamanho esperado pelo modelo (mantém aspect ratio)
            double scale = Math.min((double) DETECTION_INPUT_SIZE / originalWidth, 
                                   (double) DETECTION_INPUT_SIZE / originalHeight);
            int scaledWidth = (int) (originalWidth * scale);
            int scaledHeight = (int) (originalHeight * scale);
            
            Mat resized = new Mat();
            opencv_imgproc.resize(matImagem, resized, new Size(scaledWidth, scaledHeight));
            
            // Cria blob para o modelo DNN
            // YuNet geralmente espera valores normalizados
            Mat blob = opencv_dnn.blobFromImage(
                resized,
                1.0, // Scale factor
                new Size(scaledWidth, scaledHeight),
                new Scalar(0, 0, 0, 0), // Mean
                true, // Swap RB (BGR para RGB)
                false, // Crop
                opencv_core.CV_32F
            );
            
            // Define entrada do modelo
            faceDetectionNet.setInput(blob);
            
            // Executa inferência
            // YuNet geralmente retorna: [batch, num_detections, 15]
            // Onde 15 = [x, y, w, h, x_re, y_re, x_le, y_le, x_nt, y_nt, x_rcm, y_rcm, x_lcm, y_lcm, score]
            Mat detections = faceDetectionNet.forward();
            
            if (detections.empty()) {
                System.out.println("   ⚠️ Nenhuma detecção retornada pelo modelo DNN");
                resized.close();
                blob.close();
                matImagem.close();
                return faces;
            }
            
            System.out.println("   ✅ Detecções retornadas: " + detections.size(0) + " faces potenciais");
            
            // Processa as detecções
            // YuNet retorna detecções no formato [batch, num_detections, 15]
            int numDetections = detections.dims() >= 2 ? (int) detections.size(1) : 1;
            double threshold = 0.5; // Threshold de confiança
            
            // Verifica o formato da saída
            if (detections.dims() == 2) {
                // Formato [num_detections, 15]
                numDetections = (int) detections.size(0);
            } else if (detections.dims() == 3) {
                // Formato [batch, num_detections, 15]
                numDetections = (int) detections.size(1);
            }
            
            for (int i = 0; i < numDetections; i++) {
                // Lê os dados da detecção
                BytePointer detectionData;
                if (detections.dims() == 2) {
                    detectionData = detections.ptr(i);
                } else {
                    detectionData = detections.ptr(0, i);
                }
                
                // YuNet formato: [x, y, w, h, landmarks..., score]
                // Coordenadas são normalizadas [0, 1] ou absolutas dependendo do modelo
                float x = detectionData.getFloat(0);
                float y = detectionData.getFloat(1);
                float w = detectionData.getFloat(2);
                float h = detectionData.getFloat(3);
                float score = detectionData.getFloat(14); // Score está na posição 14
                
                // Se as coordenadas estão normalizadas, converte para absolutas
                if (x <= 1.0 && y <= 1.0 && w <= 1.0 && h <= 1.0) {
                    x = x * originalWidth;
                    y = y * originalHeight;
                    w = w * originalWidth;
                    h = h * originalHeight;
                } else {
                    // Ajusta escala se necessário
                    x = x * originalWidth / scaledWidth;
                    y = y * originalHeight / scaledHeight;
                    w = w * originalWidth / scaledWidth;
                    h = h * originalHeight / scaledHeight;
                }
                
                // Filtra por threshold de confiança
                if (score >= threshold && w > 0 && h > 0) {
                    // Converte para Rectangle (coordenadas inteiras)
                    Rectangle face = new Rectangle(
                        Math.max(0, (int) x),
                        Math.max(0, (int) y),
                        Math.min(originalWidth - (int) x, (int) w),
                        Math.min(originalHeight - (int) y, (int) h)
                    );
                    
                    // Valida a face detectada
                    if (validarQualidadeFace(face, originalWidth, originalHeight, (int) faces.size() + 1)) {
                        faces.add(face);
                        System.out.println("   ✅ Face detectada: x=" + face.x + ", y=" + face.y + 
                                         ", w=" + face.width + ", h=" + face.height + 
                                         ", score=" + String.format("%.3f", score));
                    }
                }
            }
            
            // Limpa recursos
            resized.close();
            blob.close();
            detections.close();
            matImagem.close();
            
            System.out.println("   ✅ Total de faces válidas detectadas: " + faces.size());
            
        } catch (Exception e) {
            System.err.println("   ❌ Erro ao detectar faces com DNN: " + e.getMessage());
            e.printStackTrace();
        }
        
        return faces;
    }

    /**
     * Valida as faces detectadas para filtrar falsos positivos
     */
    private boolean validarFaces(RectVector faces, int imageWidth, int imageHeight) {
        int validFaces = 0;

        for (long i = 0; i < faces.size(); i++) {
            Rect face = faces.get(i);

            // Verifica se a face está em uma posição razoável na imagem
            if (validarPosicaoFace(face, imageWidth, imageHeight)
                    && validarProporcaoFace(face)
                    && validarTamanhoFace(face, imageWidth, imageHeight)) {
                validFaces++;
            }
        }

        // Só considera válido se encontrar pelo menos uma face bem posicionada
        return validFaces > 0;
    }

    /**
     * Valida a qualidade completa de uma face detectada
     * Retorna true se a face atende a todos os critérios de qualidade
     */
    private boolean validarQualidadeFace(Rectangle face, int imageWidth, int imageHeight, int faceIndex) {
        System.out.println("      [Validação Face " + faceIndex + "] Iniciando validação...");
        System.out.println("         Coordenadas: x=" + face.x + ", y=" + face.y + 
                         ", w=" + face.width + ", h=" + face.height);
        
        // Converte Rectangle para Rect para usar os métodos existentes
        Rect faceRect = new Rect(face.x, face.y, face.width, face.height);
        
        boolean posicaoOK = validarPosicaoFace(faceRect, imageWidth, imageHeight);
        boolean proporcaoOK = validarProporcaoFace(faceRect);
        boolean tamanhoOK = validarTamanhoFace(faceRect, imageWidth, imageHeight);
        
        double aspectRatio = (double) face.width / face.height;
        double faceArea = face.width * face.height;
        double imageArea = imageWidth * imageHeight;
        double faceRatio = (faceArea * 100.0) / imageArea;
        
        System.out.println("         Posição: " + (posicaoOK ? "✅" : "❌"));
        System.out.println("         Proporção: " + (proporcaoOK ? "✅" : "❌") + 
                         " (aspect ratio: " + String.format("%.2f", aspectRatio) + ")");
        System.out.println("         Tamanho: " + (tamanhoOK ? "✅" : "❌") + 
                         " (área: " + String.format("%.2f", faceRatio) + "% da imagem)");
        
        boolean valida = posicaoOK && proporcaoOK && tamanhoOK;
        System.out.println("      [Validação Face " + faceIndex + "] Resultado: " + (valida ? "✅ VÁLIDA" : "❌ INVÁLIDA"));
        
        if (!valida) {
            System.out.println("         Motivos da rejeição:");
            if (!posicaoOK) System.out.println("            • Posição inadequada (muito próxima das bordas)");
            if (!proporcaoOK) System.out.println("            • Proporção inadequada (aspect ratio fora do range 0.7-1.4)");
            if (!tamanhoOK) System.out.println("            • Tamanho inadequado (área fora do range 0.1%-80%)");
        }
        
        faceRect.close();
        return valida;
    }
    
    /**
     * Valida se a face está em uma posição razoável na imagem
     * Critérios mais flexíveis para melhor detecção
     */
    private boolean validarPosicaoFace(Rect face, int imageWidth, int imageHeight) {
        // Margem muito pequena (quase zero) para ser mais permissivo
        int margin = Math.max(1, Math.min(imageWidth, imageHeight) / 200); // 0.5% da menor dimensão (muito permissivo)

        boolean valida = face.x() >= 0
                && face.y() >= 0
                && (face.x() + face.width()) <= imageWidth
                && (face.y() + face.height()) <= imageHeight;
        
        return valida;
    }

    /**
     * Valida se a proporção da face é razoável (aproximadamente quadrada)
     * Critérios mais flexíveis
     */
    private boolean validarProporcaoFace(Rect face) {
        double aspectRatio = (double) face.width() / face.height();
        // Range mais amplo: 0.5 a 2.0 (muito permissivo)
        return aspectRatio >= 0.5 && aspectRatio <= 2.0;
    }

    /**
     * Valida se o tamanho da face é razoável em relação à imagem
     * Critérios mais flexíveis
     */
    private boolean validarTamanhoFace(Rect face, int imageWidth, int imageHeight) {
        double faceArea = face.width() * face.height();
        double imageArea = imageWidth * imageHeight;
        double faceRatio = faceArea / imageArea;

        // Range muito amplo: 0.05% a 90% da área da imagem (muito permissivo)
        return faceRatio >= 0.0005 && faceRatio <= 0.9;
    }

    /**
     * Detecta faces e retorna as coordenadas dos retângulos
     * Tenta múltiplas configurações se a primeira não encontrar faces
     */
    public List<Rectangle> detectarFacesComCoordenadas(BufferedImage imagem) {
        List<Rectangle> faces = new ArrayList<>();

        if (imagem == null) {
            System.err.println("[Detecção] Imagem é NULL");
            return faces;
        }
        
        if (!inicializado) {
            System.err.println("[Detecção] Detector não inicializado - tentando inicializar...");
            inicializar();
            if (!inicializado) {
                System.err.println("[Detecção] ❌ Falha ao inicializar detector");
                return faces;
            }
        }

        System.out.println("[Detecção] Iniciando detecção de faces...");
        System.out.println("   Dimensões da imagem: " + imagem.getWidth() + "x" + imagem.getHeight());
        System.out.println("   Parâmetros: scaleFactor=" + scaleFactor + ", minNeighbors=" + minNeighbors + 
                         ", minSize=" + minSize + ", maxSize=" + maxSize);

        // Tenta usar detector DNN primeiro (mais robusto)
        if (dnnDetectionInicializado && faceDetectionNet != null && !faceDetectionNet.empty()) {
            System.out.println("   [DNN] Usando detector DNN de faces (YuNet)...");
            List<Rectangle> facesDNN = detectarFacesComDNN(imagem);
            
            if (!facesDNN.isEmpty()) {
                System.out.println("   ✅ " + facesDNN.size() + " face(s) detectada(s) usando DNN");
                return facesDNN;
            } else {
                System.out.println("   ⚠️ Nenhuma face detectada com DNN, tentando Haar Cascade...");
            }
        }
        
        // Fallback para Haar Cascade
        if (!inicializado) {
            System.out.println("   ⚠️ Detector não inicializado");
            return new ArrayList<>();
        }

        try (Mat matImagem = bufferedImageToMat(imagem); Mat grayImage = new Mat(); RectVector faceRects = new RectVector()) {

            // Converte para escala de cinza
            opencv_imgproc.cvtColor(matImagem, grayImage, opencv_imgproc.COLOR_BGR2GRAY);
            System.out.println("   ✅ Imagem convertida para escala de cinza");

            // Aplica equalização de histograma para melhorar o contraste
            opencv_imgproc.equalizeHist(grayImage, grayImage);
            System.out.println("   ✅ Equalização de histograma aplicada");

            // Tenta detecção com parâmetros padrão
            System.out.println("   [Haar Cascade] Tentando detecção com parâmetros padrão...");
            faceDetector.detectMultiScale(grayImage, faceRects, scaleFactor, minNeighbors, 0,
                    new org.bytedeco.opencv.opencv_core.Size(minSize, minSize),
                    new org.bytedeco.opencv.opencv_core.Size(maxSize, maxSize));

            int facesDetectadas = (int) faceRects.size();
            System.out.println("   Faces detectadas (antes da validação): " + facesDetectadas);

            // Se não encontrou faces, tenta com parâmetros mais permissivos
            if (facesDetectadas == 0) {
                System.out.println("   ⚠️ Nenhuma face encontrada com parâmetros padrão");
                System.out.println("   Tentando com parâmetros mais permissivos...");
                
                // Tenta com parâmetros mais sensíveis
                faceRects.clear();
                faceDetector.detectMultiScale(grayImage, faceRects, 1.03, 1, 0,
                        new org.bytedeco.opencv.opencv_core.Size(15, 15),
                        new org.bytedeco.opencv.opencv_core.Size(800, 800));
                
                facesDetectadas = (int) faceRects.size();
                System.out.println("   Faces detectadas (modo sensível): " + facesDetectadas);
            }

            // Filtra apenas as faces válidas
            for (long i = 0; i < faceRects.size(); i++) {
                Rect rect = faceRects.get(i);
                System.out.println("   Face candidata " + (i+1) + ": x=" + rect.x() + ", y=" + rect.y() + 
                                 ", w=" + rect.width() + ", h=" + rect.height());

                // Aplica as validações (mas com critérios mais flexíveis)
                boolean posicaoOK = validarPosicaoFace(rect, imagem.getWidth(), imagem.getHeight());
                boolean proporcaoOK = validarProporcaoFace(rect);
                boolean tamanhoOK = validarTamanhoFace(rect, imagem.getWidth(), imagem.getHeight());

                if (posicaoOK && proporcaoOK && tamanhoOK) {
                    faces.add(new Rectangle(rect.x(), rect.y(), rect.width(), rect.height()));
                    System.out.println("      ✅ Face " + (i+1) + " aceita após validação");
                } else {
                    System.out.println("      ❌ Face " + (i+1) + " rejeitada:");
                    if (!posicaoOK) System.out.println("         - Posição inadequada");
                    if (!proporcaoOK) System.out.println("         - Proporção inadequada");
                    if (!tamanhoOK) System.out.println("         - Tamanho inadequado");
                }
            }

            System.out.println("   ✅ Total de faces válidas: " + faces.size());

        } catch (Exception e) {
            System.err.println("❌ Erro ao detectar faces com coordenadas: " + e.getMessage());
            e.printStackTrace();
        }

        return faces;
    }

    /**
     * Desenha retângulos ao redor das faces detectadas com informações de
     * qualidade (versão otimizada para performance)
     */
    public BufferedImage desenharRetangulosFaces(BufferedImage imagem) {
        List<Rectangle> faces = detectarFacesComCoordenadas(imagem);

        // Cria uma cópia da imagem para desenhar
        BufferedImage imagemComRetangulos = new BufferedImage(
                imagem.getWidth(), imagem.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagemComRetangulos.createGraphics();
        g2d.drawImage(imagem, 0, 0, null);

        if (faces.isEmpty()) {
            // Desenha mensagem quando nenhuma face válida é detectada (só se necessário)
            g2d.setColor(java.awt.Color.RED);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14)); // Fonte menor
            g2d.drawString("Procurando...", 10, 25);
        } else {
            // Desenha retângulos verdes ao redor das faces válidas
            g2d.setColor(java.awt.Color.GREEN);
            g2d.setStroke(new java.awt.BasicStroke(2)); // Linha mais fina

            for (int i = 0; i < faces.size(); i++) {
                Rectangle face = faces.get(i);
                g2d.drawRect(face.x, face.y, face.width, face.height);
            }

            // Desenha informações sobre a detecção (simplificado)
            g2d.setColor(java.awt.Color.WHITE);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
            g2d.drawString("✓ Face detectada", 10, 20);
        }

        g2d.dispose();
        return imagemComRetangulos;
    }

    /**
     * Desenha retângulos baseado nas coordenadas reais das faces detectadas (para evitar piscar)
     */
    public BufferedImage desenharRetangulosComCoordenadas(BufferedImage imagem, List<Rectangle> faces, boolean faceDetectada) {
        // Cria uma cópia da imagem para desenhar
        BufferedImage imagemComRetangulos = new BufferedImage(
                imagem.getWidth(), imagem.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagemComRetangulos.createGraphics();
        g2d.drawImage(imagem, 0, 0, null);

        if (faceDetectada && !faces.isEmpty()) {
            // Desenha retângulos verdes nas posições reais das faces
            g2d.setColor(java.awt.Color.GREEN);
            g2d.setStroke(new java.awt.BasicStroke(2));

            for (int i = 0; i < faces.size(); i++) {
                Rectangle face = faces.get(i);
                g2d.drawRect(face.x, face.y, face.width, face.height);
            }

            // Desenha informações sobre a detecção
            g2d.setColor(java.awt.Color.WHITE);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
            g2d.drawString("✓ Face detectada", 10, 20);
        } else {
            // Desenha mensagem quando procurando
            g2d.setColor(java.awt.Color.RED);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
            g2d.drawString("Procurando...", 10, 25);
        }

        g2d.dispose();
        return imagemComRetangulos;
    }

    /**
     * Extrai embeddings faciais de uma imagem usando OpenCV
     * Detecta a face, extrai características robustas e serializa em JSON
     * Equivalente ao método Python: serialize_embedding(embedding) -> json.dumps(embedding).encode()
     * 
     * @param imagem BufferedImage da imagem contendo a face
     * @return String JSON com o embedding facial (array de números)
     */
    public String extrairDescritoresFaciais(BufferedImage imagem) {
        if (imagem == null) {
            System.err.println("❌ ERRO: Imagem é NULL ao extrair descritores faciais");
            return null;
        }
        
        try {
            // Converte BufferedImage para Mat do OpenCV
            Mat matImagem = bufferedImageToMat(imagem);
            if (matImagem.empty()) {
                System.err.println("❌ ERRO: Falha ao converter imagem para Mat - Mat está vazio");
                return null;
            }

            // Detecta faces na imagem
            if (!inicializado) {
                inicializar();
            }

            // DNN de reconhecimento é obrigatório para garantir embeddings consistentes
            if (!dnnInicializado || faceRecognitionNet == null || faceRecognitionNet.empty()) {
                System.err.println("❌ ERRO CRÍTICO: Modelo DNN de reconhecimento facial indisponível.");
                System.err.println("   O sistema não pode usar fallback manual para evitar vetores incompatíveis.");
                matImagem.close();
                return null;
            }
            
            List<Rectangle> faces = detectarFacesComCoordenadas(imagem);
            
            if (faces.isEmpty()) {
                System.err.println("❌ ERRO CRÍTICO: Nenhuma face detectada na imagem!");
                System.err.println("   Não é possível extrair descritores faciais sem detectar uma face.");
                System.err.println("   Causas possíveis:");
                System.err.println("     • Rosto não está visível na imagem");
                System.err.println("     • Iluminação inadequada");
                System.err.println("     • Rosto muito pequeno ou muito grande");
                System.err.println("     • Ângulo inadequado do rosto");
                System.err.println("   AÇÃO: Retornando NULL - não é possível fazer reconhecimento sem face detectada.");
                matImagem.close();
                return null;
            }
            
            // Valida qualidade das faces detectadas
            List<Rectangle> facesValidas = new ArrayList<>();
            for (int i = 0; i < faces.size(); i++) {
                Rectangle face = faces.get(i);
                boolean valida = validarQualidadeFace(face, imagem.getWidth(), imagem.getHeight(), i + 1);
                if (valida) {
                    facesValidas.add(face);
                }
            }
            
            if (facesValidas.isEmpty()) {
                System.err.println("❌ ERRO: Faces detectadas mas nenhuma passou na validação de qualidade!");
                System.err.println("   As faces detectadas não atendem aos critérios de qualidade necessários.");
                System.err.println("   AÇÃO: Retornando NULL - não é possível fazer reconhecimento com faces de baixa qualidade.");
                matImagem.close();
                return null;
            }
            
            // Pega a melhor face válida (maior ou mais central)
            Rectangle faceRect = selecionarMelhorFace(facesValidas, imagem.getWidth(), imagem.getHeight());
            
            // Extrai a região da face
            Rect faceRectCV = new Rect(faceRect.x, faceRect.y, faceRect.width, faceRect.height);
            Mat faceRoi = new Mat(matImagem, faceRectCV);

            // Extrai características da face
            List<Double> embedding = extrairEmbeddingFacial(faceRoi);
            
            if (embedding.isEmpty()) {
                System.err.println("❌ ERRO: Embedding está vazio!");
                matImagem.close();
                faceRoi.close();
                faceRectCV.close();
                return null;
            }
            
            // Serializa o embedding em JSON (equivalente a json.dumps(embedding))
            String embeddingJson = serializarEmbedding(embedding);
            
            // Limpa recursos
            matImagem.close();
            faceRoi.close();
            faceRectCV.close();

            return embeddingJson;

        } catch (Exception e) {
            System.err.println("❌ Erro ao extrair descritores faciais: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extrai embedding facial de uma região de face usando OpenCV DNN ou método melhorado
     * Similar ao DeepFace.represent() do Python
     * Garante exatamente 128 dimensões para compatibilidade com modelos de reconhecimento facial
     * 
     * @param faceRoi Mat contendo apenas a região da face
     * @return Lista de doubles representando o embedding (exatamente 128 dimensões)
     */
    private List<Double> extrairEmbeddingFacial(Mat faceRoi) {
        try {
            if (!dnnInicializado || faceRecognitionNet == null || faceRecognitionNet.empty()) {
                throw new IllegalStateException("Modelo DNN não está carregado");
            }
            return extrairEmbeddingComDNN(faceRoi);
        } catch (Exception e) {
            System.err.println("❌ Erro ao extrair embedding facial: " + e.getMessage());
            e.printStackTrace();
        }
        return new ArrayList<>();
    }
    
    /**
     * Extrai embedding usando modelo DNN (FaceNet/OpenFace/ArcFace)
     * Similar ao DeepFace.represent() do Python
     * 
     * @param faceRoi Mat contendo a região da face
     * @return Lista de doubles com o embedding (normalmente 128 ou 512 dimensões)
     */
    private List<Double> extrairEmbeddingComDNN(Mat faceRoi) {
        List<Double> embedding = new ArrayList<>();
        
        try {
            if (faceRecognitionNet == null || faceRecognitionNet.empty()) {
                System.err.println("      ❌ Modelo DNN não está disponível");
                return embedding;
            }
            
            // ETAPA 1: Alinhamento facial baseado em landmarks ANTES do redimensionamento
            Mat faceAligned = alinharFacePorLandmarks(faceRoi);
            if (faceAligned == null || faceAligned.empty()) {
                faceAligned = faceRoi.clone();
            }
            
            // ETAPA 2: Pré-processamento NHWC para FaceNet
            // O faceNet.onnx usado foi exportado em NHWC (TensorFlow). O OpenCV
            // sempre gera blob em NCHW via blobFromImage, e isso causa o erro
            // "Number of input channels should be multiple of 3 but got 160"
            // pois o Transpose [0,3,1,2] interno do modelo lê o blob como
            // [1,160,3,160]. Construímos o blob manualmente em [1,160,160,3].
            Mat faceBase = new Mat();
            opencv_imgproc.resize(faceAligned, faceBase, new Size(160, 160));

            if (faceAligned != faceRoi) {
                faceAligned.close();
            }

            // Otimização FRR: Convertendo para escala de cinza e aplicando CLAHE (Equalização Adaptativa de Histograma)
            // Isso suaviza sombras e melhora o contraste sob diferentes condições de iluminação.
            Mat faceGray = new Mat();
            if (faceBase.channels() == 3) {
                opencv_imgproc.cvtColor(faceBase, faceGray, opencv_imgproc.COLOR_BGR2GRAY);
            } else {
                faceGray = faceBase.clone();
            }

            org.bytedeco.opencv.opencv_imgproc.CLAHE clahe = opencv_imgproc.createCLAHE(2.0, new Size(8, 8));
            Mat faceEqualized = new Mat();
            clahe.apply(faceGray, faceEqualized);

            // Convertendo a imagem equalizada de volta para RGB (3 canais idênticos) esperado pelo FaceNet
            Mat faceRgb = new Mat();
            opencv_imgproc.cvtColor(faceEqualized, faceRgb, opencv_imgproc.COLOR_GRAY2RGB);

            // Liberando recursos temporários do CLAHE
            faceGray.close();
            faceEqualized.close();
            clahe.close();

            // Normalização FaceNet: (pixel - 127.5) / 128
            Mat faceFloat = new Mat();
            faceRgb.convertTo(faceFloat, opencv_core.CV_32F, 1.0 / 128.0, -127.5 / 128.0);

            if (faceFloat.empty()
                    || faceFloat.channels() != 3
                    || faceFloat.rows() != 160
                    || faceFloat.cols() != 160) {
                faceBase.close();
                faceRgb.close();
                faceFloat.close();
                throw new IllegalStateException(
                        "Pré-processamento inválido para FaceNet: esperado HxWxC = 160x160x3");
            }

            // O Mat HWC interleaved (160x160 com 3 canais float) tem o mesmo
            // layout de memória de um blob NHWC [1, 160, 160, 3], então só
            // precisamos reinterpretar a forma — sem cópia nem transpose.
            IntPointer nhwcShape = new IntPointer(new int[]{1, 160, 160, 3});
            Mat blob = faceFloat.reshape(1, 4, nhwcShape);

            if (blob.empty()
                    || blob.dims() != 4
                    || blob.size(0) != 1
                    || blob.size(1) != 160
                    || blob.size(2) != 160
                    || blob.size(3) != 3) {
                faceBase.close();
                faceRgb.close();
                faceFloat.close();
                nhwcShape.close();
                throw new IllegalStateException(
                        "Blob NHWC fora do formato esperado [1,160,160,3]");
            }

            // ETAPA 3: Define entrada do modelo (NHWC).
            faceRecognitionNet.setInput(blob);
            
            // ETAPA 4: Executa inferência (forward pass).
            // Se há um nome de saída detectado, tenta usá-lo; em caso de erro
            // (camada inexistente etc.), cai para forward() padrão antes de
            // declarar incompatibilidade de modelo.
            BytePointer outputLayerNamePtr = outputLayerName != null ? new BytePointer(outputLayerName) : null;
            Mat output;
            if (outputLayerNamePtr != null) {
                try {
                    output = faceRecognitionNet.forward(outputLayerNamePtr);
                } catch (Throwable forwardErr) {
                    System.err.println("      ⚠️ forward(\"" + outputLayerName
                            + "\") falhou (" + forwardErr.getMessage()
                            + "). Usando saída padrão do modelo.");
                    outputLayerNamePtr.close();
                    outputLayerNamePtr = null;
                    outputLayerName = null; // não tenta de novo nas próximas chamadas
                    output = faceRecognitionNet.forward();
                }
            } else {
                output = faceRecognitionNet.forward();
            }
            
            if (output.empty()) {
                System.err.println("      ❌ Erro: Saída do modelo está vazia");
                faceBase.close();
                faceRgb.close();
                faceFloat.close();
                nhwcShape.close();
                if (outputLayerNamePtr != null) outputLayerNamePtr.close();
                throw new IllegalStateException("Erro: Arquivo de modelo incompatível com a estrutura de dados esperada");
            }
            
            // ETAPA 5: Extrai valores do embedding
            // A saída pode estar em diferentes formatos dependendo do modelo
            int embeddingSize = 0;
            if (output.rows() == 1 && output.cols() > 1) {
                // Formato: 1xN (vetor linha)
                embeddingSize = output.cols();
            } else if (output.cols() == 1 && output.rows() > 1) {
                // Formato: Nx1 (vetor coluna)
                embeddingSize = output.rows();
            } else if (output.rows() == 1 && output.cols() == 1 && output.channels() > 1) {
                // Formato: 1x1xN (canal)
                embeddingSize = output.channels();
            } else {
                // Tenta achatamento
                embeddingSize = (int) output.total();
            }
            
            // Converte Mat para array de doubles usando BytePointer
            double[] embeddingArray = new double[embeddingSize];
            Mat flattened = output.reshape(1, embeddingSize);
            
            // Lê os dados do Mat usando BytePointer
            BytePointer dataPtr = flattened.data();
            if (dataPtr != null && !dataPtr.isNull()) {
                // Assume que os dados são floats (CV_32F)
                for (int i = 0; i < embeddingSize; i++) {
                    embeddingArray[i] = dataPtr.getFloat(i * 4); // float = 4 bytes
                }
            } else {
                // Fallback: tenta ler linha por linha
                for (int i = 0; i < Math.min(embeddingSize, output.rows()); i++) {
                    for (int j = 0; j < Math.min(embeddingSize, output.cols()); j++) {
                        if (i * output.cols() + j < embeddingSize) {
                            embeddingArray[i * output.cols() + j] = output.ptr(i, j).getFloat(0);
                        }
                    }
                }
            }
            
            flattened.close();
            if (outputLayerNamePtr != null) outputLayerNamePtr.close();
            
            // ETAPA 6: Normaliza o embedding (L2 normalization)
            // Importante para comparação usando cosine similarity
            double norm = 0.0;
            for (double val : embeddingArray) {
                norm += val * val;
            }
            norm = Math.sqrt(norm);
            
            if (norm > 0.0001) { // Evita divisão por zero
                for (int i = 0; i < embeddingArray.length; i++) {
                    embeddingArray[i] = embeddingArray[i] / norm;
                }
            }
            
            // Converte para lista
            for (double val : embeddingArray) {
                embedding.add(val);
            }
            
            // Se o embedding não tiver 128 dimensões, ajusta ou trunca
            if (embedding.size() != EMBEDDING_SIZE) {
                if (embedding.size() > EMBEDDING_SIZE) {
                    // Trunca para 128
                    embedding = embedding.subList(0, EMBEDDING_SIZE);
                } else {
                    // Preenche com zeros (não ideal, mas mantém compatibilidade)
                    while (embedding.size() < EMBEDDING_SIZE) {
                        embedding.add(0.0);
                    }
                }
            }
            
            // Limpa recursos
            faceBase.close();
            faceRgb.close();
            faceFloat.close();
            nhwcShape.close();
            output.close();

            return embedding;
            
        } catch (Exception e) {
            System.err.println("      ❌ Erro ao extrair embedding com DNN: " + e.getMessage());
            throw new IllegalStateException("Erro: Arquivo de modelo incompatível com a estrutura de dados esperada", e);
        }
    }
    
    /**
     * Método melhorado de extração de embedding baseado em características robustas
     * Similar ao DeepFace mas usando técnicas OpenCV avançadas
     * PRÉ-PROCESSAMENTO PADRONIZADO: Este método garante que o pré-processamento
     * é idêntico tanto no cadastro quanto na consulta
     */
    private List<Double> extrairEmbeddingMelhorado(Mat faceRoi) {
        List<Double> embedding = new ArrayList<>();

        try {
            System.out.println("      [Pré-processamento] Iniciando pré-processamento padronizado...");
            System.out.println("         Face ROI original: " + faceRoi.rows() + "x" + faceRoi.cols());
            
            // ETAPA 1: Alinhamento facial baseado em landmarks ANTES do redimensionamento
            System.out.println("      [Alinhamento] Alinhando face baseado em landmarks...");
            Mat faceAligned = alinharFacePorLandmarks(faceRoi);
            if (faceAligned == null || faceAligned.empty()) {
                System.out.println("         ⚠️ Alinhamento falhou, usando face original");
                faceAligned = faceRoi.clone();
            } else {
                System.out.println("         ✅ Face alinhada: " + faceAligned.rows() + "x" + faceAligned.cols());
            }
            
            // ETAPA 2: Normaliza o tamanho da face para o tamanho padrão (96x96 similar ao OpenFace)
            // IMPORTANTE: Este tamanho DEVE ser o mesmo no cadastro e na consulta
            Mat faceNormalizada = new Mat();
            opencv_imgproc.resize(faceAligned, faceNormalizada, new Size(FACE_INPUT_SIZE, FACE_INPUT_SIZE));
            System.out.println("         ✅ Redimensionado para: " + FACE_INPUT_SIZE + "x" + FACE_INPUT_SIZE);
            
            // Limpa faceAligned se foi criado
            if (faceAligned != faceRoi) {
                faceAligned.close();
            }

            // ETAPA 2: Converte para escala de cinza
            // IMPORTANTE: Sempre converter para grayscale antes de processar
            Mat grayFace = new Mat();
            if (faceNormalizada.channels() == 3) {
                opencv_imgproc.cvtColor(faceNormalizada, grayFace, opencv_imgproc.COLOR_BGR2GRAY);
                System.out.println("         ✅ Convertido para escala de cinza (3 canais -> 1 canal)");
            } else {
                grayFace = faceNormalizada.clone();
                System.out.println("         ✅ Já estava em escala de cinza");
            }
            
            // ETAPA 3: Aplica equalização de histograma para melhorar contraste
            // IMPORTANTE: Esta etapa melhora a qualidade do embedding
            opencv_imgproc.equalizeHist(grayFace, grayFace);
            System.out.println("         ✅ Equalização de histograma aplicada");

            // ETAPA 4: Extrai características robustas usando múltiplas técnicas
            // IMPORTANTE: A ordem e os parâmetros DEVE ser idêntica no cadastro e na consulta
            // Distribuição otimizada para 128 dimensões baseada em pesquisas de reconhecimento facial
            System.out.println("      [Extração de Características] Iniciando extração...");
            
            // 1. Histograma de orientação de gradientes (HOG-like): 64 dimensões
            System.out.println("         Extraindo HOG features (64 dimensões)...");
            embedding.addAll(extrairHOGFeatures(grayFace, 64));

            // 2. Características LBP melhoradas: 32 dimensões
            System.out.println("         Extraindo LBP features (32 dimensões)...");
            embedding.addAll(extrairCaracteristicasLBP128(grayFace, 32));

            // 3. Características de textura e forma: 32 dimensões
            System.out.println("         Extraindo características de textura (32 dimensões)...");
            embedding.addAll(extrairCaracteristicasTextura(grayFace, 32));
            
            System.out.println("         Total de características extraídas: " + embedding.size() + " dimensões");

            // ETAPA 5: Garante exatamente 128 dimensões
            System.out.println("      [Ajuste de Dimensões] Ajustando para exatamente " + EMBEDDING_SIZE + " dimensões...");
            ajustarPara128Dimensoes(embedding);
            System.out.println("         ✅ Dimensões após ajuste: " + embedding.size());

            // ETAPA 6: Normaliza o embedding (L2 normalization) - essencial para comparação
            // IMPORTANTE: A normalização DEVE ser aplicada sempre, tanto no cadastro quanto na consulta
            System.out.println("      [Normalização] Aplicando normalização L2...");
            normalizarEmbedding(embedding);
            
            // Verifica a norma após normalização
            double norma = 0.0;
            for (Double val : embedding) {
                norma += val * val;
            }
            norma = Math.sqrt(norma);
            System.out.println("         ✅ Normalização L2 aplicada (norma: " + String.format("%.6f", norma) + ")");

            // Limpa recursos
            faceNormalizada.close();
            grayFace.close();

        } catch (Exception e) {
            System.err.println("❌ Erro ao extrair embedding melhorado: " + e.getMessage());
            e.printStackTrace();
            // Em caso de erro, retorna embedding vazio de 128 zeros
            while (embedding.size() < EMBEDDING_SIZE) {
                embedding.add(0.0);
            }
        }

        return embedding;
    }
    
    /**
     * Extrai características tipo HOG (Histogram of Oriented Gradients)
     */
    private List<Double> extrairHOGFeatures(Mat imagem, int numFeatures) {
        List<Double> hogFeatures = new ArrayList<>();
        
        try {
            int cellSize = 8;
            int blockSize = 16;
            int numBins = 9;
            int rows = imagem.rows();
            int cols = imagem.cols();
            
            BytePointer dataPtr = imagem.data();
            int step = (int) imagem.step();
            int channels = imagem.channels();
            
            // Calcula gradientes e agrupa em células diretamente
            int cellsY = rows / cellSize;
            int cellsX = cols / cellSize;
            
            for (int cy = 0; cy < cellsY && hogFeatures.size() < numFeatures; cy++) {
                for (int cx = 0; cx < cellsX && hogFeatures.size() < numFeatures; cx++) {
                    double[] hist = new double[numBins];
                    
                    int startY = Math.max(1, cy * cellSize);
                    int endY = Math.min((cy + 1) * cellSize, rows - 1);
                    int startX = Math.max(1, cx * cellSize);
                    int endX = Math.min((cx + 1) * cellSize, cols - 1);
                    
                    for (int y = startY; y < endY; y++) {
                        for (int x = startX; x < endX; x++) {
                            // Gradiente horizontal
                            long offset1 = y * step + (x - 1) * channels;
                            long offset2 = y * step + (x + 1) * channels;
                            byte p1 = dataPtr.get(offset1);
                            byte p2 = dataPtr.get(offset2);
                            double gx = ((p2 & 0xFF) - (p1 & 0xFF)) / 255.0;
                            
                            // Gradiente vertical
                            offset1 = (y - 1) * step + x * channels;
                            offset2 = (y + 1) * step + x * channels;
                            p1 = dataPtr.get(offset1);
                            p2 = dataPtr.get(offset2);
                            double gy = ((p2 & 0xFF) - (p1 & 0xFF)) / 255.0;
                            
                            // Magnitude e orientação
                            double mag = Math.sqrt(gx * gx + gy * gy);
                            double angle = Math.atan2(gy, gx) * 180.0 / Math.PI;
                            if (angle < 0) angle += 360;
                            
                            int bin = (int) ((angle / 360.0) * numBins);
                            if (bin >= numBins) bin = numBins - 1;
                            hist[bin] += mag;
                        }
                    }
                    
                    // Normaliza o histograma da célula e adiciona às features
                    double sum = 0;
                    for (double h : hist) sum += h;
                    if (sum > 0) {
                        for (double h : hist) {
                            if (hogFeatures.size() < numFeatures) {
                                hogFeatures.add(h / sum);
                            }
                        }
                    } else {
                        // Se não houver gradientes, adiciona zeros
                        for (int i = 0; i < numBins && hogFeatures.size() < numFeatures; i++) {
                            hogFeatures.add(0.0);
                        }
                    }
                }
            }
            
            // Preenche se necessário
            while (hogFeatures.size() < numFeatures) {
                hogFeatures.add(0.0);
            }
            
            // Trunca se necessário
            while (hogFeatures.size() > numFeatures) {
                hogFeatures.remove(hogFeatures.size() - 1);
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao extrair HOG features: " + e.getMessage());
            while (hogFeatures.size() < numFeatures) {
                hogFeatures.add(0.0);
            }
        }
        
        return hogFeatures;
    }
    
    /**
     * Extrai características de textura avançadas
     */
    private List<Double> extrairCaracteristicasTextura(Mat imagem, int numFeatures) {
        List<Double> texturaFeatures = new ArrayList<>();
        
        try {
            // Combina estatísticas regionais, gradientes e características de forma
            texturaFeatures.addAll(extrairEstatisticasDetalhadas(imagem, numFeatures / 2));
            texturaFeatures.addAll(extrairGradientesDetalhados(imagem, numFeatures / 2));
            
            // Ajusta para ter exatamente numFeatures
            while (texturaFeatures.size() < numFeatures) {
                texturaFeatures.add(0.0);
            }
            while (texturaFeatures.size() > numFeatures) {
                texturaFeatures.remove(texturaFeatures.size() - 1);
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao extrair características de textura: " + e.getMessage());
            while (texturaFeatures.size() < numFeatures) {
                texturaFeatures.add(0.0);
            }
        }
        
        return texturaFeatures;
    }
    
    /**
     * Ajusta o embedding para ter exatamente 128 dimensões
     * Se tiver mais, trunca; se tiver menos, preenche com valores baseados na média
     */
    private void ajustarPara128Dimensoes(List<Double> embedding) {
        int targetSize = EMBEDDING_SIZE;
        int currentSize = embedding.size();
        
        if (currentSize == targetSize) {
            return; // Já tem o tamanho correto
        }
        
        if (currentSize > targetSize) {
            // Trunca para 128 dimensões (mantém as primeiras 128)
            while (embedding.size() > targetSize) {
                embedding.remove(embedding.size() - 1);
            }
        } else {
            // Calcula média e desvio padrão dos valores existentes
            double soma = 0.0;
            double somaQuadrados = 0.0;
            for (Double val : embedding) {
                soma += val;
                somaQuadrados += val * val;
            }
            double media = embedding.isEmpty() ? 0.0 : soma / embedding.size();
            double variancia = embedding.isEmpty() ? 0.0 : (somaQuadrados / embedding.size()) - (media * media);
            double desvioPadrao = Math.sqrt(Math.max(0, variancia));
            
            // Preenche com valores baseados na distribuição existente (ruído gaussiano suave)
            java.util.Random random = new java.util.Random();
            while (embedding.size() < targetSize) {
                // Adiciona valores próximos à média com pequena variação
                double valor = media + (random.nextGaussian() * desvioPadrao * 0.1);
                embedding.add(valor);
            }
        }
    }
    
    /**
     * Extrai características LBP ajustadas para contribuir com dimensões específicas
     * 
     * @param imagem Mat da imagem em escala de cinza
     * @param numFeatures Número exato de características a extrair
     * @return Lista com exatamente numFeatures características LBP
     */
    private List<Double> extrairCaracteristicasLBP128(Mat imagem, int numFeatures) {
        List<Double> lbpFeatures = new ArrayList<>();
        final int targetFeatures = numFeatures; // Final para usar no final do método
        
        try {
            int blockSize = 8; // Blocos menores para mais características
            int rows = imagem.rows();
            int cols = imagem.cols();
            
            BytePointer dataPtr = imagem.data();
            int step = (int) imagem.step();
            int channels = imagem.channels();
            
            List<Double> tempFeatures = new ArrayList<>();
            
            for (int y = 0; y < rows - blockSize; y += blockSize) {
                for (int x = 0; x < cols - blockSize; x += blockSize) {
                    double soma = 0.0;
                    double somaQuadrados = 0.0;
                    double min = Double.MAX_VALUE;
                    double max = Double.MIN_VALUE;
                    int count = 0;

                    for (int by = y; by < Math.min(y + blockSize, rows); by++) {
                        for (int bx = x; bx < Math.min(x + blockSize, cols); bx++) {
                            long offset = by * step + bx * channels;
                            byte intensity = dataPtr.get(offset);
                            double intValue = (intensity & 0xFF) / 255.0;
                            soma += intValue;
                            somaQuadrados += intValue * intValue;
                            min = Math.min(min, intValue);
                            max = Math.max(max, intValue);
                            count++;
                        }
                    }

                    if (count > 0) {
                        double media = soma / count;
                        double variancia = (somaQuadrados / count) - (media * media);
                        double desvioPadrao = Math.sqrt(Math.max(0, variancia));
                        
                        tempFeatures.add(media);
                        tempFeatures.add(desvioPadrao);
                        tempFeatures.add((max - min)); // Range
                    }
                }
            }
            
            // Ajusta para ter aproximadamente o número de características desejado
            if (tempFeatures.size() >= targetFeatures) {
                // Seleciona características distribuídas uniformemente
                double stepSize = (double) tempFeatures.size() / targetFeatures;
                for (int i = 0; i < targetFeatures; i++) {
                    int index = (int) (i * stepSize);
                    if (index < tempFeatures.size()) {
                        lbpFeatures.add(tempFeatures.get(index));
                    }
                }
            } else {
                // Se não tiver o suficiente, usa todas e preenche com média
                lbpFeatures.addAll(tempFeatures);
                double media = 0.0;
                if (!tempFeatures.isEmpty()) {
                    for (Double val : tempFeatures) {
                        media += val;
                    }
                    media /= tempFeatures.size();
                }
                while (lbpFeatures.size() < targetFeatures) {
                    lbpFeatures.add(media);
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao extrair características LBP128: " + e.getMessage());
            e.printStackTrace();
        }
        
        // Garante que tem exatamente o número esperado
        while (lbpFeatures.size() < targetFeatures) {
            lbpFeatures.add(0.0);
        }
        while (lbpFeatures.size() > targetFeatures) {
            lbpFeatures.remove(lbpFeatures.size() - 1);
        }

        return lbpFeatures;
    }
    
    /**
     * Extrai características de gradientes detalhadas
     */
    private List<Double> extrairGradientesDetalhados(Mat imagem, int numFeatures) {
        List<Double> gradientes = new ArrayList<>();
        
        try {
            int rows = imagem.rows();
            int cols = imagem.cols();
            BytePointer dataPtr = imagem.data();
            int step = (int) imagem.step();
            int channels = imagem.channels();
            
            List<Double> gradientesX = new ArrayList<>();
            List<Double> gradientesY = new ArrayList<>();
            List<Double> magnitudes = new ArrayList<>();

            // Calcula gradientes em diferentes regiões
            int regionSize = Math.max(16, Math.min(rows, cols) / 4);
            
            for (int regionY = 0; regionY < rows; regionY += regionSize) {
                for (int regionX = 0; regionX < cols; regionX += regionSize) {
                    double somaGradX = 0.0;
                    double somaGradY = 0.0;
                    double somaMagnitude = 0.0;
                    int count = 0;
                    
                    for (int y = Math.max(1, regionY); y < Math.min(rows - 1, regionY + regionSize); y++) {
                        for (int x = Math.max(1, regionX); x < Math.min(cols - 1, regionX + regionSize); x++) {
                            // Gradiente horizontal
                            long offset1 = y * step + (x - 1) * channels;
                            long offset2 = y * step + (x + 1) * channels;
                            byte pixel1 = dataPtr.get(offset1);
                            byte pixel2 = dataPtr.get(offset2);
                            double gradX = ((pixel2 & 0xFF) - (pixel1 & 0xFF)) / 255.0;

                            // Gradiente vertical
                            offset1 = (y - 1) * step + x * channels;
                            offset2 = (y + 1) * step + x * channels;
                            pixel1 = dataPtr.get(offset1);
                            pixel2 = dataPtr.get(offset2);
                            double gradY = ((pixel2 & 0xFF) - (pixel1 & 0xFF)) / 255.0;

                            // Magnitude do gradiente
                            double magnitude = Math.sqrt(gradX * gradX + gradY * gradY);
                            
                            somaGradX += Math.abs(gradX);
                            somaGradY += Math.abs(gradY);
                            somaMagnitude += magnitude;
                            count++;
                        }
                    }
                    
                    if (count > 0) {
                        gradientesX.add(somaGradX / count);
                        gradientesY.add(somaGradY / count);
                        magnitudes.add(somaMagnitude / count);
                    }
                }
            }
            
            // Seleciona características para totalizar numFeatures
            int featuresPerType = Math.max(1, numFeatures / 3);
            
            // Adiciona gradientes X
            for (int i = 0; i < Math.min(featuresPerType, gradientesX.size()); i++) {
                gradientes.add(gradientesX.get(i));
            }
            
            // Adiciona gradientes Y
            for (int i = 0; i < Math.min(featuresPerType, gradientesY.size()); i++) {
                gradientes.add(gradientesY.get(i));
            }
            
            // Adiciona magnitudes
            int remaining = numFeatures - gradientes.size();
            for (int i = 0; i < Math.min(remaining, magnitudes.size()); i++) {
                gradientes.add(magnitudes.get(i));
            }
            
            // Preenche se necessário
            while (gradientes.size() < numFeatures) {
                gradientes.add(0.0);
            }
            
            // Trunca se necessário
            while (gradientes.size() > numFeatures) {
                gradientes.remove(gradientes.size() - 1);
            }

        } catch (Exception e) {
            System.err.println("Erro ao extrair gradientes detalhados: " + e.getMessage());
            e.printStackTrace();
            while (gradientes.size() < numFeatures) {
                gradientes.add(0.0);
            }
        }

        return gradientes;
    }
    
    /**
     * Extrai estatísticas detalhadas da imagem
     */
    private List<Double> extrairEstatisticasDetalhadas(Mat imagem, int numFeatures) {
        List<Double> estatisticas = new ArrayList<>();
        
        try {
            int rows = imagem.rows();
            int cols = imagem.cols();
            BytePointer dataPtr = imagem.data();
            int step = (int) imagem.step();
            int channels = imagem.channels();
            
            // Divide a imagem em regiões e extrai estatísticas de cada região
            int numRegions = Math.max(4, (int) Math.sqrt(numFeatures / 4));
            int regionHeight = rows / numRegions;
            int regionWidth = cols / numRegions;
            
            for (int ry = 0; ry < numRegions; ry++) {
                for (int rx = 0; rx < numRegions; rx++) {
                    double soma = 0.0;
                    double somaQuadrados = 0.0;
                    double min = Double.MAX_VALUE;
                    double max = Double.MIN_VALUE;
                    int count = 0;
                    
                    int startY = ry * regionHeight;
                    int endY = Math.min((ry + 1) * regionHeight, rows);
                    int startX = rx * regionWidth;
                    int endX = Math.min((rx + 1) * regionWidth, cols);
                    
                    for (int y = startY; y < endY; y++) {
                        for (int x = startX; x < endX; x++) {
                            long offset = y * step + x * channels;
                            byte intensity = dataPtr.get(offset);
                            double intValue = (intensity & 0xFF) / 255.0;
                            soma += intValue;
                            somaQuadrados += intValue * intValue;
                            min = Math.min(min, intValue);
                            max = Math.max(max, intValue);
                            count++;
                        }
                    }
                    
                    if (count > 0) {
                        double media = soma / count;
                        double variancia = (somaQuadrados / count) - (media * media);
                        double desvioPadrao = Math.sqrt(Math.max(0, variancia));
                        double range = max - min;
                        
                        estatisticas.add(media);
                        estatisticas.add(desvioPadrao);
                        estatisticas.add(range);
                        estatisticas.add((max + min) / 2.0); // Midpoint
                    }
                }
            }
            
            // Ajusta para ter exatamente numFeatures
            while (estatisticas.size() < numFeatures) {
                estatisticas.add(0.0);
            }
            while (estatisticas.size() > numFeatures) {
                estatisticas.remove(estatisticas.size() - 1);
            }

        } catch (Exception e) {
            System.err.println("Erro ao extrair estatísticas detalhadas: " + e.getMessage());
            e.printStackTrace();
            while (estatisticas.size() < numFeatures) {
                estatisticas.add(0.0);
            }
        }

        return estatisticas;
    }

    /**
     * Extrai histograma da imagem usando análise de pixels
     */
    private List<Double> extrairHistograma(Mat imagem, int bins) {
        List<Double> histograma = new ArrayList<>();
        
        try {
            int rows = imagem.rows();
            int cols = imagem.cols();
            int[] histCounts = new int[bins];
            int totalPixels = rows * cols;

            // Acessa os dados da imagem usando BytePointer
            BytePointer dataPtr = imagem.data();
            int step = (int) imagem.step();
            int channels = imagem.channels();

            // Lê os valores dos pixels e conta no histograma
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    // Calcula offset do pixel
                    long offset = y * step + x * channels;
                    byte intensity = dataPtr.get(offset);
                    int intValue = intensity & 0xFF;
                    int bin = (int) ((intValue / 256.0) * bins);
                    if (bin >= bins) bin = bins - 1;
                    histCounts[bin]++;
                }
            }

            // Normaliza o histograma
            for (int i = 0; i < bins; i++) {
                histograma.add((double) histCounts[i] / totalPixels);
            }

        } catch (Exception e) {
            System.err.println("Erro ao extrair histograma: " + e.getMessage());
            e.printStackTrace();
            // Preenche com zeros se houver erro
            for (int i = 0; i < bins; i++) {
                histograma.add(0.0);
            }
        }

        return histograma;
    }

    /**



    /**
     * Extrai estatísticas de intensidade (média e desvio padrão)
     */
    private List<Double> extrairEstatisticasIntensidade(Mat imagem) {
        List<Double> estatisticas = new ArrayList<>();
        
        try {
            int rows = imagem.rows();
            int cols = imagem.cols();
            BytePointer dataPtr = imagem.data();
            int step = (int) imagem.step();
            int channels = imagem.channels();

            double soma = 0.0;
            double somaQuadrados = 0.0;
            int count = rows * cols;

            // Calcula média e desvio padrão manualmente
            for (int y = 0; y < rows; y++) {
                for (int x = 0; x < cols; x++) {
                    long offset = y * step + x * channels;
                    byte intensity = dataPtr.get(offset);
                    double intValue = (intensity & 0xFF) / 255.0;
                    soma += intValue;
                    somaQuadrados += intValue * intValue;
                }
            }

            if (count > 0) {
                double media = soma / count;
                double variancia = (somaQuadrados / count) - (media * media);
                double desvioPadrao = Math.sqrt(Math.max(0, variancia));

                estatisticas.add(media);
                estatisticas.add(desvioPadrao);
            } else {
                estatisticas.add(0.0);
                estatisticas.add(0.0);
            }

        } catch (Exception e) {
            System.err.println("Erro ao extrair estatísticas: " + e.getMessage());
            e.printStackTrace();
            estatisticas.add(0.0);
            estatisticas.add(0.0);
        }

        return estatisticas;
    }

    /**
     * Extrai características básicas da imagem quando nenhuma face é detectada
     */
    private String extrairCaracteristicasImagem(Mat imagem) {
        try {
            Mat gray = new Mat();
            if (imagem.channels() == 3) {
                opencv_imgproc.cvtColor(imagem, gray, opencv_imgproc.COLOR_BGR2GRAY);
            } else {
                gray = imagem.clone();
            }

            List<Double> embedding = new ArrayList<>();
            embedding.addAll(extrairHistograma(gray, 64));
            embedding.addAll(extrairEstatisticasIntensidade(gray));

            gray.close();

            return serializarEmbedding(embedding);

        } catch (Exception e) {
            System.err.println("Erro ao extrair características da imagem: " + e.getMessage());
            return "[]";
        }
    }

    /**
     * Seleciona a melhor face detectada (maior ou mais central)
     */
    private Rectangle selecionarMelhorFace(List<Rectangle> faces, int imageWidth, int imageHeight) {
        if (faces.size() == 1) {
            return faces.get(0);
        }

        // Seleciona a face maior ou mais central
        Rectangle melhorFace = faces.get(0);
        int centroX = imageWidth / 2;
        int centroY = imageHeight / 2;
        double menorDistancia = Double.MAX_VALUE;

        for (Rectangle face : faces) {
            int faceCentroX = face.x + face.width / 2;
            int faceCentroY = face.y + face.height / 2;
            double distancia = Math.sqrt(
                Math.pow(faceCentroX - centroX, 2) + Math.pow(faceCentroY - centroY, 2)
            );

            if (distancia < menorDistancia) {
                menorDistancia = distancia;
                melhorFace = face;
            }
        }

        return melhorFace;
    }

    /**
     * Normaliza o embedding usando L2 normalization
     */
    private void normalizarEmbedding(List<Double> embedding) {
        double somaQuadrados = 0.0;
        for (Double val : embedding) {
            somaQuadrados += val * val;
        }

        double norma = Math.sqrt(somaQuadrados);
        if (norma > 0.0001) {
            for (int i = 0; i < embedding.size(); i++) {
                embedding.set(i, embedding.get(i) / norma);
            }
        }
    }

    /**
     * Serializa embedding para JSON string com alta precisão
     * Equivalente ao método Python: json.dumps(embedding)
     * Preserva TODA a precisão dos números double (máxima precisão possível)
     * 
     * @param embedding Lista de doubles representando o embedding
     * @return String JSON com o embedding (preservando toda a precisão decimal)
     */
    private String serializarEmbedding(List<Double> embedding) {
        if (embedding == null || embedding.isEmpty()) {
            return "[]";
        }

        try {
            // Usa StringBuilder para construir o JSON manualmente
            // Isso garante que TODA a precisão dos números seja preservada
            StringBuilder jsonBuilder = new StringBuilder();
            jsonBuilder.append("[");
            
            for (int i = 0; i < embedding.size(); i++) {
                if (i > 0) {
                    jsonBuilder.append(",");
                }
                
                Double value = embedding.get(i);
                if (value == null) {
                    jsonBuilder.append("0.0");
                } else {
                    // Usa Double.toString() que preserva toda a precisão do double
                    // Isso garante que números como -0.9435049295425415 sejam preservados exatamente
                    jsonBuilder.append(value.toString());
                }
            }
            
            jsonBuilder.append("]");
            return jsonBuilder.toString();
        } catch (Exception e) {
            System.err.println("❌ Erro ao serializar embedding: " + e.getMessage());
            e.printStackTrace();
            return "[]";
        }
    }

    /**
     * Converte BufferedImage para byte[] para armazenamento no banco (SQLite compatível)
     * Este método é preferido para SQLite, que não suporta createBlob()
     * Usa compressão JPEG otimizada para reduzir o tamanho do BLOB
     * 
     * NOTA: SQLite via JDBC usa setBytes() que já escreve BLOBs eficientemente.
     * A interface C sqlite3_blob_write() não é necessária quando usando JDBC.
     */
    public byte[] imagemParaBytes(BufferedImage imagem) throws SQLException {
        if (imagem == null) {
            throw new SQLException("Imagem é NULL");
        }
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Usa ImageWriter com compressão otimizada para reduzir tamanho do BLOB
            javax.imageio.ImageWriter writer = null;
            javax.imageio.ImageWriteParam param = null;
            
            try {
                // Obtém o writer para JPEG
                java.util.Iterator<javax.imageio.ImageWriter> writers = 
                    ImageIO.getImageWritersByFormatName("jpg");
                
                if (writers.hasNext()) {
                    writer = writers.next();
                    param = writer.getDefaultWriteParam();
                    
                    // Habilita compressão com qualidade ajustável (0.7 = 70% qualidade)
                    // Balanceia qualidade vs tamanho do arquivo
                    if (param.canWriteCompressed()) {
                        param.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
                        param.setCompressionQuality(0.75f); // 75% de qualidade (boa qualidade, tamanho reduzido)
                    }
                    
                    // Configura o output
                    writer.setOutput(ImageIO.createImageOutputStream(baos));
                    
                    // Escreve a imagem com compressão
                    writer.write(null, new javax.imageio.IIOImage(imagem, null, null), param);
                    writer.dispose();
                } else {
                    // Fallback: usa ImageIO.write() padrão se não conseguir usar ImageWriter
                    boolean written = ImageIO.write(imagem, "jpg", baos);
                    if (!written) {
                        throw new SQLException("Falha ao converter imagem para JPG");
                    }
                }
            } catch (IOException e) {
                // Fallback: tenta método simples se o método otimizado falhar
                System.out.println("  ⚠️ Fallback para método simples de compressão");
                baos.reset();
                boolean written = ImageIO.write(imagem, "jpg", baos);
                if (!written) {
                    throw new SQLException("Falha ao converter imagem para JPG: " + e.getMessage());
                }
            } finally {
                if (writer != null) {
                    writer.dispose();
                }
            }
            
            byte[] imageBytes = baos.toByteArray();
            System.out.println("  - Bytes da imagem (comprimida): " + imageBytes.length + 
                             " bytes (" + String.format("%.1f", imageBytes.length / 1024.0) + " KB)");
            
            if (imageBytes.length == 0) {
                throw new SQLException("Imagem convertida está vazia (0 bytes)");
            }
            
            // SQLite via JDBC usa setBytes() que escreve o BLOB eficientemente
            // Não é necessário usar sqlite3_blob_write() da interface C quando usando JDBC
            return imageBytes;

        } catch (IOException e) {
            throw new SQLException("Erro ao converter imagem para byte[]: " + e.getMessage(), e);
        }
    }
    
    /**
     * Converte BufferedImage para Blob para armazenamento no banco
     * IMPORTANTE: O Blob criado deve ser usado na mesma conexão que o criou
     * NOTA: SQLite não suporta createBlob(), use imagemParaBytes() para SQLite
     */
    public Blob imagemParaBlob(BufferedImage imagem, Connection connection) throws SQLException {
        // Para SQLite, tenta usar byte[] diretamente
        try {
            byte[] imageBytes = imagemParaBytes(imagem);
            
            // Tenta criar Blob, mas se falhar (SQLite), retorna null e o código deve usar byte[] diretamente
            try {
                Blob blob = connection.createBlob();
                blob.setBytes(1, imageBytes);
                System.out.println("  - BLOB criado: " + blob.length() + " bytes");
                return blob;
            } catch (SQLException e) {
                // SQLite não suporta createBlob(), então retorna null
                // O código chamador deve usar imagemParaBytes() diretamente
                System.out.println("  - Aviso: createBlob() não suportado (SQLite), use imagemParaBytes()");
                return null;
            }
        } catch (SQLException e) {
            throw e;
        }
    }

    /**
     * Converte byte[] para BufferedImage (SQLite compatível)
     * Este método é preferido para SQLite
     */
    public BufferedImage bytesParaImagem(byte[] bytes) throws SQLException {
        if (bytes == null || bytes.length == 0) {
            throw new SQLException("Bytes da imagem são NULL ou vazios");
        }
        
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            BufferedImage imagem = ImageIO.read(bais);
            if (imagem == null) {
                throw new SQLException("Falha ao decodificar imagem dos bytes");
            }
            return imagem;
        } catch (IOException e) {
            throw new SQLException("Erro ao converter byte[] para imagem: " + e.getMessage(), e);
        }
    }
    
    /**
     * Converte Blob para BufferedImage
     * NOTA: Para SQLite, use bytesParaImagem() diretamente com byte[]
     */
    public BufferedImage blobParaImagem(Blob blob) throws SQLException {
        try {
            byte[] bytes = blob.getBytes(1, (int) blob.length());
            return bytesParaImagem(bytes);
        } catch (SQLException e) {
            throw new SQLException("Erro ao converter Blob para imagem: " + e.getMessage(), e);
        }
    }

    /**
     * Salva imagem em arquivo (para debug)
     */
    public void salvarImagem(BufferedImage imagem, String caminho) {
        try {
            ImageIO.write(imagem, "jpg", new java.io.File(caminho));
        } catch (IOException e) {
            System.err.println("Erro ao salvar imagem: " + e.getMessage());
        }
    }

    /**
     * Verifica se uma imagem contém um rosto
     */
    public boolean contemRosto(BufferedImage imagem) {
        return detectarFace(imagem);
    }

    /**
     * @deprecated Este método antigo baseado em correlação de pixels foi substituído
     * por extração de embeddings DNN e comparação usando cosine similarity.
     * Use extrairDescritoresFaciais() e comparar embeddings no DadosFaciaisDAO.
     * 
     * Calcula similaridade entre duas imagens usando embeddings faciais modernos.
     * Este método é mantido apenas para compatibilidade, mas não é recomendado.
     */
    @Deprecated
    public double calcularSimilaridade(BufferedImage img1, BufferedImage img2) {
        if (img1 == null || img2 == null) {
            return 0.0;
        }

        try {
            // Método moderno: extrai embeddings e compara usando cosine similarity
            String descritores1 = extrairDescritoresFaciais(img1);
            String descritores2 = extrairDescritoresFaciais(img2);
            
            if (descritores1 == null || descritores2 == null || 
                descritores1.equals("[]") || descritores2.equals("[]")) {
                return 0.0;
            }
            
            // Usa o mesmo método de cálculo de similaridade do DAO
            return calcularSimilaridadeEmbeddings(descritores1, descritores2);

        } catch (Exception e) {
            System.err.println("Erro ao calcular similaridade: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Calcula similaridade entre dois embeddings usando cosine similarity
     * Método moderno e recomendado para comparação de embeddings faciais
     */
    private double calcularSimilaridadeEmbeddings(String descritores1, String descritores2) {
        try {
            if (descritores1 == null || descritores2 == null || 
                descritores1.isEmpty() || descritores2.isEmpty() ||
                descritores1.equals("[]") || descritores2.equals("[]")) {
                return 0.0;
            }

            // Converte strings JSON para arrays de double
            String clean1 = descritores1.trim().replaceAll("^\\[|\\]$", "").trim();
            String clean2 = descritores2.trim().replaceAll("^\\[|\\]$", "").trim();
            
            String[] valores1 = clean1.isEmpty() ? new String[0] : clean1.split(",\\s*");
            String[] valores2 = clean2.isEmpty() ? new String[0] : clean2.split(",\\s*");

            if (valores1.length != valores2.length || valores1.length == 0) {
                return 0.0;
            }

            // Calcula cosine similarity (melhor para embeddings normalizados)
            double produtoEscalar = 0.0;
            double norma1 = 0.0;
            double norma2 = 0.0;

            for (int i = 0; i < valores1.length; i++) {
                try {
                    double v1 = Double.parseDouble(valores1[i].trim());
                    double v2 = Double.parseDouble(valores2[i].trim());
                    
                    produtoEscalar += v1 * v2;
                    norma1 += v1 * v1;
                    norma2 += v2 * v2;
                } catch (NumberFormatException e) {
                    continue;
                }
            }

            double denominador = Math.sqrt(norma1) * Math.sqrt(norma2);
            
            if (denominador == 0.0) {
                return 0.0;
            }

            double similaridade = produtoEscalar / denominador;
            return Math.max(0.0, Math.min(1.0, similaridade));

        } catch (Exception e) {
            System.err.println("Erro ao calcular similaridade de embeddings: " + e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * Alinha a face baseado em landmarks (olhos) antes do redimensionamento
     * Isso melhora significativamente a precisão do reconhecimento facial
     * 
     * @param faceRoi Mat contendo a região da face
     * @return Mat com a face alinhada, ou null se falhar
     */
    private Mat alinharFacePorLandmarks(Mat faceRoi) {
        try {
            if (faceRoi == null || faceRoi.empty()) {
                return null;
            }
            
            // Converte para escala de cinza para detecção de olhos
            Mat grayFace = new Mat();
            if (faceRoi.channels() == 3) {
                opencv_imgproc.cvtColor(faceRoi, grayFace, opencv_imgproc.COLOR_BGR2GRAY);
            } else {
                grayFace = faceRoi.clone();
            }
            
            // Detecta olhos na face
            RectVector eyes = new RectVector();
            org.bytedeco.opencv.opencv_core.Point2f leftEye = null;
            org.bytedeco.opencv.opencv_core.Point2f rightEye = null;
            
            if (eyeDetector != null && !eyeDetector.empty()) {
                // Tenta detectar olhos usando o detector
                eyeDetector.detectMultiScale(grayFace, eyes, 1.1, 3, 0,
                    new Size(grayFace.cols() / 10, grayFace.rows() / 10),
                    new Size(grayFace.cols() / 2, grayFace.rows() / 2));
                
                if (eyes.size() >= 2) {
                    // Encontrou pelo menos 2 olhos, identifica esquerdo e direito
                    List<Rect> eyeRects = new ArrayList<>();
                    for (long i = 0; i < eyes.size(); i++) {
                        Rect eye = eyes.get(i);
                        eyeRects.add(eye);
                    }
                    
                    // Ordena por posição X (esquerda para direita)
                    eyeRects.sort((a, b) -> Integer.compare(a.x(), b.x()));
                    
                    // Centro do olho esquerdo
                    Rect leftEyeRect = eyeRects.get(0);
                    leftEye = new org.bytedeco.opencv.opencv_core.Point2f(
                        leftEyeRect.x() + leftEyeRect.width() / 2.0f,
                        leftEyeRect.y() + leftEyeRect.height() / 2.0f
                    );
                    
                    // Centro do olho direito
                    Rect rightEyeRect = eyeRects.get(eyeRects.size() - 1);
                    rightEye = new org.bytedeco.opencv.opencv_core.Point2f(
                        rightEyeRect.x() + rightEyeRect.width() / 2.0f,
                        rightEyeRect.y() + rightEyeRect.height() / 2.0f
                    );
                    
                    System.out.println("         ✅ Olhos detectados: esquerdo(" + 
                                     String.format("%.1f", leftEye.x()) + "," + String.format("%.1f", leftEye.y()) + 
                                     "), direito(" + 
                                     String.format("%.1f", rightEye.x()) + "," + String.format("%.1f", rightEye.y()) + ")");
                }
            }
            
            // Se não detectou olhos, usa estimativa baseada em proporções faciais conhecidas
            if (leftEye == null || rightEye == null) {
                System.out.println("         💡 Usando estimativa de posição dos olhos baseada em proporções faciais");
                int faceWidth = faceRoi.cols();
                int faceHeight = faceRoi.rows();
                
                // Proporções típicas dos olhos em uma face frontal
                // Olho esquerdo: ~37.5% da largura, ~40% da altura
                // Olho direito: ~62.5% da largura, ~40% da altura
                leftEye = new org.bytedeco.opencv.opencv_core.Point2f(
                    faceWidth * 0.375f,
                    faceHeight * 0.40f
                );
                rightEye = new org.bytedeco.opencv.opencv_core.Point2f(
                    faceWidth * 0.625f,
                    faceHeight * 0.40f
                );
            }
            
            // Calcula o ângulo de rotação necessário para alinhar os olhos horizontalmente
            double eyeDx = rightEye.x() - leftEye.x();
            double eyeDy = rightEye.y() - leftEye.y();
            double angle = Math.toDegrees(Math.atan2(eyeDy, eyeDx));
            
            System.out.println("         Ângulo de rotação calculado: " + String.format("%.2f", angle) + "°");
            
            // Calcula o ponto central entre os olhos
            org.bytedeco.opencv.opencv_core.Point2f eyeCenter = new org.bytedeco.opencv.opencv_core.Point2f(
                (leftEye.x() + rightEye.x()) / 2.0f,
                (leftEye.y() + rightEye.y()) / 2.0f
            );
            
            // Calcula a distância entre os olhos (usado para escala)
            double eyeDistance = Math.sqrt(eyeDx * eyeDx + eyeDy * eyeDy);
            
            // Distância desejada entre os olhos (proporção ideal: ~36% da largura da face)
            double desiredEyeDistance = faceRoi.cols() * 0.36;
            double scale = desiredEyeDistance / eyeDistance;
            
            System.out.println("         Escala calculada: " + String.format("%.3f", scale) + 
                             " (distância olhos: " + String.format("%.1f", eyeDistance) + ")");
            
            // Cria matriz de transformação (rotação + escala + translação)
            // Primeiro, rotaciona em torno do centro dos olhos
            Mat rotationMatrix = opencv_imgproc.getRotationMatrix2D(
                eyeCenter,  // Centro de rotação
                angle,      // Ângulo
                scale        // Escala
            );
            
            // Ajusta a translação para manter o centro dos olhos na posição correta
            // Lê os dados da matriz usando BytePointer
            BytePointer matrixData = rotationMatrix.data();
            double[] rotationData = new double[6];
            for (int i = 0; i < 6; i++) {
                rotationData[i] = matrixData.getDouble(i * 8); // double = 8 bytes
            }
            
            // Calcula nova posição do centro após rotação/escala
            double cosAngle = Math.cos(Math.toRadians(angle));
            double sinAngle = Math.sin(Math.toRadians(angle));
            double newCenterX = eyeCenter.x() * scale * cosAngle - eyeCenter.y() * scale * sinAngle;
            double newCenterY = eyeCenter.x() * scale * sinAngle + eyeCenter.y() * scale * cosAngle;
            
            // Ajusta translação para centralizar
            double desiredCenterX = faceRoi.cols() / 2.0;
            double desiredCenterY = faceRoi.rows() * 0.40; // 40% da altura (posição dos olhos)
            
            rotationData[2] += desiredCenterX - newCenterX;
            rotationData[5] += desiredCenterY - newCenterY;
            
            // Escreve os dados de volta na matriz
            for (int i = 0; i < 6; i++) {
                matrixData.putDouble(i * 8, rotationData[i]);
            }
            
            // Aplica a transformação
            Mat alignedFace = new Mat();
            opencv_imgproc.warpAffine(
                faceRoi,
                alignedFace,
                rotationMatrix,
                new Size(faceRoi.cols(), faceRoi.rows()),
                opencv_imgproc.INTER_LINEAR,
                opencv_core.BORDER_REPLICATE,
                new Scalar(0, 0, 0, 0)
            );
            
            // Limpa recursos
            grayFace.close();
            eyes.close();
            rotationMatrix.close();
            
            System.out.println("         ✅ Transformação aplicada: rotação=" + String.format("%.2f", angle) + 
                             "°, escala=" + String.format("%.3f", scale));
            
            return alignedFace;
            
        } catch (Exception e) {
            System.err.println("         ❌ Erro ao alinhar face: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    

    /**
     * Converte BufferedImage para Mat do OpenCV
     */
    private Mat bufferedImageToMat(BufferedImage bufferedImage) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Usa PNG para melhor qualidade
            ImageIO.write(bufferedImage, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            // System.out.println("Convertendo imagem para Mat: " + imageBytes.length + " bytes");
            try (BytePointer bytePointer = new BytePointer(imageBytes)) {
                Mat mat = opencv_imgcodecs.imdecode(new Mat(bytePointer), opencv_imgcodecs.IMREAD_COLOR);
                if (mat.empty()) {
                    System.err.println("Erro: Mat vazio após decodificação");
                } else {
                    // System.out.println("Mat criado com sucesso: " + mat.rows() + "x" + mat.cols());
                }
                return mat;
            }
        } catch (IOException e) {
            System.err.println("Erro ao converter BufferedImage para Mat: " + e.getMessage());
            e.printStackTrace();
            return new Mat();
        }
    }
}

