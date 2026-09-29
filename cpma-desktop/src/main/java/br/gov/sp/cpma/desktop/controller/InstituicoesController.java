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

import java.util.ArrayList;
import java.util.List;

public class InstituicoesController {

    @FXML
    private ScrollPane scrollPaneInstituicoes;

    @FXML
    private VBox paneFormulario;

    @FXML
    private Label lblTituloForm;

    @FXML
    private Label lblSubtituloForm;

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
    private Label lblTipoInstituicaoError;

    @FXML
    private ComboBox<String> cbDispDia;

    @FXML
    private TextField txtDispHoraInicio1;

    @FXML
    private TextField txtDispHoraFim1;

    @FXML
    private TextField txtDispHoraInicio2;

    @FXML
    private TextField txtDispHoraFim2;

    @FXML
    private TableView<DisponibilidadeItemDTO> tblDisponibilidades;

    @FXML
    private TableColumn<DisponibilidadeItemDTO, String> colDispDia;

    @FXML
    private TableColumn<DisponibilidadeItemDTO, String> colDispInicio1;

    @FXML
    private TableColumn<DisponibilidadeItemDTO, String> colDispFim1;

    @FXML
    private TableColumn<DisponibilidadeItemDTO, String> colDispInicio2;

    @FXML
    private TableColumn<DisponibilidadeItemDTO, String> colDispFim2;

    @FXML
    private TableColumn<DisponibilidadeItemDTO, Void> colDispAcao;

    @FXML
    private Button btnSalvarInstituicao;

    @FXML
    private Button btnEditarInstituicao;

    @FXML
    private Button btnAlternarForm;

    @FXML
    private TableView<InstituicaoDTO> tblInstituicoes;

    @FXML
    private TableColumn<InstituicaoDTO, Number> colId;

    @FXML
    private TableColumn<InstituicaoDTO, String> colNome;

    @FXML
    private TableColumn<InstituicaoDTO, String> colTipo;

    @FXML
    private TableColumn<InstituicaoDTO, String> colResponsavel;

    @FXML
    private TableColumn<InstituicaoDTO, String> colTelefone;

    @FXML
    private TableColumn<InstituicaoDTO, String> colDisponibilidade;

    @FXML
    private TableColumn<InstituicaoDTO, String> colEndereco;

    @FXML
    private TableColumn<InstituicaoDTO, String> colBairro;

    @FXML
    private TableColumn<InstituicaoDTO, String> colCidade;

    private final CpmaApiClient apiClient = new CpmaApiClient();
    private final ObservableList<InstituicaoDTO> listaInstituicoes = FXCollections.observableArrayList();
    private final ObservableList<DisponibilidadeItemDTO> listaDisponibilidades = FXCollections.observableArrayList();

    private Long instituicaoIdEmEdicao = null;

