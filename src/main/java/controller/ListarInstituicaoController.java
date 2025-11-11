package controller;

import java.util.List;
import java.util.function.Predicate;

import dao.InstituicaoDAO;
import dao.TipoInstituicaoDAO;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Instituicao;
import model.TipoInstituicao;

public class ListarInstituicaoController {

    @FXML
    private TableView<Instituicao> tabelaInstituicoes;
    @FXML
    private TableColumn<Instituicao, String> colNome;
    @FXML
    private TableColumn<Instituicao, String> colCidade;
    @FXML
    private TableColumn<Instituicao, String> colUf;
    @FXML
    private TableColumn<Instituicao, String> colResponsavel;
    @FXML
    private TableColumn<Instituicao, String> colTelefone;
    @FXML
    private TableColumn<Instituicao, String> colTipo;
    @FXML
    private TableColumn<Instituicao, String> colAcoes;
    @FXML
    private TextField campoFiltro;
    @FXML
    private Label labelTotal;
    @FXML
    private Button botaoCopiarTelefone;
    @FXML
    private Button botaoCopiarNome;
    @FXML
    private Button botaoFechar;

    private ObservableList<Instituicao> listaCompleta;
    private FilteredList<Instituicao> listaFiltrada;

    @FXML
    private void initialize() {
        configurarTabela();
        carregarInstituicoes();
        configurarFiltro();
        configurarBotoes();
        configurarCopiarAoClicar();
    }

