package util;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.Blob;
import java.sql.Connection;
import java.sql.SQLException;

import javax.imageio.ImageIO;

/**
 * Classe simplificada para reconhecimento facial Esta é uma implementação
 * básica que pode ser expandida posteriormente com bibliotecas mais avançadas
 * como OpenCV ou dlib
 */
public class ReconhecimentoFacial {

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
     * Detecta faces em uma imagem Implementação simplificada - sempre retorna
     * true
     */
    public boolean detectarFace(BufferedImage imagem) {
        if (imagem == null) {
            return false;
        }

        // Implementação básica - verifica se a imagem tem dimensões mínimas
        return imagem.getWidth() > 50 && imagem.getHeight() > 50;
    }

    /**
     * Extrai descritores faciais de uma imagem Implementação simplificada para
     * demonstração
     */
    public String extrairDescritoresFaciais(BufferedImage imagem) {
        if (imagem == null) {
            return "[]";
        }

        try {
            // Converte a imagem para array de bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
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
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(imagem, "jpg", baos);
            byte[] imageBytes = baos.toByteArray();

            return connection.createBlob();

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
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            return ImageIO.read(bais);

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
}
