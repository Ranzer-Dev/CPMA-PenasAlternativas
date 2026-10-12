package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.ApiResponse;
import br.gov.sp.cpma.desktop.client.CpmaApiClient;
import br.gov.sp.cpma.desktop.client.EstatisticasAuditoriaDTO;
import br.gov.sp.cpma.desktop.client.LogAuditoriaDTO;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class AuditoriaLogsController implements Initializable {

    @FXML private Label lblTotalLogs;
    @FXML private Label lblTotalSucessos;
    @FXML private Label lblTotalFalhas;
    @FXML private Label lblFalhasBiometricas;

    @FXML private TextField txtBuscaLogs;
    @FXML private CheckBox chkApenasFalhas;
    @FXML private ComboBox<Integer> cbLimite;
    @FXML private Button btnAtualizar;

    @FXML private TableView<LogAuditoriaDTO> tblLogs;
    @FXML private TableColumn<LogAuditoriaDTO, String> colDataHora;
    @FXML private TableColumn<LogAuditoriaDTO, String> colStatus;
    @FXML private TableColumn<LogAuditoriaDTO, String> colMetodo;
    @FXML private TableColumn<LogAuditoriaDTO, String> colUsuario;
    @FXML private TableColumn<LogAuditoriaDTO, String> colCodigo;
    @FXML private TableColumn<LogAuditoriaDTO, String> colMensagem;

    @FXML private VBox paneDetalhes;
    @FXML private Label lblDetalheTexto;

    private final CpmaApiClient apiClient = new CpmaApiClient();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        cbLimite.setItems(FXCollections.observableArrayList(50, 100, 200, 500));
        cbLimite.setValue(100);
        cbLimite.valueProperty().addListener((obs, oldVal, newVal) -> carregarDados());

        configurarColunas();

        txtBuscaLogs.textProperty().addListener((obs, oldVal, newVal) -> {
            carregarDados();
        });

        tblLogs.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, selecionado) -> {
            if (selecionado != null) {
                exibirDetalhes(selecionado);
            } else {
                paneDetalhes.setVisible(false);
                paneDetalhes.setManaged(false);
            }
        });

        carregarDados();
    }

    private void configurarColunas() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        colDataHora.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataHora() != null ? c.getValue().getDataHora().format(dtf) : "-"
        ));

        colStatus.setCellValueFactory(c -> {
            LogAuditoriaDTO log = c.getValue();
            if (log.isSucesso()) {
                return new SimpleStringProperty("AUTORIZADO");
            } else if ("CRITICO".equalsIgnoreCase(log.getNivelRisco())) {
                return new SimpleStringProperty("RISCO FACIAL");
            } else {
                return new SimpleStringProperty("BLOQUEADO");
            }
        });

        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("AUTORIZADO".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                    } else if ("RISCO FACIAL".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colMetodo.setCellValueFactory(c -> {
            String m = c.getValue().getMetodo();
            if (m == null) return new SimpleStringProperty("-");
            if (m.contains("2FA_ETAPA2")) return new SimpleStringProperty("2FA: Biometria 1:1");
            if (m.contains("2FA_ETAPA1")) return new SimpleStringProperty("2FA: Código 6 Dígitos");
            if (m.contains("CODIGO")) return new SimpleStringProperty("Código Temporário");
            if (m.contains("FACIAL")) return new SimpleStringProperty("Reconhecimento Facial");
            return new SimpleStringProperty(m);
        });

        colUsuario.setCellValueFactory(c -> {
            LogAuditoriaDTO log = c.getValue();
            if (log.getUsuarioNome() != null && !log.getUsuarioNome().isBlank()) {
                String cpf = log.getUsuarioCpf() != null ? " (" + formatarCpf(log.getUsuarioCpf()) + ")" : "";
                return new SimpleStringProperty(log.getUsuarioNome() + cpf);
            }
            return new SimpleStringProperty("Tentativa Anônima / Código Avulso");
        });

        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getCodigoTentado() != null ? c.getValue().getCodigoTentado() : "-"
        ));

        colMensagem.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getMensagem() != null ? c.getValue().getMensagem() : "-"
        ));
    }

    @FXML
    public void handleAtualizar() {
        carregarDados();
    }

    @FXML
    public void handleFiltroAlterado() {
        carregarDados();
    }

    private void carregarDados() {
        btnAtualizar.setDisable(true);
        String termo = txtBuscaLogs.getText();
        boolean apenasFalhas = chkApenasFalhas.isSelected();
        Integer limite = cbLimite.getValue();

        CompletableFuture.supplyAsync(() -> apiClient.listarLogsAuditoria(limite, termo, apenasFalhas))
                .thenAccept(respLogs -> {
                    Platform.runLater(() -> {
                        btnAtualizar.setDisable(false);
                        if (respLogs.isSuccess() && respLogs.getData() != null) {
                            tblLogs.setItems(FXCollections.observableArrayList(respLogs.getData()));
                        } else {
                            tblLogs.setItems(FXCollections.observableArrayList());
                        }
                    });
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> btnAtualizar.setDisable(false));
                    return null;
                });

        CompletableFuture.supplyAsync(apiClient::obterEstatisticasAuditoria)
                .thenAccept(respStats -> {
                    Platform.runLater(() -> {
                        if (respStats.isSuccess() && respStats.getData() != null) {
                            EstatisticasAuditoriaDTO stats = respStats.getData();
                            lblTotalLogs.setText(String.valueOf(stats.getTotalLogs()));
                            lblTotalSucessos.setText(String.valueOf(stats.getTotalSucessos()));
                            lblTotalFalhas.setText(String.valueOf(stats.getTotalFalhas()));
                            lblFalhasBiometricas.setText(String.valueOf(stats.getFalhasBiometricas()));
                        }
                    });
                });
    }

    private void exibirDetalhes(LogAuditoriaDTO log) {
        paneDetalhes.setVisible(true);
        paneDetalhes.setManaged(true);

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm:ss");
        String dataStr = log.getDataHora() != null ? log.getDataHora().format(dtf) : "-";
        String statusStr = log.isSucesso() ? "AUTORIZADO (Sucesso)" : "BLOQUEADO (" + log.getNivelRisco() + ")";
        String titular = log.getUsuarioNome() != null ? log.getUsuarioNome() : "Não identificado / Código incorreto";
        String cpf = log.getUsuarioCpf() != null ? formatarCpf(log.getUsuarioCpf()) : "-";
        String codigo = log.getCodigoTentado() != null ? log.getCodigoTentado() : "-";
        String mensagem = log.getMensagem() != null ? log.getMensagem() : "-";

        lblDetalheTexto.setText(
                "Data/Hora: " + dataStr + " | Status: " + statusStr + "\n" +
                "Titular Associado: " + titular + " | CPF: " + cpf + " | Código Digitado: " + codigo + "\n" +
                "Método: " + log.getMetodo() + "\n" +
                "Ação / Mensagem do Sistema: " + mensagem
        );
    }

    private String formatarCpf(String cpf) {
        if (cpf == null) return "-";
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-" + digits.substring(9, 11);
        }
        return cpf;
    }
}
