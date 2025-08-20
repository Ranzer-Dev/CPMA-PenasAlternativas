package controller;

import dao.InstituicaoDAO;
import dao.PenaDAO;
import dao.UsuarioDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Instituicao;
import model.Pena;
import model.Usuario;
import util.HashUtil;
import util.ValidadorCPF;

import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CadastrarUsuarioController {

    @FXML private TextField cep;
    @FXML private TextField codigo;
    @FXML private TextField telefone;
    @FXML private ComboBox<String> comboTipoPena;
    @FXML private ComboBox<Usuario> comboUsuarios;
    @FXML private DatePicker dataNascimento, dataCadastro;
    @FXML private TextField nome, cpf, senha, nacionalidade;
    @FXML private TextField endereco, bairro ,cidade, uf;
    @FXML private TextArea observacao;
    @FXML private ComboBox<String> comboInstituicao;
    @FXML private Button btnCadastrar;

    private Usuario usuarioEditando = null;
    private boolean modoEdicao = false;
    private int idUsuarioInserido;
    private final Map<String, Integer> mapNomeParaIdInstituicao = new HashMap<>();

    @FXML
    public void initialize() {
        limitarUF();
        configurarListenersRemocaoErro();

        btnCadastrar.setOnAction(event -> {
            if (modoEdicao) {
                salvarAlteracoes();
            } else {
                cadastrarUsuario();
            }
        });

        telefone.setTextFormatter(new TextFormatter<>(c ->
                c.getControlNewText().matches("[0-9()\\-]*") ? c : null));

        cep.setTextFormatter(new TextFormatter<>(c ->
                c.getControlNewText().matches("[0-9()\\-]*") ? c : null));
    }

    public static int cadastrarUsuario(
            String nome, String cpf, String senha,
            String nacionalidade, LocalDate dataNascimento, LocalDate dataCadastro,
            String endereco, String bairro, String cidade, String uf,
            String observacao, String telefone, String cep, String codigo) {

        if (nome == null || nome.trim().isEmpty() || !nome.matches("[a-zA-ZÀ-ú\\s]+"))
            throw new IllegalArgumentException("Nome inválido.");

        String cpfLimpo = cpf.replaceAll("\\D", "");
        if (cpfLimpo.length() != 11 || !ValidadorCPF.isCPFValido(cpfLimpo))
            throw new IllegalArgumentException("CPF inválido.");

        String foneLimpo = telefone.replaceAll("\\D", "");
        if (foneLimpo.length() < 8 || foneLimpo.length() > 15)
            throw new IllegalArgumentException("Telefone inválido.");

        if (UsuarioDAO.cpfExiste(cpfLimpo))
            throw new IllegalArgumentException("CPF já cadastrado.");

        if (senha == null || senha.trim().isEmpty())
            throw new IllegalArgumentException("Senha vazia.");

        if (nacionalidade == null || nacionalidade.trim().isEmpty())
            throw new IllegalArgumentException("Nacionalidade inválida.");

        if (dataNascimento == null || dataNascimento.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("Data de nascimento inválida.");

        if (endereco == null || endereco.trim().isEmpty())
            throw new IllegalArgumentException("Endereço inválido.");

        if (bairro == null || bairro.trim().isEmpty())
            throw new IllegalArgumentException("Bairro inválido.");

        if (cidade == null || cidade.trim().isEmpty())
            throw new IllegalArgumentException("Cidade inválida.");

        if (uf == null || !uf.trim().matches("^[A-Z]{2}$"))
            throw new IllegalArgumentException("UF inválida.");

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
            if (!validarCpf(cpf.getText())) return;

            idUsuarioInserido = cadastrarUsuario(
                    nome.getText(), cpf.getText(), senha.getText(), nacionalidade.getText(),
                    dataNascimento.getValue(), dataCadastro.getValue(), endereco.getText(),
                    bairro.getText(), cidade.getText(), uf.getText(), observacao.getText(),
                    telefone.getText().trim(), cep.getText(), codigo.getText());

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
            if (u != null) preencherCamposComUsuario(u);
        });
    }

    private void carregarUsuariosNaComboBox() {
        List<Usuario> lista = UsuarioDAO.buscarTodosUsuarios();
        comboUsuarios.setItems(FXCollections.observableArrayList(lista));

        comboUsuarios.setConverter(new StringConverter<>() {
            @Override public String toString(Usuario u) { return u == null ? "" : u.getNome(); }
            @Override public Usuario fromString(String s) { return null; }
        });
    }

    private void salvarAlteracoes() {
        if (!validarCpf(cpf.getText())) return;

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
        if (d == null) return null;

        if (d instanceof java.sql.Date sqlDate) {
            return sqlDate.toLocalDate();
        }

        return d.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private void limitarUF() {
        uf.textProperty().addListener((obs, ov, nv) -> {
            if (nv.length() > 2 || !nv.matches("[a-zA-Z]*")) uf.setText(ov);
        });
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensagem, ButtonType.OK);
        a.setTitle(titulo); a.setHeaderText(null); a.showAndWait();
    }

    private void marcarErro(Control c) {
        if (!c.getStyleClass().contains("erro-campo")) c.getStyleClass().add("erro-campo");
    }
    private void limparErro(Control c) { c.getStyleClass().remove("erro-campo"); }

    private void configurarListenersRemocaoErro() {
        telefone.textProperty().addListener((o,ov,nv)->limparErro(telefone));
        cep.textProperty().addListener((o,ov,nv)->limparErro(cep));
        nome.textProperty().addListener((o,ov,nv)->limparErro(nome));
        cpf.textProperty().addListener((o,ov,nv)->limparErro(cpf));
        senha.textProperty().addListener((o,ov,nv)->limparErro(senha));
        nacionalidade.textProperty().addListener((o,ov,nv)->limparErro(nacionalidade));
        dataNascimento.valueProperty().addListener((o,ov,nv)->limparErro(dataNascimento));
        endereco.textProperty().addListener((o,ov,nv)->limparErro(endereco));
        bairro.textProperty().addListener((o,ov,nv)->limparErro(bairro));
        cidade.textProperty().addListener((o,ov,nv)->limparErro(cidade));
        uf.textProperty().addListener((o,ov,nv)->limparErro(uf));
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
            Parent root = FXMLLoader.load(getClass().getResource("/resources/view/cadastroInstituicaoView.fxml"));
            Stage st = new Stage();
            st.setTitle("Cadastrar Instituicao");
            st.setScene(new Scene(root));
            st.show();
        } catch (IOException e) { e.printStackTrace(); }
    }
}