    private void configurarTabela() {
        // Configura as colunas da tabela
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colCidade.setCellValueFactory(new PropertyValueFactory<>("cidade"));
        colUf.setCellValueFactory(new PropertyValueFactory<>("uf"));
        colResponsavel.setCellValueFactory(new PropertyValueFactory<>("responsavel"));
        
        // Formata telefone
        colTelefone.setCellValueFactory(cellData -> {
            Instituicao instituicao = cellData.getValue();
            if (instituicao != null && instituicao.getTelefone() != null) {
                String telefone = instituicao.getTelefone();
                // Formata telefone se não estiver formatado
                telefone = formatarTelefone(telefone);
                return new javafx.beans.property.SimpleStringProperty(telefone);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        // Mostra o nome do tipo de instituição
        colTipo.setCellValueFactory(cellData -> {
            Instituicao instituicao = cellData.getValue();
            if (instituicao != null) {
                TipoInstituicao tipo = TipoInstituicaoDAO.buscarPorId(instituicao.getTipo());
                String nomeTipo = tipo != null ? tipo.getTipo() : "Tipo " + instituicao.getTipo();
                return new javafx.beans.property.SimpleStringProperty(nomeTipo);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        // Configura coluna de ações com botões Editar e Excluir
        colAcoes.setCellFactory(column -> new TableCell<Instituicao, String>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnExcluir = new Button("Excluir");

            {
                btnEditar.setOnAction(e -> {
                    Instituicao instituicao = getTableView().getItems().get(getIndex());
                    editarInstituicao(instituicao);
                });
                btnExcluir.setOnAction(e -> {
                    Instituicao instituicao = getTableView().getItems().get(getIndex());
                    excluirInstituicao(instituicao);
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

    private String formatarTelefone(String telefone) {
        if (telefone == null || telefone.isEmpty()) {
            return "";
        }
        
        // Remove caracteres não numéricos
        String numeros = telefone.replaceAll("[^0-9]", "");
        
        // Formata conforme o tamanho
        if (numeros.length() == 10) {
            // Telefone fixo: (00) 0000-0000
            return "(" + numeros.substring(0, 2) + ") " + numeros.substring(2, 6) + "-" + numeros.substring(6);
        } else if (numeros.length() == 11) {
            // Celular: (00) 00000-0000
            return "(" + numeros.substring(0, 2) + ") " + numeros.substring(2, 7) + "-" + numeros.substring(7);
        } else if (telefone.contains("(") || telefone.contains("-")) {
            // Já está formatado
            return telefone;
        }
        
        return telefone;
    }

    private void carregarInstituicoes() {
        List<Instituicao> instituicoes = InstituicaoDAO.buscarTodasInstituicoes();
        listaCompleta = FXCollections.observableArrayList(instituicoes);
        listaFiltrada = new FilteredList<>(listaCompleta, p -> true);
        
        // Ordena a lista por nome
        SortedList<Instituicao> listaOrdenada = new SortedList<>(listaFiltrada);
        listaOrdenada.comparatorProperty().bind(tabelaInstituicoes.comparatorProperty());
        
        tabelaInstituicoes.setItems(listaOrdenada);
        atualizarLabelTotal();
    }

    private void configurarFiltro() {
        campoFiltro.textProperty().addListener((observable, oldValue, newValue) -> {
            listaFiltrada.setPredicate(criarPredicadoFiltro(newValue));
            atualizarLabelTotal();
        });
    }

    private Predicate<Instituicao> criarPredicadoFiltro(String textoFiltro) {
        return instituicao -> {
            if (textoFiltro == null || textoFiltro.isEmpty()) {
                return true;
            }

            String textoLowerCase = textoFiltro.toLowerCase();
            String nome = instituicao.getNome() != null ? instituicao.getNome().toLowerCase() : "";
            String cidade = instituicao.getCidade() != null ? instituicao.getCidade().toLowerCase() : "";
            String telefone = instituicao.getTelefone() != null ? instituicao.getTelefone().replaceAll("[^0-9]", "") : "";
            String textoFiltroNumerico = textoFiltro.replaceAll("[^0-9]", "");

            // Busca por nome
            if (nome.contains(textoLowerCase)) {
                return true;
            }

            // Busca por cidade
            if (cidade.contains(textoLowerCase)) {
                return true;
            }

            // Busca por telefone (remove formatação para comparar)
            if (!textoFiltroNumerico.isEmpty() && telefone.contains(textoFiltroNumerico)) {
                return true;
            }

            return false;
        };
    }

    private void configurarBotoes() {
        botaoFechar.setOnAction(e -> fechar());
        
        botaoCopiarTelefone.setOnAction(e -> copiarTelefoneSelecionado());
        botaoCopiarNome.setOnAction(e -> copiarNomeSelecionado());
    }

    @SuppressWarnings("unchecked")
    private void configurarCopiarAoClicar() {
        // Adiciona listener para duplo clique na tabela
        // Duplo clique na célula de Telefone copia o telefone
        // Duplo clique na célula de Nome copia o nome
        tabelaInstituicoes.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                Instituicao instituicaoSelecionada = tabelaInstituicoes.getSelectionModel().getSelectedItem();
                if (instituicaoSelecionada != null) {
                    // Detecta qual coluna foi clicada usando a posição do mouse
                    javafx.scene.control.TablePosition<?, ?> pos = tabelaInstituicoes.getFocusModel().getFocusedCell();
                    if (pos != null) {
                        TableColumn<Instituicao, ?> coluna = (TableColumn<Instituicao, ?>) pos.getTableColumn();
                        if (coluna != null) {
                            if (coluna == colTelefone) {
                                copiarParaClipboard(instituicaoSelecionada.getTelefone(), "Telefone", true);
                            } else if (coluna == colNome) {
                                copiarParaClipboard(instituicaoSelecionada.getNome(), "Nome", false);
                            } else {
                                // Se clicou em outra coluna, copia o nome por padrão
                                copiarParaClipboard(instituicaoSelecionada.getNome(), "Nome", false);
                            }
                        } else {
                            // Se não conseguiu detectar a coluna, copia o nome por padrão
                            copiarParaClipboard(instituicaoSelecionada.getNome(), "Nome", false);
                        }
                    } else {
                        // Se não há posição, copia o nome por padrão
                        copiarParaClipboard(instituicaoSelecionada.getNome(), "Nome", false);
                    }
                }
            }
        });
    }

    private void copiarTelefoneSelecionado() {
        Instituicao instituicao = tabelaInstituicoes.getSelectionModel().getSelectedItem();
        if (instituicao == null) {
            mostrarAlerta("Aviso", "Por favor, selecione uma instituição para copiar o telefone.");
            return;
        }
        copiarParaClipboard(instituicao.getTelefone(), "Telefone", true);
    }

    private void copiarNomeSelecionado() {
        Instituicao instituicao = tabelaInstituicoes.getSelectionModel().getSelectedItem();
        if (instituicao == null) {
            mostrarAlerta("Aviso", "Por favor, selecione uma instituição para copiar o nome.");
            return;
        }
        copiarParaClipboard(instituicao.getNome(), "Nome", false);
    }

    private void copiarParaClipboard(String texto, String tipo, boolean removerFormatacao) {
        if (texto == null || texto.trim().isEmpty()) {
            mostrarAlerta("Aviso", "Não há " + tipo.toLowerCase() + " para copiar.");
            return;
        }

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        
        String textoParaCopiar = texto;
        // Remove formatação do telefone se necessário (parênteses, espaços, hífens)
        if (removerFormatacao) {
            textoParaCopiar = texto.replaceAll("[^0-9]", "");
        }
        
        content.putString(textoParaCopiar);
        clipboard.setContent(content);
        
        // Mostra feedback visual simples (sem alerta para não interromper o fluxo)
        String textoExibicao = removerFormatacao ? textoParaCopiar : texto;
        labelTotal.setText(tipo + " copiado: " + textoExibicao + " | Total: " + listaFiltrada.size() + " instituição(ões)");
        
        // Restaura o label após 2 segundos usando PauseTransition
        PauseTransition pause = new PauseTransition(Duration.seconds(2));
        pause.setOnFinished(e -> atualizarLabelTotal());
        pause.play();
    }

    private void atualizarLabelTotal() {
        int total = listaFiltrada.size();
        labelTotal.setText("Total: " + total + " instituição(ões)");
    }

    private void fechar() {
        Stage stage = (Stage) botaoFechar.getScene().getWindow();
        stage.close();
    }

    private void editarInstituicao(Instituicao instituicao) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/cpma/cadastroInstituicaoView.fxml"));
            Parent root = loader.load();
            CadastrarInstituicaoController controller = loader.getController();
            controller.carregarInstituicaoParaEdicao(instituicao);
            
            Stage stage = new Stage();
            stage.setTitle("Editar Instituição");
            stage.setScene(new Scene(root));
            stage.show();
            
            // Atualizar lista após edição
            stage.setOnCloseRequest(e -> carregarInstituicoes());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Erro", "Erro ao abrir tela de edição: " + e.getMessage());
        }
    }

    private void excluirInstituicao(Instituicao instituicao) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Confirmar Exclusão");
        confirm.setHeaderText("Excluir Instituição");
        confirm.setContentText("Deseja realmente excluir a instituição " + instituicao.getNome() + "?\n\nAtenção: Esta ação não pode ser desfeita.");
        
        if (confirm.showAndWait().get() == ButtonType.OK) {
            if (InstituicaoDAO.deletar(instituicao.getIdInstituicao())) {
                mostrarAlerta("Sucesso", "Instituição excluída com sucesso!");
                carregarInstituicoes();
            } else {
                mostrarAlerta("Erro", "Erro ao excluir instituição. Verifique se não há penas ou registros associados.");
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

