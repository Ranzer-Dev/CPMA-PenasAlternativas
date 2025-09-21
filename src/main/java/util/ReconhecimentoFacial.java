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
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;

/**
 * Classe para reconhecimento facial usando OpenCV Implementa detecção de faces
 * em tempo real
 */
public class ReconhecimentoFacial {

    private CascadeClassifier faceDetector;
    private boolean inicializado = false;

    // Parâmetros de detecção configuráveis
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

            // System.out.println("Tentando carregar cascade de: " + cascadePath);
            faceDetector = new CascadeClassifier(cascadePath);

            if (faceDetector.empty()) {
                System.err.println("Erro ao carregar o classificador de faces - tentando arquivo alternativo");
                // Tenta o arquivo alternativo
                String altCascadePath = getClass().getResource("/opencv/haarcascades/haarcascade_frontalface_alt.xml").getPath();

                // Corrige o caminho para diferentes sistemas operacionais
                altCascadePath = corrigirCaminhoArquivo(altCascadePath);

                // System.out.println("Tentando arquivo alternativo: " + altCascadePath);
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
        this.scaleFactor = 1.05; // Mais sensível
        this.minNeighbors = 2;   // Menos confirmações
        this.minSize = 20;       // Faces menores
        this.maxSize = 500;      // Limite maior
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
            // Windows: remove a barra inicial se existir
            if (caminho.startsWith("/")) {
                caminho = caminho.substring(1);
            }
            // Converte barras para o formato Windows se necessário
            caminho = caminho.replace("/", "\\");
        } else if (osName.contains("linux") || osName.contains("unix") || osName.contains("mac")) {
            // Linux/Unix/Mac: mantém o formato com barras
            // Não precisa fazer nada especial
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

            // Converte para escala de cinza
            opencv_imgproc.cvtColor(matImagem, grayImage, opencv_imgproc.COLOR_BGR2GRAY);

            // Aplica equalização de histograma para melhorar o contraste
            opencv_imgproc.equalizeHist(grayImage, grayImage);

            // Parâmetros otimizados para reduzir falsos positivos
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
        int margin = Math.min(imageWidth, imageHeight) / 20; // 5% da menor dimensão

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

        // A face deve ocupar entre 1% e 50% da área da imagem
        return faceRatio >= 0.01 && faceRatio <= 0.5;
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
     * qualidade
     */
    public BufferedImage desenharRetangulosFaces(BufferedImage imagem) {
        List<Rectangle> faces = detectarFacesComCoordenadas(imagem);

        // Cria uma cópia da imagem para desenhar
        BufferedImage imagemComRetangulos = new BufferedImage(
                imagem.getWidth(), imagem.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = imagemComRetangulos.createGraphics();
        g2d.drawImage(imagem, 0, 0, null);

        if (faces.isEmpty()) {
            // Desenha mensagem quando nenhuma face válida é detectada
            g2d.setColor(java.awt.Color.RED);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 16));
            g2d.drawString("Nenhuma face válida detectada", 10, 30);
        } else {
            // Desenha retângulos verdes ao redor das faces válidas
            g2d.setColor(java.awt.Color.GREEN);
            g2d.setStroke(new java.awt.BasicStroke(3));

            for (int i = 0; i < faces.size(); i++) {
                Rectangle face = faces.get(i);
                g2d.drawRect(face.x, face.y, face.width, face.height);

                // Desenha um número para identificar cada face
                g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 14));
                g2d.drawString(String.valueOf(i + 1), face.x + 5, face.y + 20);
            }

            // Desenha informações sobre a detecção
            g2d.setColor(java.awt.Color.WHITE);
            g2d.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 12));
            g2d.drawString("Faces detectadas: " + faces.size(), 10, 20);
        }

        g2d.dispose();
        return imagemComRetangulos;
    }

    /**
     * Extrai descritores faciais de uma imagem Implementação simplificada para
     * demonstração
     */
    public String extrairDescritoresFaciais(BufferedImage imagem) {
        if (imagem == null) {
            return "[]";
        }

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // Converte a imagem para array de bytes
            ImageIO.write(imagem, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            // Cria um descritor simplificado baseado nos bytes da imagem
            StringBuilder descritores = new StringBuilder("[");
            for (int i = 0; i < Math.min(imageBytes.length, 100); i++) { // Limita a 100 valores
                if (i > 0) {
                    descritores.append(",");
                }
                descritores.append((int) imageBytes[i]);
            }
            descritores.append("]");

            return descritores.toString();

        } catch (IOException e) {
            System.err.println("Erro ao extrair descritores faciais: " + e.getMessage());
            return "[]";
        }
    }

    /**
     * Converte BufferedImage para Blob para armazenamento no banco
     */
    public Blob imagemParaBlob(BufferedImage imagem, Connection connection) throws SQLException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(imagem, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            Blob blob = connection.createBlob();
            blob.setBytes(1, imageBytes);
            return blob;

        } catch (IOException e) {
            throw new SQLException("Erro ao converter imagem para Blob", e);
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
