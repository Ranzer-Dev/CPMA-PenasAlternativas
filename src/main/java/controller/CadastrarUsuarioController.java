package controller;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONObject;
import org.opencv.core.Core;
import org.opencv.core.Mat;
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

public class CadastrarUsuarioController {

    // Static block to load OpenCV native library
    private static boolean openCVDisponivel = false;

    static {
        try {
            System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
            openCVDisponivel = true;
            System.out.println("OpenCV carregado com sucesso!");
        } catch (UnsatisfiedLinkError e) {
            openCVDisponivel = false;
            System.err.println("Erro ao carregar OpenCV: " + e.getMessage());
            System.err.println("A funcionalidade de câmera não estará disponível.");
        }
    }

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

    private VideoCapture camera;
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

        telefone.setTextFormatter(new TextFormatter<>(c
                -> c.getControlNewText().matches("[0-9()\\-]*") ? c : null));

        cep.setTextFormatter(new TextFormatter<>(c
                -> c.getControlNewText().matches("[0-9\\-]*") ? c : null));

        // Configurar listener para buscar CEP automaticamente
        System.out.println("Configurando busca de CEP...");
        configurarBuscaCEP();
        System.out.println("Busca de CEP configurada!");
    }

    /**
     * Ação do botão "Tirar Foto". Inicia ou para a webcam.
     */
    @FXML
    private void iniciarCamera(ActionEvent event) {
        if (!openCVDisponivel) {
            mostrarAlerta("Erro", "OpenCV não está disponível. A funcionalidade de câmera não pode ser usada.");
            return;
        }

        if (!cameraAtiva) {
            try {
                // Inicia a câmera
                camera = new VideoCapture(0); // 0 para a câmera padrão

                if (camera.isOpened()) {
                    cameraAtiva = true;

                    // Cria um serviço para ficar pegando os frames da câmera
                    Runnable frameGrabber = () -> {
                        Mat frame = new Mat();
                        if (camera.read(frame)) {
                            Image imageToShow = matToImage(frame);
                            Platform.runLater(() -> foto.setImage(imageToShow));
                            frameCapturado = frame; // Guarda o último frame para a captura
                        }
                    };

                    this.timer = Executors.newSingleThreadScheduledExecutor();
                    this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS); // ~30 FPS

                    // Atualiza a UI
                    btnIniciarCamera.setText("Parar Câmera");
                    btnCapturar.setVisible(true);
                } else {
                    mostrarAlerta("Erro", "Não foi possível abrir a câmera. Verifique se ela está conectada e não está sendo usada por outro aplicativo.");
                }
            } catch (Exception e) {
                mostrarAlerta("Erro", "Erro ao inicializar a câmera: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Para a câmera
            pararCamera();
        }
    }

    /**
     * Ação do botão "Capturar". Tira a foto.
     */
    @FXML
    private void capturarFoto(ActionEvent event) {
        if (!openCVDisponivel) {
            mostrarAlerta("Erro", "OpenCV não está disponível. A funcionalidade de câmera não pode ser usada.");
            return;
        }

        if (frameCapturado != null && !frameCapturado.empty()) {
            try {
                // Converte o frame capturado para uma imagem e exibe
                Image fotoCapturada = matToImage(frameCapturado);
                foto.setImage(fotoCapturada);

                // Converte Mat para BufferedImage para processamento facial
                imagemCapturada = matToBufferedImage(frameCapturado);

                // Para a câmera após a captura
                pararCamera();
                System.out.println("Foto capturada com sucesso!");

                // Mostra mensagem de sucesso
                mostrarAlerta("Sucesso", "Foto capturada com sucesso! Os dados faciais serão salvos junto com o cadastro do usuário.");

            } catch (Exception e) {
                mostrarAlerta("Erro", "Erro ao processar a foto: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            mostrarAlerta("Erro", "Nenhuma imagem capturada. Tente novamente.");
        }
    }

    /**
     * Para a execução da câmera e limpa os recursos.
     */
    private void pararCamera() {
        if (this.timer != null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Erro ao parar a captura de frames: " + e.getMessage());
            }
        }
        if (camera != null && camera.isOpened()) {
            camera.release();
        }
        cameraAtiva = false;
        btnIniciarCamera.setText("Tirar Foto");
        btnCapturar.setVisible(false);
    }

    /**
     * Converte um objeto Mat (OpenCV) para um objeto Image (JavaFX).
     */
    private Image matToImage(Mat frame) {
        try {
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", frame, buffer);
            return new Image(new ByteArrayInputStream(buffer.toArray()));
        } catch (Exception e) {
            System.err.println("Não foi possível converter o Mat para Image: " + e);
            return null;
        }
    }

    /**
     * Converte um objeto Mat (OpenCV) para BufferedImage (AWT).
     */
    private BufferedImage matToBufferedImage(Mat frame) {
        try {
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".jpg", frame, buffer);
            byte[] byteArray = buffer.toArray();
            return javax.imageio.ImageIO.read(new ByteArrayInputStream(byteArray));
        } catch (Exception e) {
            System.err.println("Não foi possível converter o Mat para BufferedImage: " + e);
            return null;
        }
    }

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

    public void fecharJanela() {
        if (cameraAtiva && openCVDisponivel) {
            pararCamera();
        }
        Stage stage = (Stage) btnIniciarCamera.getScene().getWindow();
        stage.close();
    }

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
        // Platform.runLater não é necessário aqui porque onSucceeded já corre na thread da UI
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
