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
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

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
    private FlowPane paneOutrasInstituicoes;

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
    private TextField txtSegIni1, txtSegFim1, txtSegIni2, txtSegFim2;

    @FXML
    private CheckBox chkTer;
    @FXML
    private TextField txtTerIni1, txtTerFim1, txtTerIni2, txtTerFim2;

    @FXML
    private CheckBox chkQua;
    @FXML
    private TextField txtQuaIni1, txtQuaFim1, txtQuaIni2, txtQuaFim2;

    @FXML
    private CheckBox chkQui;
    @FXML
    private TextField txtQuiIni1, txtQuiFim1, txtQuiIni2, txtQuiFim2;

    @FXML
    private CheckBox chkSex;
    @FXML
    private TextField txtSexIni1, txtSexFim1, txtSexIni2, txtSexFim2;

    @FXML
    private CheckBox chkSab;
    @FXML
    private TextField txtSabIni1, txtSabFim1, txtSabIni2, txtSabFim2;

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
    private final List<InstituicaoDTO> listaInstituicoesTodas = new ArrayList<>();
    private final Map<Long, CheckBox> mapCheckboxesInstituicoes = new LinkedHashMap<>();

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

        txtHorasTotais.textProperty().addListener((obs, oldVal, newVal) -> calcularEstimativaAutomatica());
        txtHorasSemanais.textProperty().addListener((obs, oldVal, newVal) -> calcularEstimativaAutomatica());
        dpDataInicio.valueProperty().addListener((obs, oldVal, newVal) -> calcularEstimativaAutomatica());

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

        cbInstituicao.valueProperty().addListener((obs, oldVal, newVal) -> {
            atualizarVisibilidadeCheckboxesOutrasInstituicoes(newVal);
        });
    }

    private void atualizarVisibilidadeCheckboxesOutrasInstituicoes(InstituicaoDTO principal) {
        Long idPrincipal = principal != null ? principal.getIdInstituicao() : null;
        for (Map.Entry<Long, CheckBox> entry : mapCheckboxesInstituicoes.entrySet()) {
            boolean isPrincipal = entry.getKey().equals(idPrincipal);
            entry.getValue().setDisable(isPrincipal);
            if (isPrincipal) {
                entry.getValue().setSelected(false);
            }
        }
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
                    listaInstituicoesTodas.clear();
                    listaInstituicoesTodas.addAll(resp.getData());
                    cbInstituicao.setItems(FXCollections.observableArrayList(listaInstituicoesTodas));
                    renderizarCheckboxesOutrasInstituicoes();
                }
            });
        }).start();
    }

    private void renderizarCheckboxesOutrasInstituicoes() {
        paneOutrasInstituicoes.getChildren().clear();
        mapCheckboxesInstituicoes.clear();

        InstituicaoDTO principal = cbInstituicao.getValue();
        Long idPrincipal = principal != null ? principal.getIdInstituicao() : null;

        for (InstituicaoDTO inst : listaInstituicoesTodas) {
            CheckBox cb = new CheckBox(inst.getNome());
            cb.setStyle("-fx-font-size: 11px; -fx-text-fill: #334155;");
            boolean isPrincipal = inst.getIdInstituicao().equals(idPrincipal);
            cb.setDisable(isPrincipal);
            mapCheckboxesInstituicoes.put(inst.getIdInstituicao(), cb);
            paneOutrasInstituicoes.getChildren().add(cb);
        }
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
        lblSubtituloForm.setText("Ajuste os termos, instituicoes parceiras e contrato de trabalho acordado com o apenado.");
        btnCancelarEdicao.setVisible(true);
        btnCancelarEdicao.setManaged(true);
        btnSalvarPena.setText("Salvar Alteracoes do Contrato");

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

        for (Map.Entry<Long, CheckBox> entry : mapCheckboxesInstituicoes.entrySet()) {
            boolean vinculada = pena.getInstituicoesVinculadasIds() != null && pena.getInstituicoesVinculadasIds().contains(entry.getKey());
            entry.getValue().setSelected(vinculada);
        }

        cbTipoPena.setValue(pena.getTipoPena());
        txtHorasTotais.setText(String.valueOf(pena.getHorasTotais()));
        txtHorasSemanais.setText(String.valueOf(pena.getHorasSemanais()));
        dpDataInicio.setValue(pena.getDataInicio());
        dpDataTermino.setValue(pena.getDataTermino());
        txtAtividades.setText(pena.getAtividadesAcordadas() != null ? pena.getAtividadesAcordadas() : "");

        desmontarContratoDiasHorarios(pena.getDiasSemanaEHorariosDisponivel());
        calcularEstimativaAutomatica();
        lblFeedback.setVisible(false);
    }

    @FXML
    public void handleCancelarEdicao() {
        this.penaIdEmEdicao = null;
        lblTituloForm.setText("Lancar Nova Pena Alternativa");
        lblSubtituloForm.setText("Vincule o apenado a instituicoes parceiras e defina os termos e contrato de cumprimento.");
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
        for (CheckBox cb : mapCheckboxesInstituicoes.values()) {
            cb.setSelected(false);
            cb.setDisable(false);
        }

        txtHorasTotais.clear();
        txtHorasSemanais.clear();
        dpDataInicio.setValue(LocalDate.now());
        dpDataTermino.setValue(null);
        txtAtividades.clear();
        lblEstimativaTexto.setText("");

        handlePreencherComercial();
        lblFeedback.setVisible(false);
    }

    private void calcularEstimativaAutomatica() {
        try {
            String totStr = txtHorasTotais.getText();
            String semStr = txtHorasSemanais.getText();
            LocalDate ini = dpDataInicio.getValue();

            if (totStr == null || totStr.isBlank() || semStr == null || semStr.isBlank() || ini == null) {
                return;
            }

            int horasTotais = Integer.parseInt(totStr.trim());
            int horasSemanais = Integer.parseInt(semStr.trim());

            if (horasTotais <= 0 || horasSemanais <= 0) {
                return;
            }

            double mesesEstimados = horasTotais / (horasSemanais * 4.0);
            long diasEstimados = (long) Math.ceil((horasTotais / (double) horasSemanais) * 7.0);
            LocalDate termino = ini.plusDays(diasEstimados);
            dpDataTermino.setValue(termino);

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            lblEstimativaTexto.setText("Estimativa: " + String.format(Locale.US, "%.1f", mesesEstimados) + " meses (~" + Math.round(horasTotais / (double) horasSemanais) + " semanas). Conclusao prevista: " + termino.format(fmt));
        } catch (NumberFormatException ignored) {
        }
    }

    @FXML
    public void handlePreencherComercial() {
        chkSeg.setSelected(true);
        txtSegIni1.setText("08:00"); txtSegFim1.setText("12:00"); txtSegIni2.setText("13:00"); txtSegFim2.setText("17:00");

        chkTer.setSelected(true);
        txtTerIni1.setText("08:00"); txtTerFim1.setText("12:00"); txtTerIni2.setText("13:00"); txtTerFim2.setText("17:00");

        chkQua.setSelected(true);
        txtQuaIni1.setText("08:00"); txtQuaFim1.setText("12:00"); txtQuaIni2.setText("13:00"); txtQuaFim2.setText("17:00");

        chkQui.setSelected(true);
        txtQuiIni1.setText("08:00"); txtQuiFim1.setText("12:00"); txtQuiIni2.setText("13:00"); txtQuiFim2.setText("17:00");

        chkSex.setSelected(true);
        txtSexIni1.setText("08:00"); txtSexFim1.setText("12:00"); txtSexIni2.setText("13:00"); txtSexFim2.setText("17:00");

        chkSab.setSelected(false);
        txtSabIni1.setText(""); txtSabFim1.setText(""); txtSabIni2.setText(""); txtSabFim2.setText("");
    }

    @FXML
    public void handlePreencherMeioPeriodo() {
        chkSeg.setSelected(true);
        txtSegIni1.setText("08:00"); txtSegFim1.setText("12:00"); txtSegIni2.setText(""); txtSegFim2.setText("");

        chkTer.setSelected(true);
        txtTerIni1.setText("08:00"); txtTerFim1.setText("12:00"); txtTerIni2.setText(""); txtTerFim2.setText("");

        chkQua.setSelected(true);
        txtQuaIni1.setText("08:00"); txtQuaFim1.setText("12:00"); txtQuaIni2.setText(""); txtQuaFim2.setText("");

        chkQui.setSelected(true);
        txtQuiIni1.setText("08:00"); txtQuiFim1.setText("12:00"); txtQuiIni2.setText(""); txtQuiFim2.setText("");

        chkSex.setSelected(true);
        txtSexIni1.setText("08:00"); txtSexFim1.setText("12:00"); txtSexIni2.setText(""); txtSexFim2.setText("");

        chkSab.setSelected(false);
        txtSabIni1.setText(""); txtSabFim1.setText(""); txtSabIni2.setText(""); txtSabFim2.setText("");
    }

    @FXML
    public void handlePreencherFimDeSemana() {
        chkSeg.setSelected(false);
        txtSegIni1.setText(""); txtSegFim1.setText(""); txtSegIni2.setText(""); txtSegFim2.setText("");

        chkTer.setSelected(false);
        txtTerIni1.setText(""); txtTerFim1.setText(""); txtTerIni2.setText(""); txtTerFim2.setText("");

        chkQua.setSelected(false);
        txtQuaIni1.setText(""); txtQuaFim1.setText(""); txtQuaIni2.setText(""); txtQuaFim2.setText("");

        chkQui.setSelected(false);
        txtQuiIni1.setText(""); txtQuiFim1.setText(""); txtQuiIni2.setText(""); txtQuiFim2.setText("");

        chkSex.setSelected(false);
        txtSexIni1.setText(""); txtSexFim1.setText(""); txtSexIni2.setText(""); txtSexFim2.setText("");

        chkSab.setSelected(true);
        txtSabIni1.setText("08:00"); txtSabFim1.setText("14:00"); txtSabIni2.setText(""); txtSabFim2.setText("");
    }

    private String montarContratoDiasHorarios() {
        List<String> partes = new ArrayList<>();
        appendTurnosDia(partes, "Segunda", chkSeg, txtSegIni1, txtSegFim1, txtSegIni2, txtSegFim2);
        appendTurnosDia(partes, "Terça", chkTer, txtTerIni1, txtTerFim1, txtTerIni2, txtTerFim2);
        appendTurnosDia(partes, "Quarta", chkQua, txtQuaIni1, txtQuaFim1, txtQuaIni2, txtQuaFim2);
        appendTurnosDia(partes, "Quinta", chkQui, txtQuiIni1, txtQuiFim1, txtQuiIni2, txtQuiFim2);
        appendTurnosDia(partes, "Sexta", chkSex, txtSexIni1, txtSexFim1, txtSexIni2, txtSexFim2);
        appendTurnosDia(partes, "Sábado", chkSab, txtSabIni1, txtSabFim1, txtSabIni2, txtSabFim2);

        if (partes.isEmpty()) {
            return "Segunda a Sexta (Horário regular)";
        }
        return String.join(", ", partes);
    }

    private void appendTurnosDia(List<String> partes, String dia, CheckBox chk, TextField i1, TextField f1, TextField i2, TextField f2) {
        if (!chk.isSelected()) return;
        StringBuilder sb = new StringBuilder(dia);
        String hi1 = i1.getText() != null ? i1.getText().trim() : "";
        String hf1 = f1.getText() != null ? f1.getText().trim() : "";
        String hi2 = i2.getText() != null ? i2.getText().trim() : "";
        String hf2 = f2.getText() != null ? f2.getText().trim() : "";

        if (!hi1.isEmpty() && !hf1.isEmpty()) {
            sb.append(" ").append(hi1).append("-").append(hf1);
        }
        if (!hi2.isEmpty() && !hf2.isEmpty()) {
            sb.append(" ").append(hi2).append("-").append(hf2);
        }
        partes.add(sb.toString());
    }

    private void desmontarContratoDiasHorarios(String valor) {
        if (valor == null || valor.isBlank()) {
            handlePreencherComercial();
            return;
        }

        chkSeg.setSelected(false);
        chkTer.setSelected(false);
        chkQua.setSelected(false);
        chkQui.setSelected(false);
        chkSex.setSelected(false);
        chkSab.setSelected(false);

        String[] itens = valor.split(",\\s*");
        for (String item : itens) {
            String lower = item.toLowerCase();
            if (lower.contains("seg")) {
                preencherCamposDia(chkSeg, txtSegIni1, txtSegFim1, txtSegIni2, txtSegFim2, item);
            } else if (lower.contains("ter")) {
                preencherCamposDia(chkTer, txtTerIni1, txtTerFim1, txtTerIni2, txtTerFim2, item);
            } else if (lower.contains("qua")) {
                preencherCamposDia(chkQua, txtQuaIni1, txtQuaFim1, txtQuaIni2, txtQuaFim2, item);
            } else if (lower.contains("qui")) {
                preencherCamposDia(chkQui, txtQuiIni1, txtQuiFim1, txtQuiIni2, txtQuiFim2, item);
            } else if (lower.contains("sex")) {
                preencherCamposDia(chkSex, txtSexIni1, txtSexFim1, txtSexIni2, txtSexFim2, item);
            } else if (lower.contains("sáb") || lower.contains("sab")) {
                preencherCamposDia(chkSab, txtSabIni1, txtSabFim1, txtSabIni2, txtSabFim2, item);
            }
        }
    }

    private void preencherCamposDia(CheckBox chk, TextField i1, TextField f1, TextField i2, TextField f2, String texto) {
        chk.setSelected(true);
        i1.clear(); f1.clear(); i2.clear(); f2.clear();

        String[] tokens = texto.trim().split("\\s+");
        List<String> faixas = new ArrayList<>();
        for (int i = 1; i < tokens.length; i++) {
            faixas.add(tokens[i]);
        }

        if (faixas.size() == 1 && faixas.get(0).contains("-")) {
            String[] p = faixas.get(0).split("-");
            if (p.length > 0) i1.setText(p[0]);
            if (p.length > 1) f1.setText(p[1]);
        } else if (faixas.size() >= 2 && faixas.get(0).contains("-") && faixas.get(1).contains("-")) {
            String[] p1 = faixas.get(0).split("-");
            if (p1.length > 0) i1.setText(p1[0]);
            if (p1.length > 1) f1.setText(p1[1]);

            String[] p2 = faixas.get(1).split("-");
            if (p2.length > 0) i2.setText(p2[0]);
            if (p2.length > 1) f2.setText(p2[1]);
        } else if (faixas.size() >= 2) {
            i1.setText(faixas.get(0));
            f1.setText(faixas.get(1));
            if (faixas.size() >= 4) {
                i2.setText(faixas.get(2));
                f2.setText(faixas.get(3));
            }
        } else {
            i1.setText("08:00");
            f1.setText("12:00");
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

        List<Long> outrasInstIds = new ArrayList<>();
        for (Map.Entry<Long, CheckBox> entry : mapCheckboxesInstituicoes.entrySet()) {
            if (entry.getValue().isSelected() && !entry.getKey().equals(instituicao.getIdInstituicao())) {
                outrasInstIds.add(entry.getKey());
            }
        }
        dto.setOutrasInstituicoesIds(outrasInstIds);

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
