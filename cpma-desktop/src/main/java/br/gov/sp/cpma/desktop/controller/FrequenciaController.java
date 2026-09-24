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

import java.time.LocalDate;
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
    private TextField txtHorasCumpridas;

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
    private TableColumn<RegistroTrabalhoDTO, String> colHistHoras;

    @FXML
    private TableColumn<RegistroTrabalhoDTO, String> colHistObs;

    private final CpmaApiClient apiClient = new CpmaApiClient();
    private UsuarioDTO apenadoAtual;

    @FXML
    public void initialize() {
        dpDataTrabalho.setValue(LocalDate.now());
        configurarColunasHistorico();
        configurarComboBoxPena();

        cbPenas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                carregarResumoEHistorico(newVal.getIdPena());
            }
        });
    }

    private void configurarColunasHistorico() {
        colHistId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getIdRegistro()));
        colHistData.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataTrabalho() != null ? c.getValue().getDataTrabalho().toString() : "-"));
        colHistHoras.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHorasCumpridas() + "h"));
        colHistObs.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getAtividades() != null ? c.getValue().getAtividades() : "-"));
    }

    private void configurarComboBoxPena() {
        cbPenas.setConverter(new StringConverter<PenaDTO>() {
            @Override
            public String toString(PenaDTO p) {
                if (p == null) return "";
                return "Pena #" + p.getIdPena() + " - " + p.getTipoPena() + " (" + p.getHorasTotais() + "h) - Inst: " + (p.getInstituicaoPrincipalNome() != null ? p.getInstituicaoPrincipalNome() : "");
            }

            @Override
            public PenaDTO fromString(String string) {
                return null;
            }
        });
    }

    @FXML
    public void handleBuscarApenado() {
        String cpf = txtCpfBusca.getText().trim().replaceAll("\\D", "");
        if (cpf.isEmpty()) {
            lblApenadoNome.setText("Informe o CPF para buscar");
            return;
        }

        new Thread(() -> {
            ApiResponse<UsuarioDTO> resp = apiClient.buscarUsuarioPorCpf(cpf);
            Platform.runLater(() -> {
                if (resp.isSuccess() && resp.getData() != null) {
                    apenadoAtual = resp.getData();
                    lblApenadoNome.setText("✓ " + apenadoAtual.getNome() + " (CPF: " + apenadoAtual.getCpf() + ")");
                    carregarPenasApenado(apenadoAtual.getIdUsuario());
                } else {
                    apenadoAtual = null;
                    lblApenadoNome.setText("Apenado nao encontrado");
                    cbPenas.getItems().clear();
                    esconderSecoes();
                }
            });
        }).start();
    }

    private void carregarPenasApenado(Long usuarioId) {
        new Thread(() -> {
            ApiResponse<List<PenaDTO>> resp = apiClient.listarPenasPorUsuario(usuarioId);
            Platform.runLater(() -> {
                if (resp.isSuccess() && resp.getData() != null && !resp.getData().isEmpty()) {
                    cbPenas.setItems(FXCollections.observableArrayList(resp.getData()));
                    cbPenas.getSelectionModel().selectFirst();
                } else {
                    cbPenas.getItems().clear();
                    lblApenadoNome.setText(lblApenadoNome.getText() + " (Sem penas cadastradas)");
                    esconderSecoes();
                }
            });
        }).start();
    }

    private void carregarResumoEHistorico(Long penaId) {
        boxResumo.setVisible(true);
        boxResumo.setManaged(true);
        boxLancamento.setVisible(true);
        boxLancamento.setManaged(true);
        boxHistorico.setVisible(true);
        boxHistorico.setManaged(true);

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
            horas = Double.parseDouble(txtHorasCumpridas.getText().trim());
            if (horas <= 0) {
                lblFeedbackLancamento.setText("Horas devem ser maiores que zero.");
                lblFeedbackLancamento.getStyleClass().setAll("banner-error");
                lblFeedbackLancamento.setVisible(true);
                return;
            }
        } catch (NumberFormatException e) {
            lblFeedbackLancamento.setText("Informe um numero valido de horas.");
            lblFeedbackLancamento.getStyleClass().setAll("banner-error");
            lblFeedbackLancamento.setVisible(true);
            return;
        }

        btnSalvarRegistro.setDisable(true);

        RegistroTrabalhoDTO dto = new RegistroTrabalhoDTO();
        dto.setPenaId(pena.getIdPena());
        dto.setDataTrabalho(dpDataTrabalho.getValue());
        dto.setHorasCumpridas(horas);
        dto.setAtividades(txtAtividades.getText());

        new Thread(() -> {
            ApiResponse<RegistroTrabalhoDTO> resp = apiClient.registrarTrabalho(dto);
            Platform.runLater(() -> {
                btnSalvarRegistro.setDisable(false);
                if (resp.isSuccess()) {
                    lblFeedbackLancamento.setText("Presenca registrada com sucesso (" + horas + "h)!");
                    lblFeedbackLancamento.getStyleClass().setAll("banner-success");
                    lblFeedbackLancamento.setVisible(true);
                    txtHorasCumpridas.clear();
                    txtAtividades.clear();
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
