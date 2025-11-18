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
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Size;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

/**
 * Classe para reconhecimento facial usando OpenCV Implementa detecção de faces
 * em tempo real
 */
public class ReconhecimentoFacial {

    private CascadeClassifier faceDetector;
    private boolean inicializado = false;

    private double scaleFactor = 1.1;
    private int minNeighbors = 4;
    private int minSize = 30;
    private int maxSize = 400;

    /**
     * Inicializa o detector de faces
     */
    public void inicializar() {
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
     * Detecta faces em uma imagem usando OpenCV com parâmetros otimizados
     */
    public boolean detectarFace(BufferedImage imagem) {
        if (imagem == null || !inicializado) {
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
     * Valida se a face está em uma posição razoável na imagem
     */
    private boolean validarPosicaoFace(Rect face, int imageWidth, int imageHeight) {
        // A face não deve estar muito próxima das bordas (exceto topo)
        // Muito tolerante para macOS
        int margin = Math.min(imageWidth, imageHeight) / 100; // 1% da menor dimensão

        return face.x() >= margin
                && face.y() >= 0
                && // Permite faces no topo da imagem
                (face.x() + face.width()) <= (imageWidth - margin)
                && (face.y() + face.height()) <= (imageHeight - margin);
    }

    /**
     * Valida se a proporção da face é razoável (aproximadamente quadrada)
     */
    private boolean validarProporcaoFace(Rect face) {
        double aspectRatio = (double) face.width() / face.height();
        // Faces humanas têm proporção entre 0.7 e 1.4
        return aspectRatio >= 0.7 && aspectRatio <= 1.4;
    }

    /**
     * Valida se o tamanho da face é razoável em relação à imagem
     */
    private boolean validarTamanhoFace(Rect face, int imageWidth, int imageHeight) {
        double faceArea = face.width() * face.height();
        double imageArea = imageWidth * imageHeight;
        double faceRatio = faceArea / imageArea;

        // A face deve ocupar entre 0.1% e 80% da área da imagem (muito tolerante para macOS)
        return faceRatio >= 0.001 && faceRatio <= 0.8;
    }

    /**
     * Detecta faces e retorna as coordenadas dos retângulos
     */
    public List<Rectangle> detectarFacesComCoordenadas(BufferedImage imagem) {
        List<Rectangle> faces = new ArrayList<>();

        if (imagem == null || !inicializado) {
            return faces;
        }

        try (Mat matImagem = bufferedImageToMat(imagem); Mat grayImage = new Mat(); RectVector faceRects = new RectVector()) {

            // Converte para escala de cinza
            opencv_imgproc.cvtColor(matImagem, grayImage, opencv_imgproc.COLOR_BGR2GRAY);

            // Aplica equalização de histograma para melhorar o contraste
            opencv_imgproc.equalizeHist(grayImage, grayImage);

            // Usa os parâmetros configuráveis
            faceDetector.detectMultiScale(grayImage, faceRects, scaleFactor, minNeighbors, 0,
                    new org.bytedeco.opencv.opencv_core.Size(minSize, minSize),
                    new org.bytedeco.opencv.opencv_core.Size(maxSize, maxSize));

            // Filtra apenas as faces válidas
            for (long i = 0; i < faceRects.size(); i++) {
                Rect rect = faceRects.get(i);

                // Aplica as mesmas validações
                if (validarPosicaoFace(rect, imagem.getWidth(), imagem.getHeight())
                        && validarProporcaoFace(rect)
                        && validarTamanhoFace(rect, imagem.getWidth(), imagem.getHeight())) {

                    faces.add(new Rectangle(rect.x(), rect.y(), rect.width(), rect.height()));
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao detectar faces com coordenadas: " + e.getMessage());
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
            System.err.println("❌ Imagem é NULL ao extrair descritores faciais");
            return "[]";
        }

        try {
            // Converte BufferedImage para Mat do OpenCV
            Mat matImagem = bufferedImageToMat(imagem);
            if (matImagem.empty()) {
                System.err.println("❌ Falha ao converter imagem para Mat");
                return "[]";
            }

            // Detecta faces na imagem
            List<Rectangle> faces = detectarFacesComCoordenadas(imagem);
            if (faces.isEmpty()) {
                System.err.println("⚠️ Nenhuma face detectada na imagem");
                // Mesmo sem face detectada, extrai características da imagem completa
                return extrairCaracteristicasImagem(matImagem);
            }

            // Pega a primeira face detectada (maior ou mais central)
            Rectangle faceRect = selecionarMelhorFace(faces, imagem.getWidth(), imagem.getHeight());
            
            // Extrai a região da face
            Rect faceRectCV = new Rect(faceRect.x, faceRect.y, faceRect.width, faceRect.height);
            Mat faceRoi = new Mat(matImagem, faceRectCV);

            // Extrai características da face
            List<Double> embedding = extrairEmbeddingFacial(faceRoi);

            // Serializa o embedding em JSON (equivalente a json.dumps(embedding))
            String embeddingJson = serializarEmbedding(embedding);

            System.out.println("✅ Embedding facial extraído: " + embedding.size() + " dimensões");
            
            // Limpa recursos
            matImagem.close();
            faceRoi.close();
            faceRectCV.close();

            return embeddingJson;

        } catch (Exception e) {
            System.err.println("❌ Erro ao extrair descritores faciais: " + e.getMessage());
            e.printStackTrace();
            return "[]";
        }
    }

    /**
     * Extrai embedding facial de uma região de face usando características OpenCV
     * Garante exatamente 128 dimensões para compatibilidade com modelos de reconhecimento facial
     * Usa LBP (Local Binary Patterns), histogramas e características de textura
     * 
     * @param faceRoi Mat contendo apenas a região da face
     * @return Lista de doubles representando o embedding (exatamente 128 dimensões)
     */
    private List<Double> extrairEmbeddingFacial(Mat faceRoi) {
        List<Double> embedding = new ArrayList<>();

        try {
            // Normaliza o tamanho da face para 128x128
            Mat faceNormalizada = new Mat();
            opencv_imgproc.resize(faceRoi, faceNormalizada, new Size(128, 128));

            // Converte para escala de cinza
            Mat grayFace = new Mat();
            if (faceNormalizada.channels() == 3) {
                opencv_imgproc.cvtColor(faceNormalizada, grayFace, opencv_imgproc.COLOR_BGR2GRAY);
            } else {
                grayFace = faceNormalizada.clone();
            }

            // Extrai características de forma balanceada para totalizar exatamente 128 dimensões
            // Distribuição: Histograma (48) + LBP (40) + Gradientes (16) + Estatísticas (24) = 128
            
            // 1. Histograma: 48 bins
            embedding.addAll(extrairHistograma(grayFace, 48));

            // 2. Características LBP: 40 dimensões
            embedding.addAll(extrairCaracteristicasLBP128(grayFace, 40));

            // 3. Características de gradientes: 16 dimensões
            embedding.addAll(extrairGradientesDetalhados(grayFace, 16));

            // 4. Características de intensidade e textura: 24 dimensões
            embedding.addAll(extrairEstatisticasDetalhadas(grayFace, 24));

            // Garante exatamente 128 dimensões
            // Se tiver mais, trunca; se tiver menos, preenche com zeros normalizados
            ajustarPara128Dimensoes(embedding);

            // Normaliza o embedding (L2 normalization) - mantém a distribuição mas normaliza
            normalizarEmbedding(embedding);

            // Limpa recursos
            faceNormalizada.close();
            grayFace.close();

        } catch (Exception e) {
            System.err.println("❌ Erro ao extrair embedding facial: " + e.getMessage());
            e.printStackTrace();
            // Em caso de erro, retorna embedding vazio de 128 zeros
            while (embedding.size() < 128) {
                embedding.add(0.0);
            }
        }

        return embedding;
    }
    
    /**
     * Ajusta o embedding para ter exatamente 128 dimensões
     * Se tiver mais, trunca; se tiver menos, preenche com valores baseados na média
     */
    private void ajustarPara128Dimensoes(List<Double> embedding) {
        int targetSize = 128;
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
     * Extrai características usando análise de blocos (LBP simplificado)
     */
    private List<Double> extrairCaracteristicasLBP(Mat imagem) {
        List<Double> lbpFeatures = new ArrayList<>();
        
        try {
            int blockSize = 16;
            int rows = imagem.rows();
            int cols = imagem.cols();

            BytePointer dataPtr = imagem.data();
            int step = (int) imagem.step();
            int channels = imagem.channels();

            for (int y = 0; y < rows - blockSize; y += blockSize) {
                for (int x = 0; x < cols - blockSize; x += blockSize) {
                    double soma = 0.0;
                    double somaQuadrados = 0.0;
                    int count = 0;

                    for (int by = y; by < Math.min(y + blockSize, rows); by++) {
                        for (int bx = x; bx < Math.min(x + blockSize, cols); bx++) {
                            long offset = by * step + bx * channels;
                            byte intensity = dataPtr.get(offset);
                            double intValue = (intensity & 0xFF) / 255.0;
                            soma += intValue;
                            somaQuadrados += intValue * intValue;
                            count++;
                        }
                    }

                    if (count > 0) {
                        double media = soma / count;
                        double variancia = (somaQuadrados / count) - (media * media);
                        double desvioPadrao = Math.sqrt(Math.max(0, variancia));
                        
                        lbpFeatures.add(media);
                        lbpFeatures.add(desvioPadrao);
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Erro ao extrair características LBP: " + e.getMessage());
            e.printStackTrace();
        }

        return lbpFeatures;
    }

    /**
     * Extrai características de gradientes usando análise de diferenças
     */
    private List<Double> extrairGradientes(Mat imagem) {
        List<Double> gradientes = new ArrayList<>();
        
        try {
            int rows = imagem.rows();
            int cols = imagem.cols();
            BytePointer dataPtr = imagem.data();
            int step = (int) imagem.step();
            int channels = imagem.channels();

            double somaGradientes = 0.0;
            double somaQuadrados = 0.0;
            int count = 0;

            // Calcula gradientes usando diferenças de pixels
            for (int y = 1; y < rows - 1; y++) {
                for (int x = 1; x < cols - 1; x++) {
                    // Gradiente horizontal
                    long offset1 = y * step + (x - 1) * channels;
                    long offset2 = y * step + (x + 1) * channels;
                    byte pixel1 = dataPtr.get(offset1);
                    byte pixel2 = dataPtr.get(offset2);
                    double gradX = Math.abs((pixel2 & 0xFF) - (pixel1 & 0xFF)) / 255.0;

                    // Gradiente vertical
                    offset1 = (y - 1) * step + x * channels;
                    offset2 = (y + 1) * step + x * channels;
                    pixel1 = dataPtr.get(offset1);
                    pixel2 = dataPtr.get(offset2);
                    double gradY = Math.abs((pixel2 & 0xFF) - (pixel1 & 0xFF)) / 255.0;

                    // Magnitude do gradiente
                    double magnitude = Math.sqrt(gradX * gradX + gradY * gradY);
                    somaGradientes += magnitude;
                    somaQuadrados += magnitude * magnitude;
                    count++;
                }
            }

            if (count > 0) {
                double media = somaGradientes / count;
                double variancia = (somaQuadrados / count) - (media * media);
                double desvioPadrao = Math.sqrt(Math.max(0, variancia));

                gradientes.add(media);
                gradientes.add(desvioPadrao);
            } else {
                gradientes.add(0.0);
                gradientes.add(0.0);
            }

        } catch (Exception e) {
            System.err.println("Erro ao extrair gradientes: " + e.getMessage());
            e.printStackTrace();
            gradientes.add(0.0);
            gradientes.add(0.0);
        }

        return gradientes;
    }

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
     * Converte BufferedImage para Blob para armazenamento no banco
     * IMPORTANTE: O Blob criado deve ser usado na mesma conexão que o criou
     */
    public Blob imagemParaBlob(BufferedImage imagem, Connection connection) throws SQLException {
        if (imagem == null) {
            throw new SQLException("Imagem é NULL");
        }
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Converte a imagem para JPG
            boolean written = ImageIO.write(imagem, "jpg", baos);
            if (!written) {
                throw new SQLException("Falha ao converter imagem para JPG");
            }
            
            byte[] imageBytes = baos.toByteArray();
            System.out.println("  - Bytes da imagem: " + imageBytes.length);
            
            if (imageBytes.length == 0) {
                throw new SQLException("Imagem convertida está vazia (0 bytes)");
            }

            // Cria o Blob usando a conexão fornecida
            Blob blob = connection.createBlob();
            blob.setBytes(1, imageBytes);
            
            System.out.println("  - BLOB criado: " + blob.length() + " bytes");
            return blob;

        } catch (IOException e) {
            throw new SQLException("Erro ao converter imagem para Blob: " + e.getMessage(), e);
        }
    }

    /**
     * Converte Blob para BufferedImage
     */
    public BufferedImage blobParaImagem(Blob blob) throws SQLException {
        try {
            byte[] bytes = blob.getBytes(1, (int) blob.length());
            try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
                return ImageIO.read(bais);
            }
        } catch (IOException e) {
            throw new SQLException("Erro ao converter Blob para imagem", e);
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
     * Calcula similaridade entre duas imagens Implementação básica baseada em
     * correlação de pixels
     */
    public double calcularSimilaridade(BufferedImage img1, BufferedImage img2) {
        if (img1 == null || img2 == null) {
            return 0.0;
        }

        try {
            // Redimensiona ambas as imagens para o mesmo tamanho
            BufferedImage resized1 = redimensionarImagem(img1, 50, 50);
            BufferedImage resized2 = redimensionarImagem(img2, 50, 50);

            // Calcula correlação simples entre os pixels
            double soma = 0.0;
            double total = 0.0;

            for (int x = 0; x < 50; x++) {
                for (int y = 0; y < 50; y++) {
                    int rgb1 = resized1.getRGB(x, y);
                    int rgb2 = resized2.getRGB(x, y);

                    // Extrai o valor de cinza
                    int gray1 = (rgb1 >> 16) & 0xFF;
                    int gray2 = (rgb2 >> 16) & 0xFF;

                    soma += Math.abs(gray1 - gray2);
                    total += 255;
                }
            }

            // Converte para similaridade (0 = diferente, 1 = idêntico)
            double diferenca = soma / total;
            return Math.max(0, 1 - diferenca);

        } catch (Exception e) {
            System.err.println("Erro ao calcular similaridade: " + e.getMessage());
            return 0.0;
        }
    }

    /**
     * Redimensiona uma imagem para o tamanho especificado
     */
    private BufferedImage redimensionarImagem(BufferedImage original, int largura, int altura) {
        BufferedImage redimensionada = new BufferedImage(largura, altura, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < largura; x++) {
            for (int y = 0; y < altura; y++) {
                int srcX = (x * original.getWidth()) / largura;
                int srcY = (y * original.getHeight()) / altura;
                redimensionada.setRGB(x, y, original.getRGB(srcX, srcY));
            }
        }

        return redimensionada;
    }

    /**
     * Cria uma imagem de teste para demonstração
     */
    public BufferedImage criarImagemTeste() {
        BufferedImage imagem = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < 100; x++) {
            for (int y = 0; y < 100; y++) {
                int cor = (x + y) % 256;
                imagem.setRGB(x, y, (cor << 16) | (cor << 8) | cor);
            }
        }

        return imagem;
    }

    /**
     * Testa a detecção facial com uma imagem simples
     */
    public void testarDetecao() {
        System.out.println("=== TESTE DE DETECÇÃO FACIAL ===");
        System.out.println("Inicializado: " + inicializado);

        if (!inicializado) {
            System.out.println("Detector não inicializado - tentando inicializar...");
            inicializar();
        }

        if (inicializado) {
            BufferedImage imagemTeste = criarImagemTeste();
            boolean resultado = detectarFace(imagemTeste);
            System.out.println("Resultado do teste: " + resultado);
        } else {
            System.out.println("Não foi possível inicializar o detector");
        }
        System.out.println("=== FIM DO TESTE ===");
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