    @FXML
    public void initialize() {
        configurarColunas();
        configurarTabelaDisponibilidades();
        tblInstituicoes.setItems(listaInstituicoes);
        tblDisponibilidades.setItems(listaDisponibilidades);

        cbDispDia.setItems(FXCollections.observableArrayList(
                "Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira", "Sábado", "Domingo"
        ));
        cbDispDia.getSelectionModel().selectFirst();
        txtDispHoraInicio1.setText("08:00");
        txtDispHoraFim1.setText("12:00");
        txtDispHoraInicio2.setText("13:00");
        txtDispHoraFim2.setText("17:00");

        configurarComboBoxTipo();
        carregarTipos();
        carregarInstituicoes();

        FormValidator.vincularLimpezaAoInteragir(txtNome, lblNomeError);
        FormValidator.vincularLimpezaAoInteragir(txtResponsavel, lblResponsavelError);
        FormValidator.vincularLimpezaAoInteragir(cbTipoInstituicao, lblTipoInstituicaoError);

        tblInstituicoes.setRowFactory(tv -> {
            TableRow<InstituicaoDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    carregarInstituicaoParaEdicao(row.getItem());
                }
            });
            return row;
        });
    }

    private void configurarColunas() {
        colId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getIdInstituicao()));
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoNome() != null ? c.getValue().getTipoNome() : "-"));
        colResponsavel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getResponsavel() != null ? c.getValue().getResponsavel() : "-"));
        colTelefone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefone() != null ? c.getValue().getTelefone() : "-"));
        colDisponibilidade.setCellValueFactory(c -> new SimpleStringProperty(formatarResumoDisponibilidade(c.getValue().getDisponibilidades())));
        colEndereco.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEndereco() != null ? c.getValue().getEndereco() : "-"));
        colBairro.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBairro() != null ? c.getValue().getBairro() : "-"));
        colCidade.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCidade() != null ? c.getValue().getCidade() : "-"));
    }

    private String formatarResumoDisponibilidade(List<DisponibilidadeItemDTO> disps) {
        if (disps == null || disps.isEmpty()) {
            return "Sem horários cadastrados";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < disps.size(); i++) {
            if (i > 0) sb.append(", ");
            DisponibilidadeItemDTO d = disps.get(i);
            String abrev = abreviarDia(d.getDiaSemana());
            sb.append(abrev);
            if (d.getHoraInicio1() != null && !d.getHoraInicio1().isBlank()) {
                sb.append(" (").append(d.getHoraInicio1()).append("-").append(d.getHoraFim1() != null ? d.getHoraFim1() : "").append(")");
            }
        }
        return sb.toString();
    }

    private String abreviarDia(String dia) {
        if (dia == null) return "";
        String lower = dia.toLowerCase();
        if (lower.contains("seg")) return "Seg";
        if (lower.contains("ter")) return "Ter";
        if (lower.contains("qua")) return "Qua";
        if (lower.contains("qui")) return "Qui";
        if (lower.contains("sex")) return "Sex";
        if (lower.contains("sáb") || lower.contains("sab")) return "Sáb";
        if (lower.contains("dom")) return "Dom";
        return dia;
    }

    private void configurarTabelaDisponibilidades() {
        colDispDia.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDiaSemana()));
        colDispInicio1.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHoraInicio1() != null ? c.getValue().getHoraInicio1() : "-"));
        colDispFim1.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHoraFim1() != null ? c.getValue().getHoraFim1() : "-"));
        colDispInicio2.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHoraInicio2() != null ? c.getValue().getHoraInicio2() : "-"));
        colDispFim2.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHoraFim2() != null ? c.getValue().getHoraFim2() : "-"));

        colDispAcao.setCellFactory(param -> new TableCell<>() {
            private final Button btnRemover = new Button("🗑️");

            {
                btnRemover.setStyle("-fx-background-color: transparent; -fx-text-fill: #dc2626; -fx-cursor: hand; -fx-font-size: 13px; -fx-padding: 2px 6px;");
                btnRemover.setOnAction(event -> {
                    DisponibilidadeItemDTO item = getTableView().getItems().get(getIndex());
                    listaDisponibilidades.remove(item);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnRemover);
                }
            }
        });
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
    public void handleRecarregarInstituicoes() {
        carregarInstituicoes();
    }

    @FXML
    public void handleAlternarFormulario() {
        if (!paneFormulario.isVisible()) {
            iniciarCadastroNovo();
        } else {
            handleCancelarFormulario();
        }
    }

    private void iniciarCadastroNovo() {
        this.instituicaoIdEmEdicao = null;
        lblTituloForm.setText("Cadastro de Nova Instituicao Parceira");
        lblSubtituloForm.setText("Preencha os dados da instituicao e defina o quadro de disponibilidade semanal.");
        btnSalvarInstituicao.setText("Salvar Instituicao");
        limparForm();
        paneFormulario.setVisible(true);
        paneFormulario.setManaged(true);
        if (scrollPaneInstituicoes != null) {
            scrollPaneInstituicoes.setVvalue(0.0);
        }
    }

    @FXML
    public void handleEditarInstituicaoSelecionada() {
        InstituicaoDTO selecionada = tblInstituicoes.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            carregarInstituicaoParaEdicao(selecionada);
        } else {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Selecao Necessaria");
            alert.setHeaderText("Nenhuma instituicao selecionada");
            alert.setContentText("Por favor, selecione uma instituicao na tabela abaixo para editar.");
            alert.showAndWait();
        }
    }

    public void carregarInstituicaoParaEdicao(InstituicaoDTO inst) {
        if (inst == null) return;
        this.instituicaoIdEmEdicao = inst.getIdInstituicao();
        lblTituloForm.setText("Editar Instituicao Parceira (#" + inst.getIdInstituicao() + " - " + inst.getNome() + ")");
        lblSubtituloForm.setText("Modifique os dados da instituicao, responsavel ou sua grade semanal de disponibilidade.");
        btnSalvarInstituicao.setText("Salvar Alteracoes");

        txtNome.setText(inst.getNome() != null ? inst.getNome() : "");
        txtResponsavel.setText(inst.getResponsavel() != null ? inst.getResponsavel() : "");
        txtTelefone.setText(inst.getTelefone() != null ? inst.getTelefone() : "");
        txtCep.setText(inst.getCep() != null ? inst.getCep() : "");
        txtEndereco.setText(inst.getEndereco() != null ? inst.getEndereco() : "");
        txtBairro.setText(inst.getBairro() != null ? inst.getBairro() : "");
        txtCidade.setText(inst.getCidade() != null ? inst.getCidade() : "");

        if (inst.getTipoId() != null && cbTipoInstituicao.getItems() != null) {
            for (TipoInstituicaoDTO tipo : cbTipoInstituicao.getItems()) {
                if (tipo.getIdTipo().equals(inst.getTipoId())) {
                    cbTipoInstituicao.setValue(tipo);
                    break;
                }
            }
        }

        listaDisponibilidades.clear();
        if (inst.getDisponibilidades() != null) {
            listaDisponibilidades.addAll(inst.getDisponibilidades());
        }

        lblFeedbackForm.setVisible(false);
        paneFormulario.setVisible(true);
        paneFormulario.setManaged(true);
        if (scrollPaneInstituicoes != null) {
            scrollPaneInstituicoes.setVvalue(0.0);
        }
    }

    @FXML
    public void handleCancelarFormulario() {
        this.instituicaoIdEmEdicao = null;
        paneFormulario.setVisible(false);
        paneFormulario.setManaged(false);
        lblFeedbackForm.setVisible(false);
        limparForm();
    }

    @FXML
    public void handleAdicionarDisponibilidade() {
        String dia = cbDispDia.getValue();
        String ini1 = txtDispHoraInicio1.getText() != null ? txtDispHoraInicio1.getText().trim() : "";
        String fim1 = txtDispHoraFim1.getText() != null ? txtDispHoraFim1.getText().trim() : "";
        String ini2 = txtDispHoraInicio2.getText() != null ? txtDispHoraInicio2.getText().trim() : "";
        String fim2 = txtDispHoraFim2.getText() != null ? txtDispHoraFim2.getText().trim() : "";

        if (dia == null || dia.isBlank() || ini1.isBlank() || fim1.isBlank()) {
            lblFeedbackForm.setText("Informe ao menos o dia e o horário do Turno 1 (início e fim).");
            lblFeedbackForm.getStyleClass().setAll("banner-error");
            lblFeedbackForm.setVisible(true);
            return;
        }

        listaDisponibilidades.removeIf(d -> d.getDiaSemana() != null && d.getDiaSemana().equalsIgnoreCase(dia));
        listaDisponibilidades.add(new DisponibilidadeItemDTO(dia, ini1, fim1, ini2, fim2));
        lblFeedbackForm.setVisible(false);
    }

    @FXML
    public void handlePreencherPadraoComercial() {
        listaDisponibilidades.clear();
        String[] dias = {"Segunda-feira", "Terça-feira", "Quarta-feira", "Quinta-feira", "Sexta-feira"};
        for (String dia : dias) {
            listaDisponibilidades.add(new DisponibilidadeItemDTO(dia, "08:00", "12:00", "13:00", "17:00"));
        }
    }

    @FXML
    public void handleSalvarInstituicao() {
        boolean nomeVal = FormValidator.validarCampoObrigatorio(txtNome, lblNomeError, "Nome da instituicao e obrigatorio");
        boolean respVal = FormValidator.validarCampoObrigatorio(txtResponsavel, lblResponsavelError, "Responsavel e obrigatorio");
        boolean tipoVal = FormValidator.validarCampoObrigatorio(cbTipoInstituicao, lblTipoInstituicaoError, "Selecione o tipo de instituicao");

        if (!nomeVal || !respVal || !tipoVal) {
            lblFeedbackForm.setText("Preencha todos os campos obrigatorios destacados em vermelho.");
            lblFeedbackForm.getStyleClass().setAll("banner-error");
            lblFeedbackForm.setVisible(true);
            return;
        }

        btnSalvarInstituicao.setDisable(true);
        lblFeedbackForm.setVisible(false);

        CadastroInstituicaoDTO dto = new CadastroInstituicaoDTO();
        dto.setNome(txtNome.getText().trim());
        dto.setResponsavel(txtResponsavel.getText().trim());
        dto.setTelefone(txtTelefone.getText() != null ? txtTelefone.getText().trim() : null);
        dto.setCep(txtCep.getText() != null ? txtCep.getText().trim() : null);
        dto.setEndereco(txtEndereco.getText() != null ? txtEndereco.getText().trim() : null);
        dto.setBairro(txtBairro.getText() != null ? txtBairro.getText().trim() : null);
        dto.setCidade(txtCidade.getText() != null ? txtCidade.getText().trim() : null);

        if (cbTipoInstituicao.getValue() != null) {
            dto.setTipoId(cbTipoInstituicao.getValue().getIdTipo());
        }

        dto.setDisponibilidades(new ArrayList<>(listaDisponibilidades));

        final boolean isEdicao = (this.instituicaoIdEmEdicao != null);
        final Long idSalvar = this.instituicaoIdEmEdicao;

        new Thread(() -> {
            ApiResponse<InstituicaoDTO> resp;
            if (isEdicao) {
                resp = apiClient.atualizarInstituicao(idSalvar, dto);
            } else {
                resp = apiClient.cadastrarInstituicao(dto);
            }

            Platform.runLater(() -> {
                btnSalvarInstituicao.setDisable(false);
                if (resp.isSuccess() && resp.getData() != null) {
                    lblFeedbackForm.setText(isEdicao
                            ? "Instituicao atualizada com sucesso!"
                            : "Instituicao cadastrada com sucesso! ID: " + resp.getData().getIdInstituicao());
                    lblFeedbackForm.getStyleClass().setAll("banner-success");
                    lblFeedbackForm.setVisible(true);
                    carregarInstituicoes();
                    handleCancelarFormulario();
                } else {
                    String mensagemErro = FormValidator.interpretarMensagemErro(resp.getError(), resp.getStatusCode());
                    lblFeedbackForm.setText(mensagemErro);
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
        cbTipoInstituicao.setValue(null);
        listaDisponibilidades.clear();
        FormValidator.limparErro(txtNome, lblNomeError);
        FormValidator.limparErro(txtResponsavel, lblResponsavelError);
        FormValidator.limparErro(cbTipoInstituicao, lblTipoInstituicaoError);
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
