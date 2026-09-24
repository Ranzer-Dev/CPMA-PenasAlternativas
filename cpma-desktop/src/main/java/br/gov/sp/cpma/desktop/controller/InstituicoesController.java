package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.*;
import br.gov.sp.cpma.desktop.util.FormValidator;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;

public class InstituicoesController {

    @FXML
    private VBox paneFormulario;

    @FXML
    private Label lblFeedbackForm;

    @FXML
    private TextField txtNome;

    @FXML
    private Label lblNomeError;

    @FXML
    private TextField txtResponsavel;

    @FXML
    private Label lblResponsavelError;

    @FXML
    private TextField txtTelefone;

    @FXML
    private TextField txtCep;

    @FXML
    private TextField txtEndereco;

    @FXML
    private TextField txtBairro;

    @FXML
    private TextField txtCidade;

    @FXML
    private ComboBox<TipoInstituicaoDTO> cbTipoInstituicao;

    @FXML
    private Button btnSalvarInstituicao;

    @FXML
    private TableView<InstituicaoDTO> tblInstituicoes;

    @FXML
    private TableColumn<InstituicaoDTO, Number> colId;

    @FXML
    private TableColumn<InstituicaoDTO, String> colNome;

    @FXML
    private TableColumn<InstituicaoDTO, String> colResponsavel;

    @FXML
    private TableColumn<InstituicaoDTO, String> colTelefone;

    @FXML
    private TableColumn<InstituicaoDTO, String> colEndereco;

    @FXML
    private TableColumn<InstituicaoDTO, String> colBairro;

    @FXML
    private TableColumn<InstituicaoDTO, String> colCidade;

    private final CpmaApiClient apiClient = new CpmaApiClient();
    private final ObservableList<InstituicaoDTO> listaInstituicoes = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarColunas();
        tblInstituicoes.setItems(listaInstituicoes);

        configurarComboBoxTipo();
        carregarTipos();
        carregarInstituicoes();

        FormValidator.limparAoDigitar(txtNome, lblNomeError);
        FormValidator.limparAoDigitar(txtResponsavel, lblResponsavelError);
    }

    private void configurarColunas() {
        colId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getIdInstituicao()));
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colResponsavel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getResponsavel() != null ? c.getValue().getResponsavel() : "-"));
        colTelefone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefone() != null ? c.getValue().getTelefone() : "-"));
        colEndereco.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEndereco() != null ? c.getValue().getEndereco() : "-"));
        colBairro.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBairro() != null ? c.getValue().getBairro() : "-"));
        colCidade.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCidade() != null ? c.getValue().getCidade() : "-"));
    }

    private void configurarComboBoxTipo() {
        cbTipoInstituicao.setConverter(new StringConverter<TipoInstituicaoDTO>() {
            @Override
            public String toString(TipoInstituicaoDTO t) {
                return t != null ? t.getTipo() : "";
            }

            @Override
            public TipoInstituicaoDTO fromString(String string) {
                return null;
            }
        });
    }

    private void carregarTipos() {
        new Thread(() -> {
            ApiResponse<List<TipoInstituicaoDTO>> resp = apiClient.listarTiposInstituicao();
            Platform.runLater(() -> {
                if (resp.isSuccess() && resp.getData() != null) {
                    cbTipoInstituicao.setItems(FXCollections.observableArrayList(resp.getData()));
                }
            });
        }).start();
    }

    private void carregarInstituicoes() {
        new Thread(() -> {
            ApiResponse<List<InstituicaoDTO>> resp = apiClient.listarInstituicoes();
            Platform.runLater(() -> {
                if (resp.isSuccess() && resp.getData() != null) {
                    listaInstituicoes.setAll(resp.getData());
                }
            });
        }).start();
    }

    @FXML
    public void handleAlternarFormulario() {
        boolean visivel = !paneFormulario.isVisible();
        paneFormulario.setVisible(visivel);
        paneFormulario.setManaged(visivel);
    }

    @FXML
    public void handleCancelarFormulario() {
        paneFormulario.setVisible(false);
        paneFormulario.setManaged(false);
        lblFeedbackForm.setVisible(false);
    }

    @FXML
    public void handleSalvarInstituicao() {
        boolean nomeVal = FormValidator.validarCampoObrigatorio(txtNome, lblNomeError, "Nome e obrigatorio");
        boolean respVal = FormValidator.validarCampoObrigatorio(txtResponsavel, lblResponsavelError, "Responsavel e obrigatorio");

        if (!nomeVal || !respVal) {
            return;
        }

        btnSalvarInstituicao.setDisable(true);

        CadastroInstituicaoDTO dto = new CadastroInstituicaoDTO();
        dto.setNome(txtNome.getText().trim());
        dto.setResponsavel(txtResponsavel.getText().trim());
        dto.setTelefone(txtTelefone.getText().trim());
        dto.setCep(txtCep.getText().trim());
        dto.setEndereco(txtEndereco.getText().trim());
        dto.setBairro(txtBairro.getText().trim());
        dto.setCidade(txtCidade.getText().trim());

        if (cbTipoInstituicao.getValue() != null) {
            dto.setTipoId(cbTipoInstituicao.getValue().getIdTipo());
        }

        new Thread(() -> {
            ApiResponse<InstituicaoDTO> resp = apiClient.cadastrarInstituicao(dto);
            Platform.runLater(() -> {
                btnSalvarInstituicao.setDisable(false);
                if (resp.isSuccess()) {
                    lblFeedbackForm.setText("Instituicao cadastrada com sucesso!");
                    lblFeedbackForm.getStyleClass().setAll("banner-success");
                    lblFeedbackForm.setVisible(true);
                    carregarInstituicoes();
                    limparForm();
                } else {
                    lblFeedbackForm.setText("Erro ao salvar: " + (resp.getError() != null ? resp.getError().getMessage() : ""));
                    lblFeedbackForm.getStyleClass().setAll("banner-error");
                    lblFeedbackForm.setVisible(true);
                }
            });
        }).start();
    }

    private void limparForm() {
        txtNome.clear();
        txtResponsavel.clear();
        txtTelefone.clear();
        txtCep.clear();
        txtEndereco.clear();
        txtBairro.clear();
        txtCidade.clear();
    }

    @FXML
    public void handleNovoTipoInstituicao() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Novo Tipo de Instituicao");
        dialog.setHeaderText("Cadastrar Tipo de Entidade / Instituicao");
        dialog.setContentText("Nome do tipo (ex: ONG, Escola Municipal, Hospital):");

        dialog.showAndWait().ifPresent(tipoNome -> {
            String tipoLimpo = tipoNome.trim();
            if (!tipoLimpo.isEmpty()) {
                new Thread(() -> {
                    ApiResponse<TipoInstituicaoDTO> resp = apiClient.cadastrarTipoInstituicao(tipoLimpo);
                    Platform.runLater(() -> {
                        if (resp.isSuccess() && resp.getData() != null) {
                            carregarTipos();
                            cbTipoInstituicao.setValue(resp.getData());
                        } else {
                            Alert alert = new Alert(Alert.AlertType.ERROR);
                            alert.setTitle("Erro");
                            alert.setHeaderText("Nao foi possivel cadastrar o tipo");
                            alert.setContentText(resp.getError() != null ? resp.getError().getMessage() : "Erro desconhecido");
                            alert.showAndWait();
                        }
                    });
                }).start();
            }
        });
    }

}
