package controller;

import dao.UsuarioDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.layout.HBox;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.PauseTransition;
import model.Usuario;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Predicate;

public class ListarApenadosController {

    @FXML
    private TableView<Usuario> tabelaApenados;
    @FXML
    private TableColumn<Usuario, String> colNome;
    @FXML
    private TableColumn<Usuario, String> colCpf;
    @FXML
    private TableColumn<Usuario, String> colDataNasc;
    @FXML
    private TableColumn<Usuario, String> colCidade;
    @FXML
    private TableColumn<Usuario, String> colUf;
    @FXML
    private TableColumn<Usuario, String> colAcoes;
    @FXML
    private TextField campoFiltro;
    @FXML
    private Label labelTotal;
    @FXML
    private Button botaoCopiarCpf;
    @FXML
    private Button botaoCopiarNome;
    @FXML
    private Button botaoFechar;

    private ObservableList<Usuario> listaCompleta;
    private FilteredList<Usuario> listaFiltrada;
    private SimpleDateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");

    @FXML
    private void initialize() {
        configurarTabela();
        carregarApenados();
        configurarFiltro();
        configurarBotoes();
        configurarCopiarAoClicar();
    }

    private void configurarTabela() {
        // Configura as colunas da tabela
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCpf.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            if (usuario != null && usuario.getCpf() != null) {
                String cpf = usuario.getCpf();
                // Formata CPF se não estiver formatado
                if (cpf.length() == 11 && cpf.matches("\\d+")) {
                    cpf = cpf.substring(0, 3) + "." + 
                          cpf.substring(3, 6) + "." + 
                          cpf.substring(6, 9) + "-" + 
                          cpf.substring(9, 11);
                }
                return new javafx.beans.property.SimpleStringProperty(cpf);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        colDataNasc.setCellValueFactory(cellData -> {
            Usuario usuario = cellData.getValue();
            if (usuario != null && usuario.getDataNascimento() != null) {
                try {
                    String dataFormatada = dateFormatter.format(usuario.getDataNascimento());
                    return new javafx.beans.property.SimpleStringProperty(dataFormatada);
                } catch (Exception e) {
                    return new javafx.beans.property.SimpleStringProperty("");
                }
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        colCidade.setCellValueFactory(new PropertyValueFactory<>("cidade"));
        colUf.setCellValueFactory(new PropertyValueFactory<>("uf"));
        
        // Configura coluna de ações com botões Editar e Excluir
        colAcoes.setCellFactory(column -> new TableCell<Usuario, String>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");

            {
                btnEditar.setOnAction(e -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    editarApenado(usuario);
                });
                btnExcluir.setOnAction(e -> {
                    Usuario usuario = getTableView().getItems().get(getIndex());
                    excluirApenado(usuario);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(5, btnEditar, btnExcluir);
                    setGraphic(hbox);
                }
            }
        });
    }

    private void carregarApenados() {
        List<Usuario> usuarios = UsuarioDAO.buscarTodosUsuarios();
        listaCompleta = FXCollections.observableArrayList(usuarios);
        listaFiltrada = new FilteredList<>(listaCompleta, p -> true);
        
        // Ordena a lista por nome
        SortedList<Usuario> listaOrdenada = new SortedList<>(listaFiltrada);
        listaOrdenada.comparatorProperty().bind(tabelaApenados.comparatorProperty());
        
        tabelaApenados.setItems(listaOrdenada);
        atualizarLabelTotal();
    }

    private void configurarFiltro() {
        campoFiltro.textProperty().addListener((observable, oldValue, newValue) -> {
            listaFiltrada.setPredicate(criarPredicadoFiltro(newValue));
            atualizarLabelTotal();
        });
    }

    private Predicate<Usuario> criarPredicadoFiltro(String textoFiltro) {
        return usuario -> {
            if (textoFiltro == null || textoFiltro.isEmpty()) {
                return true;
            }

            String textoLowerCase = textoFiltro.toLowerCase();
            String nome = usuario.getNome() != null ? usuario.getNome().toLowerCase() : "";
            String cpf = usuario.getCpf() != null ? usuario.getCpf().replaceAll("[^0-9]", "") : "";
            String textoFiltroNumerico = textoFiltro.replaceAll("[^0-9]", "");

            // Busca por nome
            if (nome.contains(textoLowerCase)) {
                return true;
            }

            // Busca por CPF (remove pontos e hífens para comparar)
            if (!textoFiltroNumerico.isEmpty() && cpf.contains(textoFiltroNumerico)) {
                return true;
            }

            return false;
        };
    }

    private void configurarBotoes() {
        botaoFechar.setOnAction(e -> fechar());
        
        botaoCopiarCpf.setOnAction(e -> copiarCpfSelecionado());
        botaoCopiarNome.setOnAction(e -> copiarNomeSelecionado());
    }

    @SuppressWarnings("unchecked")
    private void configurarCopiarAoClicar() {
        // Adiciona listener para duplo clique na tabela
        // Duplo clique na célula de CPF copia o CPF
        // Duplo clique na célula de Nome copia o nome
        tabelaApenados.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                Usuario usuarioSelecionado = tabelaApenados.getSelectionModel().getSelectedItem();
                if (usuarioSelecionado != null) {
                    // Detecta qual coluna foi clicada usando a posição do mouse
                    javafx.scene.control.TablePosition<?, ?> pos = tabelaApenados.getFocusModel().getFocusedCell();
                    if (pos != null) {
                        TableColumn<Usuario, ?> coluna = (TableColumn<Usuario, ?>) pos.getTableColumn();
                        if (coluna != null) {
                            if (coluna == colCpf) {
                                copiarParaClipboard(usuarioSelecionado.getCpf(), "CPF", true);
                            } else if (coluna == colNome) {
                                copiarParaClipboard(usuarioSelecionado.getNome(), "Nome", false);
                            } else {
                                // Se clicou em outra coluna, copia o CPF por padrão
                                copiarParaClipboard(usuarioSelecionado.getCpf(), "CPF", true);
                            }
                        } else {
                            // Se não conseguiu detectar a coluna, copia o CPF por padrão
                            copiarParaClipboard(usuarioSelecionado.getCpf(), "CPF", true);
                        }
                    } else {
                        // Se não há posição, copia o CPF por padrão
                        copiarParaClipboard(usuarioSelecionado.getCpf(), "CPF", true);
                    }
                }
            }
        });
    }

