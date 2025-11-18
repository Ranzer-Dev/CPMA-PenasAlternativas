package controller;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.opencv.global.opencv_imgcodecs;
import org.bytedeco.opencv.opencv_core.Mat;
import org.json.JSONObject;

import dao.DadosFaciaisDAO;
import dao.InstituicaoDAO;
import dao.PenaDAO;
import dao.UsuarioDAO;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.application.Platform;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.DadosFaciais;
import model.Instituicao;
import model.Pena;
import model.Usuario;
 import util.ReconhecimentoFacial;
import util.ValidadorCPF;

public class CadastrarUsuarioController {

    @FXML
    private TextField cep;
    @FXML
    private TextField telefone;
    @FXML
    private ComboBox<String> comboTipoPena;
    @FXML
    private ComboBox<Usuario> comboUsuarios;
    @FXML
    private DatePicker dataNascimento;
    @FXML
    private TextField criadoEm;
    @FXML
    private TextField nome, cpf, nacionalidade;
    @FXML
    private TextField endereco, bairro, cidade, uf;
    @FXML
    private TextArea observacao;
    @FXML
    private ComboBox<String> comboInstituicao;
    @FXML
    private Button btnCadastrar;
    @FXML
    private ImageView foto;
    @FXML
    private Button btnIniciarCamera;
    @FXML
    private Button btnCapturar;
    @FXML
    private Button btnBuscarCep;
    @FXML
    private ProgressIndicator loadingCep;
    @FXML
    private javafx.scene.control.Label lblStatusCep;
    private FrameGrabber camera;
    private ScheduledExecutorService timer;
    private boolean cameraAtiva = false;
    private Mat frameCapturado;

    // Variáveis para reconhecimento facial
    private ReconhecimentoFacial reconhecimentoFacial;
    private DadosFaciaisDAO dadosFaciaisDAO;
    private BufferedImage imagemCapturada;

    private Usuario usuarioEditando = null;
    private boolean modoEdicao = false;
    private int idUsuarioInserido;
    private final Map<String, Integer> mapNomeParaIdInstituicao = new HashMap<>();

    /**
     * Aplica máscara no formato CPF (000.000.000-00)
     */
    private String aplicarMascaraCPF(String digits) {
        if (digits == null || digits.isEmpty()) {
            return "";
        }

        // Remove caracteres não numéricos
        digits = digits.replaceAll("[^0-9]", "");

        // Aplica a máscara conforme o tamanho
        if (digits.length() <= 3) {
            return digits;
        } else if (digits.length() <= 6) {
            return digits.substring(0, 3) + "." + digits.substring(3);
        } else if (digits.length() <= 9) {
            return digits.substring(0, 3) + "." + digits.substring(3, 6) + "." + digits.substring(6);
        } else {
            return digits.substring(0, 3) + "." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-" + digits.substring(9);
        }
    }

    /**
     * Aplica máscara no formato telefone ((00) 00000-0000)
     */
    private String aplicarMascaraTelefone(String digits) {
        if (digits == null || digits.isEmpty()) {
            return "";
        }

        // Remove caracteres não numéricos
        digits = digits.replaceAll("[^0-9]", "");

        // Aplica a máscara conforme o tamanho
        if (digits.length() <= 2) {
            return digits;
        } else if (digits.length() <= 6) {
            return "(" + digits.substring(0, 2) + ") " + digits.substring(2);
        } else if (digits.length() <= 10) {
            return "(" + digits.substring(0, 2) + ") " + digits.substring(2, 7) + "-" + digits.substring(7);
        } else {
            return "(" + digits.substring(0, 2) + ") " + digits.substring(2, 7) + "-" + digits.substring(7, 11);
        }
    }

