package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class FrequenciaController {

    @FXML
    private TextField txtCpfBusca;

    @FXML
    private Label lblApenadoNome;

    @FXML
    private ComboBox<PenaDTO> cbPenas;

    @FXML
    private VBox boxResumo;

    @FXML
    private Label lblStatusCumprimento;

    @FXML
    private ProgressBar pbProgresso;

    @FXML
    private Label lblHorasCumpridas;

    @FXML
    private Label lblHorasRestantes;

    @FXML
    private Label lblHorasTotais;

    @FXML
    private Label lblPercentual;

    @FXML
    private VBox boxLancamento;

    @FXML
    private Label lblFeedbackLancamento;

    @FXML
    private DatePicker dpDataTrabalho;

    @FXML
    private ComboBox<InstituicaoDTO> cbInstituicaoFrequencia;

    @FXML
    private TextField txtHorarioInicio;

    @FXML
    private TextField txtHorarioAlmoco;

    @FXML
    private TextField txtHorarioVolta;

    @FXML
    private TextField txtHorarioSaida;

    @FXML
    private TextField txtHorasCumpridas;

    @FXML
    private Label lblCalculoAuto;

    @FXML
    private TextField txtAtividades;

    @FXML
    private Button btnSalvarRegistro;

    @FXML
    private VBox boxHistorico;

    @FXML
    private TableView<RegistroTrabalhoDTO> tblHistorico;

    @FXML
    private TableColumn<RegistroTrabalhoDTO, Number> colHistId;

    @FXML
    private TableColumn<RegistroTrabalhoDTO, String> colHistData;

    @FXML
    private TableColumn<RegistroTrabalhoDTO, String> colHistEntrada;

    @FXML
    private TableColumn<RegistroTrabalhoDTO, String> colHistAlmoco;

    @FXML
    private TableColumn<RegistroTrabalhoDTO, String> colHistVolta;

    @FXML
    private TableColumn<RegistroTrabalhoDTO, String> colHistSaida;

    @FXML
    private TableColumn<RegistroTrabalhoDTO, String> colHistHoras;

    @FXML
    private TableColumn<RegistroTrabalhoDTO, String> colHistInstituicao;

    @FXML
    private TableColumn<RegistroTrabalhoDTO, String> colHistObs;

    private final CpmaApiClient apiClient = new CpmaApiClient();
    private UsuarioDTO apenadoAtual;

    @FXML
    public void initialize() {
        dpDataTrabalho.setValue(LocalDate.now());
        configurarColunasHistorico();
        configurarComboBoxPena();
        configurarComboBoxInstituicao();
        carregarInstituicoes();
        configurarListenersCalculoHoras();

        cbPenas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                carregarResumoEHistorico(newVal.getIdPena());
                preSelecionarInstituicao(newVal);
            }
        });
    }

    private void configurarColunasHistorico() {
        colHistId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getIdRegistro()));
        colHistData.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataTrabalho() != null ? c.getValue().getDataTrabalho().toString() : "-"));
        colHistEntrada.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHorarioInicio() != null ? c.getValue().getHorarioInicio() : "-"));
        colHistAlmoco.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHorarioAlmoco() != null ? c.getValue().getHorarioAlmoco() : "-"));
        colHistVolta.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHorarioVolta() != null ? c.getValue().getHorarioVolta() : "-"));
        colHistSaida.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHorarioSaida() != null ? c.getValue().getHorarioSaida() : "-"));
        colHistHoras.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHorasCumpridas() != null ? String.format("%.1fh", c.getValue().getHorasCumpridas()) : "-"));
        colHistInstituicao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInstituicaoNome() != null ? c.getValue().getInstituicaoNome() : "-"));
        colHistObs.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAtividades() != null ? c.getValue().getAtividades() : "-"));
    }

    private void configurarComboBoxPena() {
        cbPenas.setConverter(new StringConverter<PenaDTO>() {
            @Override
            public String toString(PenaDTO p) {
                if (p == null) return "";
                String inst = p.getInstituicaoPrincipalNome() != null ? " (" + p.getInstituicaoPrincipalNome() + ")" : "";
                return p.getTipoPena() + " - " + p.getHorasTotais() + "h" + inst;
            }

            @Override
            public PenaDTO fromString(String string) {
                return null;
            }
        });
    }

    private void configurarComboBoxInstituicao() {
        cbInstituicaoFrequencia.setConverter(new StringConverter<InstituicaoDTO>() {
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

    private void carregarInstituicoes() {
        new Thread(() -> {
            ApiResponse<List<InstituicaoDTO>> resp = apiClient.listarInstituicoes();
            Platform.runLater(() -> {
                if (resp.isSuccess() && resp.getData() != null) {
                    cbInstituicaoFrequencia.setItems(FXCollections.observableArrayList(resp.getData()));
                }
            });
        }).start();
    }

    private void preSelecionarInstituicao(PenaDTO pena) {
        if (pena == null || cbInstituicaoFrequencia.getItems() == null) return;
        for (InstituicaoDTO inst : cbInstituicaoFrequencia.getItems()) {
            if (inst.getIdInstituicao().equals(pena.getInstituicaoPrincipalId())) {
                cbInstituicaoFrequencia.setValue(inst);
                break;
            }
        }
    }

    private void configurarListenersCalculoHoras() {
        txtHorarioInicio.textProperty().addListener((obs, oldVal, newVal) -> calcularHorasEmTempoReal());
        txtHorarioAlmoco.textProperty().addListener((obs, oldVal, newVal) -> calcularHorasEmTempoReal());
        txtHorarioVolta.textProperty().addListener((obs, oldVal, newVal) -> calcularHorasEmTempoReal());
        txtHorarioSaida.textProperty().addListener((obs, oldVal, newVal) -> calcularHorasEmTempoReal());
    }

    private void calcularHorasEmTempoReal() {
        String strInicio = txtHorarioInicio.getText().trim();
        String strAlmoco = txtHorarioAlmoco.getText().trim();
        String strVolta = txtHorarioVolta.getText().trim();
        String strSaida = txtHorarioSaida.getText().trim();

        LocalTime tInicio = parseHora(strInicio);
        LocalTime tAlmoco = parseHora(strAlmoco);
        LocalTime tVolta = parseHora(strVolta);
        LocalTime tSaida = parseHora(strSaida);

        if (tInicio == null || tSaida == null) {
            return;
        }

        double minutosTotais = 0.0;
        if (tAlmoco != null && tVolta != null) {
            long manha = Duration.between(tInicio, tAlmoco).toMinutes();
            long tarde = Duration.between(tVolta, tSaida).toMinutes();
            if (manha > 0) minutosTotais += manha;
            if (tarde > 0) minutosTotais += tarde;
        } else {
            long total = Duration.between(tInicio, tSaida).toMinutes();
            if (total > 0) minutosTotais = total;
        }

        double horasCalculadas = minutosTotais / 60.0;
        if (horasCalculadas > 0.0) {
            txtHorasCumpridas.setText(String.format(java.util.Locale.US, "%.1f", horasCalculadas));
            lblCalculoAuto.setText("Cálculo automático: " + String.format("%.1f", horasCalculadas) + " horas líquidas apuradas.");
        }
    }

    private LocalTime parseHora(String texto) {
        if (texto == null || texto.isBlank()) return null;
        try {
            String limpo = texto.trim();
            if (limpo.length() == 4 && !limpo.contains(":")) {
                limpo = limpo.substring(0, 2) + ":" + limpo.substring(2);
            }
            return LocalTime.parse(limpo);
        } catch (Exception e) {
            return null;
        }
    }

    @FXML
    public void handleBuscarApenado() {
        String termo = txtCpfBusca.getText().trim().replaceAll("\\D", "");
        if (termo.length() != 11) {
            lblApenadoNome.setText("Informe um CPF valido com 11 digitos.");
            esconderSecoes();
            return;
        }

        lblApenadoNome.setText("Buscando...");
        new Thread(() -> {
            ApiResponse<UsuarioDTO> resp = apiClient.buscarUsuarioPorCpf(termo);
            Platform.runLater(() -> {
                if (resp.isSuccess() && resp.getData() != null) {
                    apenadoAtual = resp.getData();
                    lblApenadoNome.setText(apenadoAtual.getNome() + " (Código: " + apenadoAtual.getCodigo() + ")");
                    carregarPenasDoApenado(apenadoAtual.getIdUsuario());
                } else {
                    apenadoAtual = null;
                    lblApenadoNome.setText("Apenado nao encontrado.");
                    esconderSecoes();
                }
            });
        }).start();
    }

    private void carregarPenasDoApenado(Long usuarioId) {
        new Thread(() -> {
            ApiResponse<List<PenaDTO>> resp = apiClient.listarPenasPorUsuario(usuarioId);
            Platform.runLater(() -> {
                if (resp.isSuccess() && resp.getData() != null && !resp.getData().isEmpty()) {
                    cbPenas.setItems(FXCollections.observableArrayList(resp.getData()));
                    cbPenas.getSelectionModel().selectFirst();
                    mostrarSecoes();
                } else {
                    cbPenas.getItems().clear();
                    esconderSecoes();
                    lblApenadoNome.setText(apenadoAtual.getNome() + " (Nenhuma pena ativa)");
                }
            });
        }).start();
    }

    private void mostrarSecoes() {
        boxResumo.setVisible(true);
        boxResumo.setManaged(true);
        boxLancamento.setVisible(true);
        boxLancamento.setManaged(true);
        boxHistorico.setVisible(true);
        boxHistorico.setManaged(true);
    }

    private void carregarResumoEHistorico(Long penaId) {
        if (penaId == null) {
            return;
        }

        new Thread(() -> {
            ApiResponse<ResumoCumprimentoDTO> respResumo = apiClient.obterResumoCumprimento(penaId);
            ApiResponse<List<RegistroTrabalhoDTO>> respHist = apiClient.listarRegistrosPorPena(penaId);

            Platform.runLater(() -> {
                if (respResumo.isSuccess() && respResumo.getData() != null) {
                    ResumoCumprimentoDTO r = respResumo.getData();
                    lblHorasCumpridas.setText("Cumpridas: " + r.getHorasCumpridas() + "h");
                    lblHorasRestantes.setText("Restantes: " + r.getHorasRestantes() + "h");
                    lblHorasTotais.setText("Total: " + r.getHorasTotais() + "h");
                    lblPercentual.setText(r.getPercentualConcluido() + "%");
                    pbProgresso.setProgress(r.getPercentualConcluido() / 100.0);

                    if ("CONCLUIDO".equalsIgnoreCase(r.getStatus())) {
                        lblStatusCumprimento.setText("PENA CONCLUIDA");
                        lblStatusCumprimento.getStyleClass().setAll("status-badge-online");
                    } else {
                        lblStatusCumprimento.setText("EM CUMPRIMENTO");
                        lblStatusCumprimento.getStyleClass().setAll("status-badge-offline");
                    }
                }

                if (respHist.isSuccess() && respHist.getData() != null) {
                    tblHistorico.setItems(FXCollections.observableArrayList(respHist.getData()));
                }
            });
        }).start();
    }

    @FXML
    public void handleSalvarRegistro() {
        PenaDTO pena = cbPenas.getValue();
        if (pena == null) {
            return;
        }

        double horas;
        try {
            horas = Double.parseDouble(txtHorasCumpridas.getText().trim().replace(",", "."));
            if (horas <= 0) {
                lblFeedbackLancamento.setText("Horas devem ser maiores que zero.");
                lblFeedbackLancamento.getStyleClass().setAll("banner-error");
                lblFeedbackLancamento.setVisible(true);
                return;
            }
        } catch (NumberFormatException e) {
            lblFeedbackLancamento.setText("Informe um numero valido de horas cumpridas.");
            lblFeedbackLancamento.getStyleClass().setAll("banner-error");
            lblFeedbackLancamento.setVisible(true);
            return;
        }

        btnSalvarRegistro.setDisable(true);

        RegistroTrabalhoDTO dto = new RegistroTrabalhoDTO();
        dto.setPenaId(pena.getIdPena());
        dto.setDataTrabalho(dpDataTrabalho.getValue());
        dto.setHorasCumpridas(horas);
        dto.setHorarioInicio(txtHorarioInicio.getText().trim());
        dto.setHorarioAlmoco(txtHorarioAlmoco.getText().trim());
        dto.setHorarioVolta(txtHorarioVolta.getText().trim());
        dto.setHorarioSaida(txtHorarioSaida.getText().trim());
        dto.setAtividades(txtAtividades.getText());

        InstituicaoDTO instSelecionada = cbInstituicaoFrequencia.getValue();
        if (instSelecionada != null) {
            dto.setInstituicaoId(instSelecionada.getIdInstituicao());
        }

        new Thread(() -> {
            ApiResponse<RegistroTrabalhoDTO> resp = apiClient.registrarTrabalho(dto);
            Platform.runLater(() -> {
                btnSalvarRegistro.setDisable(false);
                if (resp.isSuccess()) {
                    lblFeedbackLancamento.setText("Presenca e batidas de ponto registradas com sucesso (" + horas + "h)!");
                    lblFeedbackLancamento.getStyleClass().setAll("banner-success");
                    lblFeedbackLancamento.setVisible(true);

                    txtHorasCumpridas.clear();
                    txtHorarioInicio.clear();
                    txtHorarioAlmoco.clear();
                    txtHorarioVolta.clear();
                    txtHorarioSaida.clear();
                    txtAtividades.clear();
                    lblCalculoAuto.setText("Preencha os horários para cálculo automático de horas líquidas ou informe manualmente.");

                    carregarResumoEHistorico(pena.getIdPena());
                } else {
                    lblFeedbackLancamento.setText("Falha ao registrar: " + (resp.getError() != null ? resp.getError().getMessage() : ""));
                    lblFeedbackLancamento.getStyleClass().setAll("banner-error");
                    lblFeedbackLancamento.setVisible(true);
                }
            });
        }).start();
    }

    private void esconderSecoes() {
        boxResumo.setVisible(false);
        boxResumo.setManaged(false);
        boxLancamento.setVisible(false);
        boxLancamento.setManaged(false);
        boxHistorico.setVisible(false);
        boxHistorico.setManaged(false);
    }
}
