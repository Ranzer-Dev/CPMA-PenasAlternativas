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
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PenasController {

    @FXML
    private Label lblTituloForm;

    @FXML
    private Label lblSubtituloForm;

    @FXML
    private Button btnCancelarEdicao;

    @FXML
    private Label lblFeedback;

    @FXML
    private TextField txtCpfApenado;

    @FXML
    private Button btnBuscarApenado;

    @FXML
    private Label lblCpfError;

    @FXML
    private HBox boxApenadoEncontrado;

    @FXML
    private Label lblNomeApenado;

    @FXML
    private ComboBox<InstituicaoDTO> cbInstituicao;

    @FXML
    private Label lblInstituicaoError;

    @FXML
    private ComboBox<String> cbTipoPena;

    @FXML
    private Label lblTipoPenaError;

    @FXML
    private TextField txtHorasTotais;

    @FXML
    private Label lblHorasTotaisError;

    @FXML
    private TextField txtHorasSemanais;

    @FXML
    private Label lblHorasSemanaisError;

    @FXML
    private DatePicker dpDataInicio;

    @FXML
    private Label lblDataInicioError;

    @FXML
    private DatePicker dpDataTermino;

    @FXML
    private Label lblEstimativaTexto;

    @FXML
    private CheckBox chkSeg;

    @FXML
    private CheckBox chkTer;

    @FXML
    private CheckBox chkQua;

    @FXML
    private CheckBox chkQui;

    @FXML
    private CheckBox chkSex;

    @FXML
    private CheckBox chkSab;

    @FXML
    private CheckBox chkDom;

    @FXML
    private TextField txtHorasDiarias;

    @FXML
    private TextField txtTurnoAcordado;

    @FXML
    private TextArea txtAtividades;

    @FXML
    private Button btnSalvarPena;

    @FXML
    private Button btnEditarPenaTabela;

    @FXML
    private TableView<PenaDTO> tblPenasGeral;

    @FXML
    private TableColumn<PenaDTO, Number> colPenaGeralId;

    @FXML
    private TableColumn<PenaDTO, String> colPenaGeralCodigo;

    @FXML
    private TableColumn<PenaDTO, String> colPenaGeralApenado;

    @FXML
    private TableColumn<PenaDTO, String> colPenaGeralTipo;

    @FXML
    private TableColumn<PenaDTO, String> colPenaGeralInstituicao;

    @FXML
    private TableColumn<PenaDTO, String> colPenaGeralHoras;

    @FXML
    private TableColumn<PenaDTO, String> colPenaGeralSemanais;

    @FXML
    private TableColumn<PenaDTO, String> colPenaGeralDias;

    @FXML
    private TableColumn<PenaDTO, String> colPenaGeralInicio;

    private final CpmaApiClient apiClient = new CpmaApiClient();
    private final ObservableList<PenaDTO> masterPenas = FXCollections.observableArrayList();
    private UsuarioDTO apenadoSelecionado;
    private Long penaIdEmEdicao = null;

    @FXML
    public void initialize() {
        cbTipoPena.setItems(FXCollections.observableArrayList(
                "Prestacao de Servicos a Comunidade (PSC)",
                "Prestacao Pecuniaria",
                "Limitacao de Fim de Semana"
        ));
        cbTipoPena.getSelectionModel().selectFirst();
        cbTipoPena.setEditable(true);
        dpDataInicio.setValue(LocalDate.now());

        configurarComboBoxInstituicao();
        configurarTabelaPenas();
        carregarInstituicoes();
        carregarPenas();

        FormValidator.vincularLimpezaAoInteragir(txtCpfApenado, lblCpfError);
        FormValidator.vincularLimpezaAoInteragir(cbInstituicao, lblInstituicaoError);
        FormValidator.vincularLimpezaAoInteragir(cbTipoPena, lblTipoPenaError);
        FormValidator.vincularLimpezaAoInteragir(txtHorasTotais, lblHorasTotaisError);
        FormValidator.vincularLimpezaAoInteragir(txtHorasSemanais, lblHorasSemanaisError);
        FormValidator.vincularLimpezaAoInteragir(dpDataInicio, lblDataInicioError);

        tblPenasGeral.setRowFactory(tv -> {
            TableRow<PenaDTO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    carregarPenaParaEdicao(row.getItem());
                }
            });
            return row;
        });

        if (MainShellController.getInstance() != null) {
            PenaDTO penaPendente = MainShellController.getInstance().consumirPenaParaEdicao();
            if (penaPendente != null) {
                carregarPenaParaEdicao(penaPendente);
            }
        }
    }

    private void configurarComboBoxInstituicao() {
        cbInstituicao.setConverter(new StringConverter<InstituicaoDTO>() {
            @Override
            public String toString(InstituicaoDTO obj) {
                return obj == null ? "" : obj.getNome();
            }

            @Override
            public InstituicaoDTO fromString(String string) {
                return null;
            }
        });
    }

    private void configurarTabelaPenas() {
        colPenaGeralId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getIdPena()));
        colPenaGeralCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigoAtualUsuario() != null ? c.getValue().getCodigoAtualUsuario() : "-"));
        colPenaGeralApenado.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getUsuarioNome() != null ? c.getValue().getUsuarioNome() : "-"));
        colPenaGeralTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoPena()));
        colPenaGeralInstituicao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInstituicaoPrincipalNome() != null ? c.getValue().getInstituicaoPrincipalNome() : "-"));
        colPenaGeralHoras.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHorasTotais() + "h"));
        colPenaGeralSemanais.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHorasSemanais() + "h/sem"));
        colPenaGeralDias.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDiasSemanaEHorariosDisponivel() != null ? c.getValue().getDiasSemanaEHorariosDisponivel() : "-"));
        colPenaGeralInicio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataInicio() != null ? c.getValue().getDataInicio().toString() : "-"));

        tblPenasGeral.setItems(masterPenas);
    }

    private void carregarInstituicoes() {
        new Thread(() -> {
            ApiResponse<List<InstituicaoDTO>> resp = apiClient.listarInstituicoes();
            Platform.runLater(() -> {
                if (resp.isSuccess() && resp.getData() != null) {
                    cbInstituicao.setItems(FXCollections.observableArrayList(resp.getData()));
                }
            });
        }).start();
    }

    public void carregarPenas() {
        new Thread(() -> {
            ApiResponse<List<PenaDTO>> resp = apiClient.listarTodasPenas();
            Platform.runLater(() -> {
                if (resp.isSuccess() && resp.getData() != null) {
                    masterPenas.setAll(resp.getData());
                }
            });
        }).start();
    }

    @FXML
    public void handleRecarregarPenas() {
        carregarPenas();
    }

    @FXML
    public void handleEditarPenaSelecionada() {
        PenaDTO selecionada = tblPenasGeral.getSelectionModel().getSelectedItem();
        if (selecionada != null) {
            carregarPenaParaEdicao(selecionada);
        } else {
            lblFeedback.setText("Selecione uma pena na tabela abaixo para editar.");
            lblFeedback.getStyleClass().setAll("banner-error");
            lblFeedback.setVisible(true);
        }
    }

    public void carregarPenaParaEdicao(PenaDTO pena) {
        if (pena == null) return;

        this.penaIdEmEdicao = pena.getIdPena();
        lblTituloForm.setText("Editar Contrato / Medida Alternativa (#" + pena.getIdPena() + ")");
        lblSubtituloForm.setText("Ajuste os termos, dias de comparecimento e carga horária acordada com o apenado.");
        btnCancelarEdicao.setVisible(true);
        btnCancelarEdicao.setManaged(true);
        btnSalvarPena.setText("Salvar Alterações do Contrato");

        txtCpfApenado.setText("");
        txtCpfApenado.setDisable(true);
        btnBuscarApenado.setDisable(true);

        boxApenadoEncontrado.setVisible(true);
        boxApenadoEncontrado.setManaged(true);
        lblNomeApenado.setText(pena.getUsuarioNome() != null ? pena.getUsuarioNome() : "Apenado #" + pena.getUsuarioId());

        if (this.apenadoSelecionado == null) {
            this.apenadoSelecionado = new UsuarioDTO();
            this.apenadoSelecionado.setIdUsuario(pena.getUsuarioId());
            this.apenadoSelecionado.setNome(pena.getUsuarioNome());
        }

        if (cbInstituicao.getItems() != null) {
            for (InstituicaoDTO inst : cbInstituicao.getItems()) {
                if (inst.getIdInstituicao().equals(pena.getInstituicaoPrincipalId())) {
                    cbInstituicao.setValue(inst);
                    break;
                }
            }
        }

        cbTipoPena.setValue(pena.getTipoPena());
        txtHorasTotais.setText(String.valueOf(pena.getHorasTotais()));
        txtHorasSemanais.setText(String.valueOf(pena.getHorasSemanais()));
        dpDataInicio.setValue(pena.getDataInicio());
        dpDataTermino.setValue(pena.getDataTermino());
        txtAtividades.setText(pena.getAtividadesAcordadas() != null ? pena.getAtividadesAcordadas() : "");

        desmontarContratoDiasHorarios(pena.getDiasSemanaEHorariosDisponivel());
        lblFeedback.setVisible(false);
    }

    @FXML
    public void handleCancelarEdicao() {
        this.penaIdEmEdicao = null;
        lblTituloForm.setText("Lancar Nova Pena Alternativa");
        lblSubtituloForm.setText("Vincule o apenado a uma instituicao parceira e defina os termos e contrato de cumprimento.");
        btnCancelarEdicao.setVisible(false);
        btnCancelarEdicao.setManaged(false);
        btnSalvarPena.setText("Salvar e Registrar Pena");

        txtCpfApenado.setDisable(false);
        btnBuscarApenado.setDisable(false);
        txtCpfApenado.clear();
        boxApenadoEncontrado.setVisible(false);
        boxApenadoEncontrado.setManaged(false);
        this.apenadoSelecionado = null;

        cbInstituicao.getSelectionModel().clearSelection();
        txtHorasTotais.clear();
        txtHorasSemanais.clear();
        dpDataInicio.setValue(LocalDate.now());
        dpDataTermino.setValue(null);
        txtAtividades.clear();
        txtHorasDiarias.setText("4");
        txtTurnoAcordado.setText("08:00 às 12:00");
        chkSeg.setSelected(true);
        chkTer.setSelected(true);
        chkQua.setSelected(true);
        chkQui.setSelected(true);
        chkSex.setSelected(true);
        chkSab.setSelected(false);
        chkDom.setSelected(false);
        lblFeedback.setVisible(false);
    }

    private String montarContratoDiasHorarios() {
        List<String> dias = new ArrayList<>();
        if (chkSeg.isSelected()) dias.add("Seg");
        if (chkTer.isSelected()) dias.add("Ter");
        if (chkQua.isSelected()) dias.add("Qua");
        if (chkQui.isSelected()) dias.add("Qui");
        if (chkSex.isSelected()) dias.add("Sex");
        if (chkSab.isSelected()) dias.add("Sáb");
        if (chkDom.isSelected()) dias.add("Dom");

        String strDias = dias.isEmpty() ? "A definir" : String.join(", ", dias);
        String hDiarias = txtHorasDiarias.getText().trim();
        if (hDiarias.isEmpty()) hDiarias = "4";
        String turno = txtTurnoAcordado.getText().trim();
        if (turno.isEmpty()) turno = "Horário regular";

        return strDias + " | " + hDiarias + "h/dia (" + turno + ")";
    }

    private void desmontarContratoDiasHorarios(String valor) {
        if (valor == null || valor.isBlank()) {
            chkSeg.setSelected(true);
            chkTer.setSelected(true);
            chkQua.setSelected(true);
            chkQui.setSelected(true);
            chkSex.setSelected(true);
            chkSab.setSelected(false);
            chkDom.setSelected(false);
            txtHorasDiarias.setText("4");
            txtTurnoAcordado.setText("08:00 às 12:00");
            return;
        }

        chkSeg.setSelected(valor.contains("Seg"));
        chkTer.setSelected(valor.contains("Ter"));
        chkQua.setSelected(valor.contains("Qua"));
        chkQui.setSelected(valor.contains("Qui"));
        chkSex.setSelected(valor.contains("Sex"));
        chkSab.setSelected(valor.contains("Sáb") || valor.contains("Sab"));
        chkDom.setSelected(valor.contains("Dom"));

        if (valor.contains("|")) {
            String[] partes = valor.split("\\|");
            if (partes.length > 1) {
                String dadosHorario = partes[1].trim();
                if (dadosHorario.contains("h/dia")) {
                    String[] tokens = dadosHorario.split("h/dia");
                    txtHorasDiarias.setText(tokens[0].trim());
                    if (tokens.length > 1) {
                        String turnoLimpo = tokens[1].replace("(", "").replace(")", "").trim();
                        txtTurnoAcordado.setText(turnoLimpo);
                    }
                }
            }
        }
    }

    @FXML
    public void handleBuscarApenado() {
        String cpf = txtCpfApenado.getText().trim().replaceAll("\\D", "");
        if (cpf.length() != 11) {
            FormValidator.marcarErro(txtCpfApenado, lblCpfError, "Informe um CPF valido com 11 digitos");
            return;
        }

        new Thread(() -> {
            ApiResponse<UsuarioDTO> resp = apiClient.buscarUsuarioPorCpf(cpf);
            Platform.runLater(() -> {
                if (resp.isSuccess() && resp.getData() != null) {
                    apenadoSelecionado = resp.getData();
                    lblNomeApenado.setText(apenadoSelecionado.getNome() + " (Código: " + apenadoSelecionado.getCodigo() + ")");
                    boxApenadoEncontrado.setVisible(true);
                    boxApenadoEncontrado.setManaged(true);
                    FormValidator.limparErro(txtCpfApenado, lblCpfError);
                } else {
                    apenadoSelecionado = null;
                    boxApenadoEncontrado.setVisible(false);
                    boxApenadoEncontrado.setManaged(false);
                    FormValidator.marcarErro(txtCpfApenado, lblCpfError, "Apenado nao encontrado para o CPF informado");
                }
            });
        }).start();
    }

    @FXML
    public void handleNovoTipoPena() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Novo Tipo de Pena");
        dialog.setHeaderText("Cadastrar Novo Tipo de Medida Alternativa");
        dialog.setContentText("Descricao do tipo de pena:");

        dialog.showAndWait().ifPresent(tipo -> {
            String valor = tipo.trim();
            if (!valor.isEmpty()) {
                if (!cbTipoPena.getItems().contains(valor)) {
                    cbTipoPena.getItems().add(valor);
                }
                cbTipoPena.setValue(valor);
                lblTipoPenaError.setText("");
            }
        });
    }

    @FXML
    public void handleCalcularEstimativa() {
        try {
            int horasTotais = Integer.parseInt(txtHorasTotais.getText().trim());
            int horasSemanais = Integer.parseInt(txtHorasSemanais.getText().trim());
            LocalDate inicio = dpDataInicio.getValue();

            if (inicio == null || horasSemanais <= 0 || horasTotais <= 0) {
                lblEstimativaTexto.setText("Preencha horas totais, horas semanais e data de inicio.");
                return;
            }

            EstimativaPenaDTO req = new EstimativaPenaDTO();
            req.setHorasTotais(horasTotais);
            req.setHorasSemanais(horasSemanais);
            req.setDataInicio(inicio);

            new Thread(() -> {
                ApiResponse<EstimativaPenaDTO> resp = apiClient.calcularEstimativaPena(req);
                Platform.runLater(() -> {
                    if (resp.isSuccess() && resp.getData() != null) {
                        dpDataTermino.setValue(resp.getData().getDataTerminoEstimada());
                        lblEstimativaTexto.setText("Estimativa: " + resp.getData().getTempoEstimadoMeses() + " meses.");
                    }
                });
            }).start();
        } catch (NumberFormatException e) {
            lblEstimativaTexto.setText("Horas devem ser valores numericos inteiros.");
        }
    }

    @FXML
    public void handleSalvarPena() {
        boolean apenadoVal = true;
        if (apenadoSelecionado == null && penaIdEmEdicao == null) {
            FormValidator.marcarErro(txtCpfApenado, lblCpfError, "Localize um apenado antes de registrar");
            apenadoVal = false;
        }

        InstituicaoDTO instituicao = cbInstituicao.getValue();
        boolean instVal = FormValidator.validarCampoObrigatorio(cbInstituicao, lblInstituicaoError, "Selecione uma instituicao parceira");

        String tipoPena = null;
        if (cbTipoPena.getEditor() != null && cbTipoPena.getEditor().getText() != null && !cbTipoPena.getEditor().getText().isBlank()) {
            tipoPena = cbTipoPena.getEditor().getText().trim();
        } else if (cbTipoPena.getValue() != null && !cbTipoPena.getValue().isBlank()) {
            tipoPena = cbTipoPena.getValue().trim();
        }

        boolean tipoVal = true;
        if (tipoPena == null || tipoPena.isEmpty()) {
            FormValidator.marcarErro(cbTipoPena, lblTipoPenaError, "Informe ou selecione o tipo de pena");
            tipoVal = false;
        } else {
            FormValidator.limparErro(cbTipoPena, lblTipoPenaError);
        }

        boolean hTotaisVal = FormValidator.validarNumeroInteiroPositivo(txtHorasTotais, lblHorasTotaisError, "Horas totais sao obrigatorias");
        boolean hSemanaisVal = FormValidator.validarNumeroInteiroPositivo(txtHorasSemanais, lblHorasSemanaisError, "Horas semanais sao obrigatorias");
        boolean dataInicioVal = FormValidator.validarCampoObrigatorio(dpDataInicio, lblDataInicioError, "Data de inicio e obrigatoria");

        if (!apenadoVal || !instVal || !tipoVal || !hTotaisVal || !hSemanaisVal || !dataInicioVal) {
            lblFeedback.setText("Preencha todos os campos obrigatorios destacados em vermelho.");
            lblFeedback.getStyleClass().setAll("banner-error");
            lblFeedback.setVisible(true);
            return;
        }

        btnSalvarPena.setDisable(true);
        lblFeedback.setVisible(false);

        CadastroPenaDTO dto = new CadastroPenaDTO();
        if (apenadoSelecionado != null) {
            dto.setUsuarioId(apenadoSelecionado.getIdUsuario());
        }
        dto.setInstituicaoId(instituicao.getIdInstituicao());
        dto.setTipoPena(tipoPena);
        dto.setHorasTotais(Integer.parseInt(txtHorasTotais.getText().trim()));
        dto.setHorasSemanais(Integer.parseInt(txtHorasSemanais.getText().trim()));
        dto.setDataInicio(dpDataInicio.getValue());
        dto.setDataTermino(dpDataTermino.getValue());
        dto.setDiasSemanaEHorariosDisponivel(montarContratoDiasHorarios());
        dto.setAtividadesAcordadas(txtAtividades.getText());

        final boolean isEdicao = (penaIdEmEdicao != null);
        final Long idPenaAtual = penaIdEmEdicao;

        new Thread(() -> {
            ApiResponse<PenaDTO> response;
            if (isEdicao) {
                response = apiClient.atualizarPena(idPenaAtual, dto);
            } else {
                response = apiClient.cadastrarPena(dto);
            }

            Platform.runLater(() -> {
                btnSalvarPena.setDisable(false);
                if (response.isSuccess()) {
                    lblFeedback.setText(isEdicao ? "Contrato de cumprimento de pena atualizado com sucesso!" : "Pena alternativa e contrato cadastrados com sucesso!");
                    lblFeedback.getStyleClass().setAll("banner-success");
                    lblFeedback.setVisible(true);
                    carregarPenas();
                    handleCancelarEdicao();
                } else {
                    lblFeedback.setText("Erro ao salvar contrato: " + response.getErrorMessage());
                    lblFeedback.getStyleClass().setAll("banner-error");
                    lblFeedback.setVisible(true);
                }
            });
        }).start();
    }
}
