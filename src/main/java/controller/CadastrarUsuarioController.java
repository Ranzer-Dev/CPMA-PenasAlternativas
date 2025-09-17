package controller;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javafx.stage.Modality;
import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacv.*;

import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.json.JSONObject;
import org.opencv.core.Core;

import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import dao.DadosFaciaisDAO;
import dao.InstituicaoDAO;
import dao.PenaDAO;
import dao.UsuarioDAO;
import javafx.application.Platform;
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
import javafx.scene.image.ImageView; // Supondo que o tenha no FXML
import javafx.stage.Stage; // Requer a biblioteca org.json no projeto
import javafx.util.StringConverter;
import model.DadosFaciais;
import model.Instituicao;
import model.Pena;
import model.Usuario;
import util.HashUtil;
import util.ReconhecimentoFacial;
import util.ValidadorCPF;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.RectVector;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.global.opencv_imgcodecs;

import javax.imageio.ImageIO;


public class CadastrarUsuarioController {
    @FXML
    private TextField cep;
    @FXML
    private TextField codigo;
    @FXML
    private TextField telefone;
    @FXML
    private ComboBox<String> comboTipoPena;
    @FXML
    private ComboBox<Usuario> comboUsuarios;
    @FXML
    private DatePicker dataNascimento, dataCadastro;
    @FXML
    private TextField nome, cpf, senha, nacionalidade;
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
    private ProgressIndicator loadingCep;
    private FrameGrabber camera;
    private ScheduledExecutorService timer;
    private boolean cameraAtiva = false;
    private Mat frameCapturado;
    private OpenCVFrameConverter.ToMat converterParaMat;

    // Variáveis para reconhecimento facial
    private ReconhecimentoFacial reconhecimentoFacial;
    private DadosFaciaisDAO dadosFaciaisDAO;
    private BufferedImage imagemCapturada;

    private Usuario usuarioEditando = null;
    private boolean modoEdicao = false;
    private int idUsuarioInserido;
    private final Map<String, Integer> mapNomeParaIdInstituicao = new HashMap<>();