    private void copiarCpfSelecionado() {
        Usuario usuario = tabelaApenados.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            mostrarAlerta("Aviso", "Por favor, selecione um apenado para copiar o CPF.");
            return;
        }
        copiarParaClipboard(usuario.getCpf(), "CPF", true);
    }

    private void copiarNomeSelecionado() {
        Usuario usuario = tabelaApenados.getSelectionModel().getSelectedItem();
        if (usuario == null) {
            mostrarAlerta("Aviso", "Por favor, selecione um apenado para copiar o nome.");
            return;
        }
        copiarParaClipboard(usuario.getNome(), "Nome", false);
    }

    private void copiarParaClipboard(String texto, String tipo, boolean removerFormatacao) {
        if (texto == null || texto.trim().isEmpty()) {
            mostrarAlerta("Aviso", "Não há " + tipo.toLowerCase() + " para copiar.");
            return;
        }

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        
        String textoParaCopiar = texto;
        // Remove formatação do CPF se necessário (pontos e hífens)
        if (removerFormatacao) {
            textoParaCopiar = texto.replaceAll("[^0-9]", "");
        }
        
        content.putString(textoParaCopiar);
        clipboard.setContent(content);
        
        // Mostra feedback visual simples (sem alerta para não interromper o fluxo)
        String textoExibicao = removerFormatacao ? textoParaCopiar : texto;
        labelTotal.setText(tipo + " copiado: " + textoExibicao + " | Total: " + listaFiltrada.size() + " apenado(s)");
        
        // Restaura o label após 2 segundos usando PauseTransition
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> atualizarLabelTotal());
        pause.play();
    }

    private void atualizarLabelTotal() {
        int total = listaFiltrada.size();
        labelTotal.setText("Total: " + total + " apenado(s)");
    }

    private void fechar() {
        Stage stage = (Stage) botaoFechar.getScene().getWindow();
        stage.close();
    }

    private void editarApenado(Usuario usuario) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/cpma/telaCadastroUsuario.fxml"));
            Parent root = loader.load();
            CadastrarUsuarioController controller = loader.getController();
            controller.carregarUsuarioParaEdicao(usuario);
            
            Stage stage = new Stage();
            stage.setTitle("Editar Apenado");
            stage.setScene(new Scene(root));
            stage.show();
            
            // Atualizar lista após edição
            stage.setOnCloseRequest(e -> carregarApenados());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao abrir tela de edição: " + e.getMessage());
        }
    }

    private void excluirApenado(Usuario usuario) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Exclusão");
        confirm.setHeaderText("Excluir Apenado");
        confirm.setContentText("Deseja realmente excluir o apenado " + usuario.getNome() + "?");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (UsuarioDAO.deletar(usuario.getIdUsuario())) {
                mostrarAlerta("Sucesso", "Apenado excluído com sucesso!");
                carregarApenados();
            } else {
                mostrarAlerta("Erro", "Erro ao excluir apenado.");
            }
        }
    }

    private void mostrarAlerta(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, mensagem, ButtonType.OK);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}

