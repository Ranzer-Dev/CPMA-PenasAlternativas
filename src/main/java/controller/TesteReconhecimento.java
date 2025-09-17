package controller;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import util.ReconhecimentoFacial;

/**
 * Classe para testar o reconhecimento facial
 */
public class TesteReconhecimento {

    public static void main(String[] args) {
        System.out.println("=== INICIANDO TESTE DE RECONHECIMENTO FACIAL ===");

        // Teste 1: Inicialização
        ReconhecimentoFacial reconhecimento = new ReconhecimentoFacial();
        System.out.println("1. Criando instância...");

        System.out.println("2. Inicializando detector...");
        reconhecimento.inicializar();

        System.out.println("3. Verificando inicialização...");
        if (reconhecimento.isInicializado()) {
            System.out.println("✓ Detector inicializado com sucesso!");
        } else {
            System.out.println("✗ Falha na inicialização do detector");
            return;
        }

        // Teste 2: Detecção com imagem de teste
        System.out.println("4. Testando detecção com imagem de teste...");
        reconhecimento.testarDetecao();

        // Teste 3: Verificar arquivos de cascade
        System.out.println("5. Verificando arquivos de cascade...");
        try {
            String cascadePath1 = reconhecimento.getClass().getResource("/opencv/haarcascades/haarcascade_frontalface_default.xml").getPath();
            System.out.println("Arquivo 1 encontrado: " + cascadePath1);

            String cascadePath2 = reconhecimento.getClass().getResource("/opencv/haarcascades/haarcascade_frontalface_alt.xml").getPath();
            System.out.println("Arquivo 2 encontrado: " + cascadePath2);
        } catch (Exception e) {
            System.out.println("Erro ao verificar arquivos: " + e.getMessage());
        }

        // Teste 4: Criar imagem de teste com padrão que pode ser detectado
        System.out.println("6. Criando imagem de teste mais realista...");
        BufferedImage imagemTeste = criarImagemComPadrao();

        try {
            ImageIO.write(imagemTeste, "png", new File("teste_imagem.png"));
            System.out.println("Imagem de teste salva como 'teste_imagem.png'");
        } catch (IOException e) {
            System.out.println("Erro ao salvar imagem: " + e.getMessage());
        }

        System.out.println("7. Testando detecção na imagem de teste...");
        boolean resultado = reconhecimento.detectarFace(imagemTeste);
        System.out.println("Resultado: " + resultado);

        System.out.println("=== FIM DO TESTE ===");
    }

    /**
     * Cria uma imagem com um padrão que pode ser detectado como face
     */
    private static BufferedImage criarImagemComPadrao() {
        BufferedImage imagem = new BufferedImage(200, 200, BufferedImage.TYPE_INT_RGB);

        // Fundo branco
        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 200; y++) {
                imagem.setRGB(x, y, 0xFFFFFF); // Branco
            }
        }

        // Desenha um padrão oval (simulando uma face)
        int centroX = 100;
        int centroY = 100;
        int raioX = 60;
        int raioY = 80;

        for (int x = 0; x < 200; x++) {
            for (int y = 0; y < 200; y++) {
                int dx = x - centroX;
                int dy = y - centroY;

                // Fórmula da elipse
                if ((dx * dx) / (raioX * raioX) + (dy * dy) / (raioY * raioY) <= 1) {
                    imagem.setRGB(x, y, 0xCCCCCC); // Cinza claro
                }
            }
        }

        // Desenha olhos
        for (int x = 80; x < 95; x++) {
            for (int y = 80; y < 95; y++) {
                imagem.setRGB(x, y, 0x000000); // Preto
            }
        }

        for (int x = 105; x < 120; x++) {
            for (int y = 80; y < 95; y++) {
                imagem.setRGB(x, y, 0x000000); // Preto
            }
        }

        // Desenha boca
        for (int x = 90; x < 110; x++) {
            for (int y = 120; y < 130; y++) {
                imagem.setRGB(x, y, 0x000000); // Preto
            }
        }

        return imagem;
    }
}