    @FXML
    public void initialize() {
        System.out.println("Inicializando CadastrarUsuarioController...");

        // Inicializar reconhecimento facial
        reconhecimentoFacial = new ReconhecimentoFacial();
        dadosFaciaisDAO = new DadosFaciaisDAO();

        limitarUF();
        configurarListenersRemocaoErro();

        btnCadastrar.setOnAction(event -> {
            if (modoEdicao) {
                salvarAlteracoes();
            } else {
                cadastrarUsuario();
            }
        });

        telefone.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("[0-9()\\-]*") ? c : null));

        cep.setTextFormatter(new TextFormatter<>(c -> c.getControlNewText().matches("[0-9\\-]*") ? c : null));

        // Configurar listener para buscar CEP automaticamente
        System.out.println("Configurando busca de CEP...");
        configurarBuscaCEP();
        System.out.println("Busca de CEP configurada!");
    }

    @FXML
    private void iniciarCamera(ActionEvent event) {
        try {
            // Carrega o FXML da janela da câmera
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/cpma/cameraView.fxml"));
            Parent root = loader.load();

            // Pega o controller da janela da câmera
            CameraController cameraController = loader.getController();

            // Cria uma nova janela (Stage)
            Stage cameraStage = new Stage();
            cameraStage.setTitle("Capturar Foto");
            cameraStage.setScene(new Scene(root));

            // Configura para ser uma janela modal (bloqueia a janela de cadastro)
            cameraStage.initModality(Modality.APPLICATION_MODAL);

            // Define uma ação para quando a janela for fechada
            cameraStage.setOnHidden(e -> {
                // Pega a imagem que foi capturada
                BufferedImage imagem = cameraController.getImagemCapturada();
                if (imagem != null) {
                    // Guarda a imagem para salvar no banco
                    this.imagemCapturada = imagem;

                    // Usa o novo método para converter a imagem
                    Image imagePreview = converterBufferedImageParaImage(imagem);
                    if (imagePreview != null) {
                        foto.setImage(imagePreview);
                    }

                    mostrarAlerta("Sucesso", "Foto capturada com sucesso!");
                }
            });

            // Mostra a janela e espera ela ser fechada
            cameraStage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Não foi possível abrir a janela da câmera.");
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
     * @param bufferedImage A imagem a ser convertida.
     * @return Uma imagem compatível com JavaFX ou null se ocorrer um erro.
     */
    private Image converterBufferedImageParaImage(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            return null;
        }
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            // Escreve a imagem como um PNG em um fluxo de bytes na memória
            ImageIO.write(bufferedImage, "png", outputStream);
            // Cria uma Image do JavaFX a partir do fluxo de bytes
            return new Image(new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
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
     * Converte um objeto Mat (Bytedeco) para BufferedImage (AWT).
     * Necessário para o reconhecimento facial.
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
    /**
     * Converte um objeto Mat (OpenCV) para um objeto Image (JavaFX).
     */
//    private Image matToImage(Mat frame) {
//        try {
//            MatOfByte buffer = new MatOfByte();
//            Imgcodecs.imencode(".png", frame, buffer);
//            return new Image(new ByteArrayInputStream(buffer.toArray()));
//        } catch (Exception e) {
//            System.err.println("Não foi possível converter o Mat para Image: " + e);
//            return null;
//        }
//    }

    /**
     * Converte um objeto Mat (OpenCV) para BufferedImage (AWT).
     */
//    private BufferedImage matToBufferedImage(Mat frame) {
//        try {
//            MatOfByte buffer = new MatOfByte();
//            Imgcodecs.imencode(".jpg", frame, buffer);
//            byte[] byteArray = buffer.toArray();
//            return javax.imageio.ImageIO.read(new ByteArrayInputStream(byteArray));
//        } catch (Exception e) {
//            System.err.println("Não foi possível converter o Mat para BufferedImage: " + e);
//            return null;
//        }
//    }



    public static int cadastrarUsuario(
            String nome, String cpf, String senha,
            String nacionalidade, LocalDate dataNascimento, LocalDate dataCadastro,
            String endereco, String bairro, String cidade, String uf,
            String observacao, String telefone, String cep, String codigo) {

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

        if (senha == null || senha.trim().isEmpty()) {
            throw new IllegalArgumentException("Senha vazia.");
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
        usuario.setSenha(HashUtil.gerarHash(senha.trim()));
        usuario.setNacionalidade(nacionalidade.trim());
        usuario.setDataNascimento(Date.valueOf(dataNascimento));
        usuario.setDataCadastro(Date.valueOf(dataCadastro));
        usuario.setEndereco(endereco.trim());
        usuario.setBairro(bairro.trim());
        usuario.setCidade(cidade.trim());
        usuario.setUf(uf.trim().toUpperCase());
        usuario.setObservacao(observacao == null ? "" : observacao.trim());
        usuario.setFoto("");
        usuario.setTelefone(foneLimpo);
        usuario.setCodigo(codigo);
        usuario.setCep(cep);

        return UsuarioDAO.inserir(usuario);
    }

    private void cadastrarUsuario() {
        try {
            if (!validarCpf(cpf.getText())) {
                return;
            }

            idUsuarioInserido = cadastrarUsuario(
                    nome.getText(), cpf.getText(), senha.getText(), nacionalidade.getText(),
                    dataNascimento.getValue(), dataCadastro.getValue(), endereco.getText(),
                    bairro.getText(), cidade.getText(), uf.getText(), observacao.getText(),
                    telefone.getText().trim(), cep.getText(), codigo.getText());

            // Se uma foto foi capturada, salvar os dados faciais
            if (imagemCapturada != null) {
                salvarDadosFaciais(idUsuarioInserido);
            }

            mostrarAlerta("Sucesso", "Usuário cadastrado com sucesso!");

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

            if (!senha.getText().trim().isEmpty()) {
                usuarioEditando.setSenha(HashUtil.gerarHash(senha.getText().trim()));
            }

            usuarioEditando.setNacionalidade(nacionalidade.getText());
            usuarioEditando.setDataNascimento(Date.valueOf(dataNascimento.getValue()));
            usuarioEditando.setDataCadastro(Date.valueOf(dataCadastro.getValue()));
            usuarioEditando.setEndereco(endereco.getText());
            usuarioEditando.setBairro(bairro.getText());
            usuarioEditando.setCidade(cidade.getText());
            usuarioEditando.setUf(uf.getText().toUpperCase());
            usuarioEditando.setObservacao(observacao.getText());
            usuarioEditando.setTelefone(telefone.getText());

            boolean sucesso = UsuarioDAO.atualizar(usuarioEditando);

            if (sucesso) {
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

    private void preencherCamposComUsuario(Usuario u) {
        usuarioEditando = u;
        nome.setText(u.getNome());
        cpf.setText(u.getCpf());
        senha.setText("");
        nacionalidade.setText(u.getNacionalidade());
        dataNascimento.setValue(convertToLocalDate(u.getDataNascimento()));
        dataCadastro.setValue(convertToLocalDate(u.getDataCadastro()));
        endereco.setText(u.getEndereco());
        bairro.setText(u.getBairro());
        cidade.setText(u.getCidade());
        uf.setText(u.getUf());
        observacao.setText(u.getObservacao());
        telefone.setText(u.getTelefone());
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

    private void marcarErro(Control c) {
        if (!c.getStyleClass().contains("erro-campo")) {
            c.getStyleClass().add("erro-campo");
        }
    }

    private void limparErro(Control c) {
        c.getStyleClass().remove("erro-campo");
    }

    private void configurarListenersRemocaoErro() {
        telefone.textProperty().addListener((o, ov, nv) -> limparErro(telefone));
        cep.textProperty().addListener((o, ov, nv) -> limparErro(cep));
        nome.textProperty().addListener((o, ov, nv) -> limparErro(nome));
        cpf.textProperty().addListener((o, ov, nv) -> limparErro(cpf));
        senha.textProperty().addListener((o, ov, nv) -> limparErro(senha));
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

//    public void fecharJanela() {
//        if (cameraAtiva && openCVDisponivel) {
//            pararCamera();
//        }
//        Stage stage = (Stage) btnIniciarCamera.getScene().getWindow();
//        stage.close();
//    }

    /**
     * Configura o listener para buscar CEP automaticamente quando o campo for
     * preenchido
     */
    private void configurarBuscaCEP() {
        cep.focusedProperty().addListener((obs, oldVal, newVal) -> {
            // Se o campo PERDEU o foco (!newVal) e tem um CEP válido...
            if (!newVal) {
                String cepLimpo = cep.getText().replaceAll("\\D", "");
                if (cepLimpo.length() == 8) {
                    buscarCEPComTask(cepLimpo);
                }
            }
        });
    }

    /**
     * Busca informações do CEP na API ViaCEP
     */
    private void buscarCEPComTask(String cep) {
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
                mostrarAlerta("CEP não encontrado", "O CEP informado não foi encontrado.");
            } else {
                preencherCamposEnderecoComJson(enderecoJson);
            }
            setCarregando(false); // Esconde o indicador de "a carregar"
        });

        // O que fazer SE a task falhar
        task.setOnFailed(e -> {
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
     */
    private void salvarDadosFaciais(int idUsuario) {
        try {
            // Extrai descritores faciais da imagem capturada
            String descritores = reconhecimentoFacial.extrairDescritoresFaciais(imagemCapturada);

            if (descritores != null && !descritores.isEmpty()) {
                // Cria objeto DadosFaciais
                DadosFaciais dadosFaciais = new DadosFaciais();
                dadosFaciais.setFkUsuarioIdUsuario(idUsuario);
                dadosFaciais.setDescritoresFaciais(descritores);

                // Salva no banco
                if (dadosFaciaisDAO.cadastrar(dadosFaciais)) {
                    System.out.println("Dados faciais salvos com sucesso para o usuário ID: " + idUsuario);
                } else {
                    System.err.println("Erro ao salvar dados faciais para o usuário ID: " + idUsuario);
                }
            } else {
                System.err.println("Não foi possível extrair descritores faciais da imagem");
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar dados faciais: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