    @FXML
    public void initialize() {
        System.out.println("Inicializando CadastrarUsuarioController...");

        // Inicializar reconhecimento facial
        reconhecimentoFacial = new ReconhecimentoFacial();
        dadosFaciaisDAO = new DadosFaciaisDAO();

        limitarUF();
        configurarListenersRemocaoErro();
        configurarDataAtual();
        configurarFormatoBrasileiro();

        btnCadastrar.setOnAction(event -> {
            if (modoEdicao) {
                salvarAlteracoes();
            } else {
                cadastrarUsuario();
            }
        });

        // Máscara para CPF (000.000.000-00)
        cpf.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() <= 14) { // 11 dígitos + 3 pontos + 1 hífen = 14 caracteres
                return change;
            }
            return null;
        }));

        // Listener para aplicar máscara do CPF automaticamente
        cpf.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                String digitsOnly = newValue.replaceAll("[^0-9]", "");
                if (digitsOnly.length() <= 11) {
                    String masked = aplicarMascaraCPF(digitsOnly);
                    if (!masked.equals(newValue)) {
                        cpf.setText(masked);
                        cpf.positionCaret(masked.length());
                    }
                }
            }
        });

        // Máscara para telefone ((00) 00000-0000)
        telefone.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.length() <= 15) { // 11 dígitos + 4 caracteres especiais = 15 caracteres
                return change;
            }
            return null;
        }));

        // Listener para aplicar máscara do telefone automaticamente
        telefone.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(oldValue)) {
                String digitsOnly = newValue.replaceAll("[^0-9]", "");
                if (digitsOnly.length() <= 11) {
                    String masked = aplicarMascaraTelefone(digitsOnly);
                    if (!masked.equals(newValue)) {
                        telefone.setText(masked);
                        telefone.positionCaret(masked.length());
                    }
                }
            }
        });

        cep.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("[0-9\\-]*") ? c : null));

        // Configurar listener para buscar CEP automaticamente
        System.out.println("Configurando busca de CEP...");
        configurarBuscaCEP();
        System.out.println("Busca de CEP configurada!");

        // Configura shutdown hook para liberar recursos da câmera
        // configurarShutdownHook();
    }

    @FXML
    private void iniciarCamera(ActionEvent event) {
        System.out.println("=== INICIANDO CÂMERA ===");
        System.out.println("Evento recebido: " + event);
        System.out.println("Botão clicado: " + (event.getSource() != null ? event.getSource().getClass().getSimpleName() : "NULL"));

        try {
            System.out.println("Carregando FXML da câmera...");
            // Carrega o FXML da janela da câmera
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/cpma/cameraView.fxml"));
            Parent root = loader.load();
            System.out.println("FXML carregado com sucesso!");

            // Pega o controller da janela da câmera
            CameraController cameraController = loader.getController();
            System.out.println("Controller da câmera obtido: " + (cameraController != null ? "OK" : "NULL"));

            // Cria uma nova janela (Stage)
            Stage cameraStage = new Stage();
            cameraStage.setTitle("Capturar Foto");
            cameraStage.setScene(new Scene(root));
            System.out.println("Stage da câmera criado!");

            // Configura para ser uma janela modal (bloqueia a janela de cadastro)
            cameraStage.initModality(Modality.APPLICATION_MODAL);

            // Define uma ação para quando a janela for fechada
            cameraStage.setOnCloseRequest(e -> {
                System.out.println("Janela da câmera sendo fechada pelo usuário");
            });

            // Mostra a janela e espera ela ser fechada
            System.out.println("Mostrando janela da câmera...");
            cameraStage.showAndWait();
            System.out.println("Janela da câmera fechada!");

            // IMPORTANTE: Aguarda um pouco para garantir que a Task assíncrona que fecha a janela terminou
            // e que a imagem foi capturada
            // Tenta pegar a imagem várias vezes, com pequenos delays, até conseguir ou desistir
            BufferedImage imagemCapturadaTemp = null;
            int tentativas = 0;
            int maxTentativas = 15; // Tenta por até 1.5 segundos (15 x 100ms)
            
            System.out.println("=== PROCESSANDO RESULTADO DA CÂMERA ===");
            System.out.println("Verificando imagem capturada após fechar janela...");
            
            while (tentativas < maxTentativas && imagemCapturadaTemp == null) {
                imagemCapturadaTemp = cameraController.getImagemCapturada();
                if (imagemCapturadaTemp == null) {
                    tentativas++;
                    System.out.println("  - Tentativa " + tentativas + "/" + maxTentativas + ": imagem ainda não disponível...");
                    try {
                        Thread.sleep(100); // Aguarda 100ms antes de tentar novamente
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    System.out.println("  - ✅ Imagem encontrada na tentativa " + tentativas + "!");
                }
            }
            
            // Agora usa uma variável final para o lambda
            final BufferedImage imagemCapturada = imagemCapturadaTemp;
            
            if (imagemCapturada != null) {
                System.out.println("✅ Imagem capturada do controller: OK (após " + tentativas + " tentativas)");
                System.out.println("✅ Dimensões da imagem: " + imagemCapturada.getWidth() + "x" + imagemCapturada.getHeight());
                System.out.println("✅ Tipo da imagem: " + imagemCapturada.getType());

                // Guarda a imagem para salvar no banco (BLOB no campo imagem_rosto)
                this.imagemCapturada = imagemCapturada;
                System.out.println("✅ Imagem armazenada em this.imagemCapturada");
                System.out.println("  - Dimensões: " + this.imagemCapturada.getWidth() + "x" + this.imagemCapturada.getHeight());
                System.out.println("  - Tipo: " + this.imagemCapturada.getType());
                System.out.println("  - Será salva como BLOB no campo imagem_rosto ao cadastrar o usuário");

                // Exibe a foto imediatamente na ImageView
                Platform.runLater(() -> {
                    try {
                        Image imagePreview = converterBufferedImageParaImage(imagemCapturada);
                        if (imagePreview != null && !imagePreview.isError()) {
                            foto.setImage(imagePreview);
                            foto.setFitWidth(120.0);
                            foto.setFitHeight(120.0);
                            foto.setPreserveRatio(true);
                            foto.setSmooth(true);
                            foto.setCache(true);
                            foto.setVisible(true);
                            System.out.println("✅ Foto exibida no ImageView com sucesso!");
                        } else {
                            System.err.println("⚠️ Erro ao exibir foto na ImageView, mas será salva no banco");
                        }
                    } catch (Exception ex) {
                        System.err.println("⚠️ Erro ao exibir foto: " + ex.getMessage());
                        // A foto ainda será salva no banco mesmo se houver erro na exibição
                    }
                });
            } else {
                System.out.println("❌ Nenhuma imagem foi capturada após " + tentativas + " tentativas");
                this.imagemCapturada = null; // Garante que está NULL se não houve captura
            }

        } catch (IOException e) {
            System.err.println("❌ ERRO ao abrir janela da câmera: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível abrir a janela da câmera: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ ERRO inesperado: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro inesperado: " + e.getMessage());
        }
    }

    /**
     * Converte um objeto Mat (Bytedeco) para um objeto Image (JavaFX).
     */
    private Image matToImage(Mat frame) {
        try {
            // Usa um ponteiro de bytes para armazenar a imagem codificada
            BytePointer bytePointer = new BytePointer();
            opencv_imgcodecs.imencode(".png", frame, bytePointer);

            // Pega os bytes do ponteiro
            byte[] bytes = new byte[(int) bytePointer.limit()];
            bytePointer.get(bytes);

            // Libera o ponteiro
            bytePointer.close();

            return new Image(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            System.err.println("Não foi possível converter o Mat para Image: " + e);
            return null;
        }
    }

    /**
     * Converte um BufferedImage (AWT) para uma Image (JavaFX) manualmente.
     *
     * @param bufferedImage A imagem a ser convertida.
     * @return Uma imagem compatível com JavaFX ou null se ocorrer um erro.
     */
    private Image converterBufferedImageParaImage(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            System.err.println("❌ BufferedImage é NULL na conversão");
            return null;
        }
        try {
            System.out.println("Convertendo BufferedImage de " + bufferedImage.getWidth() + "x" + bufferedImage.getHeight());
            
            // Melhora a qualidade da imagem antes da conversão
            BufferedImage imageToConvert = melhorarQualidadeImagem(bufferedImage);
            if (imageToConvert == null) {
                System.err.println("❌ Falha ao melhorar qualidade da imagem");
                return null;
            }
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // Escreve a imagem como um PNG em um fluxo de bytes na memória
            boolean written = ImageIO.write(imageToConvert, "png", outputStream);
            if (!written) {
                System.err.println("❌ Falha ao escrever imagem para stream");
                return null;
            }
            
            byte[] bytes = outputStream.toByteArray();
            System.out.println("Bytes da imagem: " + bytes.length);
            
            // Cria uma Image do JavaFX a partir do fluxo de bytes
            Image result = new Image(new ByteArrayInputStream(bytes));
            System.out.println("Image do JavaFX criada: " + (result != null ? "OK" : "FALHOU"));
            return result;
        } catch (IOException e) {
            System.err.println("❌ Erro na conversão: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Melhora a qualidade da imagem para exibição
     */
    private BufferedImage melhorarQualidadeImagem(BufferedImage original) {
        if (original == null) {
            return null;
        }
        
        // Cria uma nova imagem com melhor qualidade
        BufferedImage improved = new BufferedImage(
            original.getWidth(), 
            original.getHeight(), 
            BufferedImage.TYPE_INT_RGB
        );
        
        // Desenha a imagem original na nova com melhor qualidade
        Graphics2D g2d = improved.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2d.drawImage(original, 0, 0, null);
        g2d.dispose();
        
        return improved;
    }
// ... outras importações ...

    @FXML
    private void capturarFoto(ActionEvent event) {
        if (frameCapturado != null && !frameCapturado.empty()) {
            try {
                // Converte o frame capturado para uma imagem e exibe no ImageView
                Image fotoCapturada = matToImage(frameCapturado);
                foto.setImage(fotoCapturada);

                // Converte o Mat do Bytedeco para um BufferedImage para processamento facial
                imagemCapturada = matToBufferedImage(frameCapturado);

                // Para a câmera após a captura
                pararCamera();
                System.out.println("Foto capturada com sucesso!");

                // Mostra mensagem de sucesso
                mostrarAlerta("Sucesso", "Foto capturada com sucesso! Os dados faciais serão salvos junto com o cadastro.");

            } catch (Exception e) {
                mostrarAlerta("Erro", "Erro ao processar a foto: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Aviso", "Nenhuma imagem foi capturada pela câmera. Tente novamente.");
        }
    }

    /**
     * Converte um objeto Mat (Bytedeco) para BufferedImage (AWT). Necessário
     * para o reconhecimento facial.
     */
    private BufferedImage matToBufferedImage(Mat frame) {
        try {
            // Usa o mesmo processo do matToImage, mas para o BufferedImage
            BytePointer bytePointer = new BytePointer();
            opencv_imgcodecs.imencode(".png", frame, bytePointer);
            byte[] bytes = new byte[(int) bytePointer.limit()];
            bytePointer.get(bytes);
            bytePointer.close();

            return ImageIO.read(new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            System.err.println("Não foi possível converter o Mat para BufferedImage: " + e.getMessage());
            return null;
        }
    }

    /**
     * Para a execução da câmera e limpa os recursos.
     */
    private void pararCamera() {
        if (timer != null && !timer.isShutdown()) {
            try {
                timer.shutdown();
                timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Erro ao parar a captura de frames: " + e.getMessage());
            }
        }

        if (camera != null) {
            try {
                camera.stop();
                camera.release();
            } catch (FrameGrabber.Exception e) {
                System.err.println("Erro ao parar a câmera: " + e.getMessage());
            }
        }

        camera = null;
        cameraAtiva = false;
        btnIniciarCamera.setText("Tirar Foto");
        btnCapturar.setVisible(false);
    }

    public static int cadastrarUsuario(
            String nome, String cpf,
            String nacionalidade, LocalDate dataNascimento,
            String endereco, String bairro, String cidade, String uf,
            String observacao, String telefone, String cep) {

        if (nome == null || nome.trim().isEmpty() || !nome.matches("[a-zA-ZÀ-ú\\s]+")) {
            throw new IllegalArgumentException("Nome inválido.");
        }

        String cpfLimpo = cpf.replaceAll("\\D", "");
        if (cpfLimpo.length() != 11 || !ValidadorCPF.isCPFValido(cpfLimpo)) {
            throw new IllegalArgumentException("CPF inválido.");
        }

        String foneLimpo = telefone.replaceAll("\\D", "");
        if (foneLimpo.length() < 8 || foneLimpo.length() > 15) {
            throw new IllegalArgumentException("Telefone inválido.");
        }

        if (UsuarioDAO.cpfExiste(cpfLimpo)) {
            throw new IllegalArgumentException("CPF já cadastrado.");
        }

        if (nacionalidade == null || nacionalidade.trim().isEmpty()) {
            throw new IllegalArgumentException("Nacionalidade inválida.");
        }

        if (dataNascimento == null || dataNascimento.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Data de nascimento inválida.");
        }

        if (endereco == null || endereco.trim().isEmpty()) {
            throw new IllegalArgumentException("Endereço inválido.");
        }

        if (bairro == null || bairro.trim().isEmpty()) {
            throw new IllegalArgumentException("Bairro inválido.");
        }

        if (cidade == null || cidade.trim().isEmpty()) {
            throw new IllegalArgumentException("Cidade inválida.");
        }

        if (uf == null || !uf.trim().matches("^[A-Z]{2}$")) {
            throw new IllegalArgumentException("UF inválida.");
        }

        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setCpf(cpfLimpo);
        usuario.setNacionalidade(nacionalidade.trim());
        usuario.setDataNascimento(Date.valueOf(dataNascimento));
        usuario.setCriadoEm(new Date(System.currentTimeMillis()));
        usuario.setEndereco(endereco.trim());
        usuario.setBairro(bairro.trim());
        usuario.setCidade(cidade.trim());
        usuario.setUf(uf.trim().toUpperCase());
        usuario.setObservacao(observacao == null ? "" : observacao.trim());
        usuario.setFoto("");
        usuario.setTelefone(foneLimpo);
        usuario.setCep(cep);
        // Código será gerado automaticamente quando a primeira pena for cadastrada
        usuario.setCodigo(""); // Inicia vazio

        return UsuarioDAO.inserir(usuario);
    }

    private void cadastrarUsuario() {
        try {
            if (!validarCpf(cpf.getText())) {
                return;
            }

            idUsuarioInserido = cadastrarUsuario(
                    nome.getText(), cpf.getText(), nacionalidade.getText(),
                    dataNascimento.getValue(), endereco.getText(),
                    bairro.getText(), cidade.getText(), uf.getText(), observacao.getText(),
                    telefone.getText().trim(), cep.getText());

            System.out.println("=== VERIFICANDO SE HÁ FOTO PARA SALVAR ===");
            System.out.println("ID Usuário inserido: " + idUsuarioInserido);
            System.out.println("imagemCapturada é NULL? " + (imagemCapturada == null ? "SIM" : "NÃO"));
            if (imagemCapturada != null) {
                System.out.println("  - Dimensões: " + imagemCapturada.getWidth() + "x" + imagemCapturada.getHeight());
            }

            // Se uma foto foi capturada, salvar os dados faciais
            if (imagemCapturada != null) {
                System.out.println("✅ Foto encontrada - chamando salvarDadosFaciais...");
                salvarDadosFaciais(idUsuarioInserido);
            } else {
                System.out.println("⚠️ Nenhuma foto capturada - dados faciais não serão salvos");
            }

            mostrarAlerta("Sucesso", "Usuário cadastrado com sucesso!");
            
            // Fechar a janela após inserir os dados com sucesso
            fecharJanela();

        } catch (IllegalArgumentException e) {
            mostrarAlerta("Erro", e.getMessage());
        }
    }

    public void ativarModoEdicao() {
        modoEdicao = true;
        comboUsuarios.setVisible(true);
        nome.setPrefWidth(204);
        nome.setLayoutX(145);
        comboUsuarios.setPrefWidth(210);
        comboUsuarios.setLayoutX(370);
        btnCadastrar.setText("Salvar alterações");

        carregarUsuariosNaComboBox();

        comboUsuarios.setOnAction(e -> {
            Usuario u = comboUsuarios.getValue();
            if (u != null) {
                preencherCamposComUsuario(u);
            }
        });
    }

    private void carregarUsuariosNaComboBox() {
        List<Usuario> lista = UsuarioDAO.buscarTodosUsuarios();
        comboUsuarios.setItems(FXCollections.observableArrayList(lista));

        comboUsuarios.setConverter(new StringConverter<>() {
            @Override
            public String toString(Usuario u) {
                return u == null ? "" : u.getNome();
            }

            @Override
            public Usuario fromString(String s) {
                return null;
            }
        });
    }

    private void salvarAlteracoes() {
        if (!validarCpf(cpf.getText())) {
            return;
        }

        try {
            usuarioEditando.setNome(nome.getText());
            usuarioEditando.setCpf(cpf.getText().replaceAll("\\D", ""));

            usuarioEditando.setNacionalidade(nacionalidade.getText());
            usuarioEditando.setDataNascimento(Date.valueOf(dataNascimento.getValue()));
            // criadoEm não é editável, mantém o valor original
            // usuarioEditando.setCriadoEm(Date.valueOf(criadoEm.getValue()));
            usuarioEditando.setEndereco(endereco.getText());
            usuarioEditando.setBairro(bairro.getText());
            usuarioEditando.setCidade(cidade.getText());
            usuarioEditando.setUf(uf.getText().toUpperCase());
            usuarioEditando.setObservacao(observacao.getText());
            usuarioEditando.setTelefone(telefone.getText());

            boolean sucesso = UsuarioDAO.atualizar(usuarioEditando);

            if (sucesso) {
                // Se uma nova foto foi capturada, salva ela
                if (imagemCapturada != null) {
                    salvarDadosFaciais(usuarioEditando.getIdUsuario());
                }
                mostrarAlerta("Sucesso", "Usuário atualizado com sucesso!");
            } else {
                mostrarAlerta("Erro", "Falha ao atualizar usuário.");
            }

        } catch (IllegalArgumentException e) {
            mostrarAlerta("Erro", e.getMessage());
        }
    }

    private boolean validarCpf(String cpfTexto) {
        String cpfLimpo = cpfTexto.replaceAll("\\D", "");

        if (cpfLimpo.length() != 11 || !ValidadorCPF.isCPFValido(cpfLimpo)) {
            mostrarAlerta("Erro", "CPF inválido.");
            return false;
        }

        boolean cpfExiste = UsuarioDAO.cpfExiste(cpfLimpo);

        if (modoEdicao && usuarioEditando != null) {
            if (cpfLimpo.equals(usuarioEditando.getCpf())) {
                cpfExiste = false; // Ignora se é o mesmo usuário
            }
        }

        if (cpfExiste) {
            mostrarAlerta("Erro", "CPF já cadastrado.");
            return false;
        }

        return true;
    }

    /**
     * Método público para carregar usuário para edição (chamado por outras telas)
     */
    public void carregarUsuarioParaEdicao(Usuario u) {
        if (u == null) {
            return;
        }
        // Configura a tela para modo edição
        btnCadastrar.setText("Salvar alterações");
        comboUsuarios.setVisible(true);
        nome.setPrefWidth(204);
        nome.setLayoutX(145);
        if (comboUsuarios != null) {
            comboUsuarios.setPrefWidth(210);
            comboUsuarios.setLayoutX(370);
            carregarUsuariosNaComboBox();
        }
        // Preenche os campos com os dados do usuário
        preencherCamposComUsuario(u);
    }

    private void preencherCamposComUsuario(Usuario u) {
        usuarioEditando = u;
        nome.setText(u.getNome());
        cpf.setText(u.getCpf());
        nacionalidade.setText(u.getNacionalidade());
        dataNascimento.setValue(convertToLocalDate(u.getDataNascimento()));
        criadoEm.setText(u.getCriadoEm() != null ? 
            new java.text.SimpleDateFormat("dd/MM/yyyy").format(u.getCriadoEm()) : "");
        endereco.setText(u.getEndereco());
        bairro.setText(u.getBairro());
        cidade.setText(u.getCidade());
        uf.setText(u.getUf());
        observacao.setText(u.getObservacao());
        telefone.setText(u.getTelefone());

        // Carrega a foto do usuário se existir
        carregarFotoDoArquivo(u.getIdUsuario());
    }

    private LocalDate convertToLocalDate(java.util.Date d) {
        if (d == null) {
            return null;
        }

        if (d instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        return d.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private void limitarUF() {
        uf.textProperty().addListener((obs, ov, nv) -> {
            if (nv.length() > 2 || !nv.matches("[a-zA-Z]*")) {
                uf.setText(ov);
            }
        });
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensagem, ButtonType.OK);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }

    private void fecharJanela() {
        Stage stage = (Stage) btnCadastrar.getScene().getWindow();
        stage.close();
    }

    private void configurarDataAtual() {
        // Configurar data atual no campo criadoEm
        java.time.LocalDate hoje = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");
        criadoEm.setText(hoje.format(formatter));
    }

    private void configurarFormatoBrasileiro() {
        // Configurar formato brasileiro para DatePicker
        dataNascimento.setConverter(new javafx.util.StringConverter<java.time.LocalDate>() {
            private final java.time.format.DateTimeFormatter dateFormatter = 
                java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy");

            @Override
            public String toString(java.time.LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public java.time.LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return java.time.LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });
    }

    private void limparErro(Control c) {
        c.getStyleClass().remove("erro-campo");
    }

    private void configurarListenersRemocaoErro() {
        telefone.textProperty().addListener((o, ov, nv) -> limparErro(telefone));
        cep.textProperty().addListener((o, ov, nv) -> limparErro(cep));
        nome.textProperty().addListener((o, ov, nv) -> limparErro(nome));
        cpf.textProperty().addListener((o, ov, nv) -> limparErro(cpf));
        nacionalidade.textProperty().addListener((o, ov, nv) -> limparErro(nacionalidade));
        dataNascimento.valueProperty().addListener((o, ov, nv) -> limparErro(dataNascimento));
        endereco.textProperty().addListener((o, ov, nv) -> limparErro(endereco));
        bairro.textProperty().addListener((o, ov, nv) -> limparErro(bairro));
        cidade.textProperty().addListener((o, ov, nv) -> limparErro(cidade));
        uf.textProperty().addListener((o, ov, nv) -> limparErro(uf));
    }

    private void carregarPenas() {
        comboTipoPena.getItems().clear();
        mapNomeParaIdInstituicao.clear();

        List<Pena> penas = PenaDAO.buscarTodasPenas();
        for (Pena p : penas) {
            comboTipoPena.getItems().add(p.getDescricao());
            mapNomeParaIdInstituicao.put(p.getDescricao(), p.getIdPena());
        }
        comboTipoPena.getItems().add("Adicionar nova pena...");
    }

    private void carregarInstituicoes() {
        comboInstituicao.getItems().clear();
        mapNomeParaIdInstituicao.clear();

        List<Instituicao> insts = InstituicaoDAO.buscarTodasInstituicoes();
        for (Instituicao inst : insts) {
            comboInstituicao.getItems().add(inst.getNome());
            mapNomeParaIdInstituicao.put(inst.getNome(), inst.getIdInstituicao());
        }
        comboInstituicao.getItems().add("Adicionar nova instituicao...");
    }

    private void abrirCadastroInstituicao() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/com/mycompany/cpma/cadastroInstituicaoView.fxml"));
            Stage st = new Stage();
            st.setTitle("Cadastrar Instituicao");
            st.setScene(new Scene(root));
            st.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Configura o listener para buscar CEP automaticamente quando o campo for
     * preenchido
     */
    private void configurarBuscaCEP() {
        // Listener para busca automática quando o CEP perde o foco
        cep.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Quando perde o foco
                String cepLimpo = cep.getText().replaceAll("\\D", "");
                if (cepLimpo.length() == 8) {
                    buscarCEPComTask(cepLimpo);
                }
            }
        });

        // Botão de busca manual
        btnBuscarCep.setOnAction(e -> {
            String cepLimpo = cep.getText().replaceAll("\\D", "");
            if (cepLimpo.length() == 8) {
                buscarCEPComTask(cepLimpo);
            } else {
                lblStatusCep.setText("CEP deve ter 8 dígitos");
                lblStatusCep.setStyle("-fx-text-fill: #ef4444;");
            }
        });

        // Formatação do CEP usando TextFormatter
        cep.setTextFormatter(new TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d{0,5}-?\\d{0,3}")) {
                return change;
            }
            return null;
        }));    
    }

    /**
     * Busca informações do CEP na API ViaCEP
     */
    private void buscarCEPComTask(String cep) {
        lblStatusCep.setText("Buscando CEP...");
        lblStatusCep.setStyle("-fx-text-fill: #06b6d4;");
        btnBuscarCep.setDisable(true);

        // Cria uma Task para a operação de rede
        Task<JSONObject> task = new Task<>() {
            @Override
            protected JSONObject call() throws Exception {
                // Este código é executado numa thread de segundo plano
                URL url = new URL("https://viacep.com.br/ws/" + cep + "/json/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Falha : HTTP error code : " + conn.getResponseCode());
                }

                // Ler a resposta
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        response.append(line);
                    }
                    return new JSONObject(response.toString());
                } finally {
                    conn.disconnect();
                }
            }
        };

        // O que fazer QUANDO a task for bem-sucedida
        task.setOnSucceeded(e -> {
            JSONObject enderecoJson = task.getValue();
            if (enderecoJson.has("erro")) {
                lblStatusCep.setText("CEP não encontrado");
                lblStatusCep.setStyle("-fx-text-fill: #ef4444;");
            } else {
                preencherCamposEnderecoComJson(enderecoJson);
                lblStatusCep.setText("CEP encontrado com sucesso!");
                lblStatusCep.setStyle("-fx-text-fill: #10b981;");
            }
            btnBuscarCep.setDisable(false);
            setCarregando(false); // Esconde o indicador de "a carregar"
        });

        // O que fazer SE a task falhar
        task.setOnFailed(e -> {
            lblStatusCep.setText("Erro ao buscar CEP");
            lblStatusCep.setStyle("-fx-text-fill: #ef4444;");
            btnBuscarCep.setDisable(false);
            mostrarAlerta("Erro de Rede", "Não foi possível conectar à API do ViaCEP.");
            setCarregando(false);
            task.getException().printStackTrace(); // Para depuração
        });

        // Inicia o processo de "a carregar" e a task
        setCarregando(true);
        new Thread(task).start();
    }

    private void preencherCamposEnderecoComJson(JSONObject json) {
        // Platform.runLater não é necessário aqui porque onSucceeded já corre na thread
        // da UI
        endereco.setText(json.optString("logradouro", ""));
        bairro.setText(json.optString("bairro", ""));
        cidade.setText(json.optString("localidade", ""));
        uf.setText(json.optString("uf", ""));
    }

    /**
     * Controla a visibilidade do indicador de "a carregar" e desativa/ativa os
     * campos.
     */
    private void setCarregando(boolean carregando) {
        if (loadingCep != null) {
            loadingCep.setVisible(carregando);
        }
        endereco.setDisable(carregando);
        bairro.setDisable(carregando);
        cidade.setDisable(carregando);
        uf.setDisable(carregando);
    }

    /**
     * Salva os dados faciais do usuário no banco de dados
     * Armazena a imagem como BLOB no campo imagem_rosto e os embeddings no campo descritores_faciais
     */
    private void salvarDadosFaciais(int idUsuario) {
        try {
            if (imagemCapturada == null) {
                System.err.println("❌ Nenhuma imagem capturada para processar");
                return;
            }

            System.out.println("=== SALVANDO DADOS FACIAIS PARA USUÁRIO ID: " + idUsuario + " ===");
            System.out.println("  - Dimensões da imagem: " + imagemCapturada.getWidth() + "x" + imagemCapturada.getHeight());
            System.out.println("  - Tipo da imagem: " + imagemCapturada.getType());

            // 1. Extrai descritores faciais (embeddings) da imagem capturada
            String descritores = null;
            try {
                System.out.println("Extraindo descritores faciais (embeddings) da imagem...");
                descritores = reconhecimentoFacial.extrairDescritoresFaciais(imagemCapturada);
                System.out.println("✅ Descritores faciais extraídos: " + (descritores != null && !descritores.isEmpty() ? "OK" : "VAZIO"));
                if (descritores != null && !descritores.isEmpty()) {
                    System.out.println("  - Tamanho dos descritores: " + descritores.length() + " caracteres");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Aviso: Erro ao extrair descritores faciais: " + e.getMessage());
                e.printStackTrace();
                descritores = "";
            }

            // 2. Converte a imagem para byte[] para salvar no banco como BLOB
            byte[] imagemBytes = null;
            try {
                System.out.println("Convertendo imagem para byte[] para salvar no banco...");
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                boolean written = ImageIO.write(imagemCapturada, "jpg", baos);
                if (written) {
                    imagemBytes = baos.toByteArray();
                    System.out.println("✅ Imagem convertida para byte[]: " + imagemBytes.length + " bytes");
                } else {
                    System.err.println("❌ Erro ao converter imagem para byte[]");
                }
            } catch (Exception e) {
                System.err.println("⚠️ Aviso: Erro ao converter imagem para byte[]: " + e.getMessage());
                e.printStackTrace();
            }

            // 3. Verifica se já existe dados faciais para este usuário
            DadosFaciais dadosFaciaisExistentes = dadosFaciaisDAO.buscarPorUsuario(idUsuario);
            
            DadosFaciais dadosFaciais;
            boolean sucesso;
            
            if (dadosFaciaisExistentes != null) {
                // Atualiza registro existente
                System.out.println("Atualizando dados faciais existentes para o usuário ID: " + idUsuario);
                dadosFaciais = dadosFaciaisExistentes;
                dadosFaciais.setDescritoresFaciais(descritores != null ? descritores : "");
                dadosFaciais.setImagemRosto(imagemBytes); // Atualiza a imagem no banco
                dadosFaciais.setDataAtualizacao(new java.sql.Date(System.currentTimeMillis()));
                dadosFaciais.setAtivo(true);
                sucesso = dadosFaciaisDAO.atualizar(dadosFaciais);
            } else {
                // Cria novo registro
                System.out.println("Criando novos dados faciais para o usuário ID: " + idUsuario);
                dadosFaciais = new DadosFaciais();
                dadosFaciais.setFkUsuarioIdUsuario(idUsuario);
                dadosFaciais.setDescritoresFaciais(descritores != null ? descritores : "");
                dadosFaciais.setImagemRosto(imagemBytes); // Salva a imagem no banco como BLOB
                dadosFaciais.setCriadoEm(new java.sql.Date(System.currentTimeMillis()));
                dadosFaciais.setDataAtualizacao(new java.sql.Date(System.currentTimeMillis()));
                dadosFaciais.setAtivo(true);
                sucesso = dadosFaciaisDAO.cadastrar(dadosFaciais);
            }

            if (sucesso) {
                System.out.println("✅ Dados faciais salvos com sucesso para o usuário ID: " + idUsuario);
                System.out.println("  - Imagem no banco (campo imagem_rosto): " + (imagemBytes != null ? "SIM (" + imagemBytes.length + " bytes)" : "NÃO"));
                System.out.println("  - Embeddings salvos (campo descritores_faciais): " + (descritores != null && !descritores.isEmpty() ? "SIM" : "NÃO"));
            } else {
                System.err.println("❌ Erro ao salvar dados faciais para o usuário ID: " + idUsuario);
                mostrarAlerta("Erro", "Erro ao salvar dados faciais no banco de dados.");
            }

        } catch (Exception e) {
            System.err.println("❌ Erro ao processar dados faciais: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao processar dados faciais: " + e.getMessage());
        }
    }

    /**
     * Salva a foto em arquivo na pasta fotos-apenados
     */
    private String salvarFotoEmArquivo(int idUsuario) {
        try {
            if (imagemCapturada == null) {
                System.err.println("Nenhuma imagem capturada para salvar");
                return null;
            }

            // Cria o diretório se não existir
            java.io.File diretorioFotos = new java.io.File("fotos-apenados");
            if (!diretorioFotos.exists()) {
                diretorioFotos.mkdirs();
                System.out.println("Diretório fotos-apenados criado");
            }

            // Gera nome do arquivo: usuario_ID_timestamp.jpg
            String timestamp = String.valueOf(System.currentTimeMillis());
            String nomeArquivo = "usuario_" + idUsuario + "_" + timestamp + ".jpg";
            java.io.File arquivoFoto = new java.io.File(diretorioFotos, nomeArquivo);

            // Salva a imagem
            boolean salvo = ImageIO.write(imagemCapturada, "jpg", arquivoFoto);

            if (salvo) {
                String caminhoCompleto = arquivoFoto.getAbsolutePath();
                System.out.println("Foto salva com sucesso: " + caminhoCompleto);
                return caminhoCompleto;
            } else {
                System.err.println("Erro ao salvar a imagem");
                return null;
            }

        } catch (Exception e) {
            System.err.println("Erro ao salvar foto em arquivo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Carrega a foto do usuário do banco de dados (campo imagem_rosto byte[])
     * Carrega apenas do banco de dados, sem fallback para arquivos
     */
    private void carregarFotoDoArquivo(int idUsuario) {
        try {
            System.out.println("=== CARREGANDO FOTO PARA USUÁRIO ID: " + idUsuario + " ===");
            
            // Carrega do banco de dados (campo imagem_rosto byte[])
            System.out.println("Carregando foto do banco de dados (campo imagem_rosto)...");
            DadosFaciais dadosFaciais = dadosFaciaisDAO.buscarPorUsuario(idUsuario);
            if (dadosFaciais != null && dadosFaciais.getImagemRosto() != null && dadosFaciais.getImagemRosto().length > 0) {
                try {
                    // Converte byte[] para BufferedImage
                    byte[] imagemBytes = dadosFaciais.getImagemRosto();
                    System.out.println("✅ Bytes encontrados no banco: " + imagemBytes.length + " bytes");
                    
                    BufferedImage imagemDoBanco = bytesParaBufferedImage(imagemBytes);
                    if (imagemDoBanco != null) {
                        System.out.println("✅ Foto convertida do banco de dados (byte[])");
                        System.out.println("  - Dimensões: " + imagemDoBanco.getWidth() + "x" + imagemDoBanco.getHeight());
                        
                        // Converte e exibe na ImageView
                        Image imagePreview = converterBufferedImageParaImage(imagemDoBanco);
                        
                        if (imagePreview != null && !imagePreview.isError()) {
                            Platform.runLater(() -> {
                                foto.setImage(imagePreview);
                                foto.setFitWidth(120.0);
                                foto.setFitHeight(120.0);
                                foto.setPreserveRatio(true);
                                foto.setSmooth(true);
                                foto.setCache(true);
                                foto.setVisible(true);
                                
                                System.out.println("✅ Foto exibida no ImageView do banco de dados");
                            });

                            // Armazena para possível atualização
                            this.imagemCapturada = imagemDoBanco;
                            return; // Sucesso ao carregar do banco
                        } else {
                            System.err.println("❌ Erro ao converter BufferedImage para Image JavaFX");
                        }
                    } else {
                        System.err.println("❌ Erro ao converter bytes para BufferedImage");
                    }
                } catch (Exception e) {
                    System.err.println("❌ Erro ao carregar foto do banco de dados: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("⚠️ Nenhuma foto encontrada no banco de dados (campo imagem_rosto está NULL ou vazio) para o usuário ID: " + idUsuario);
            }

        } catch (Exception e) {
            System.err.println("❌ Erro ao carregar foto: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Converte byte[] para BufferedImage
     * 
     * @param imagemBytes Array de bytes da imagem
     * @return BufferedImage ou null se houver erro
     */
    private BufferedImage bytesParaBufferedImage(byte[] imagemBytes) {
        if (imagemBytes == null || imagemBytes.length == 0) {
            return null;
        }
        
        try {
            // Cria um ByteArrayInputStream a partir dos bytes
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(imagemBytes);
            
            // Lê a imagem usando ImageIO
            BufferedImage imagem = ImageIO.read(bais);
            bais.close();
            
            if (imagem != null) {
                System.out.println("✅ BufferedImage criada com sucesso: " + imagem.getWidth() + "x" + imagem.getHeight());
                return imagem;
            } else {
                System.err.println("❌ ImageIO.read() retornou null - bytes podem estar corrompidos ou formato inválido");
                return null;
            }
        } catch (Exception e) {
            System.err.println("❌ Erro ao converter bytes para BufferedImage: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}
