package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.ApiResponse;
import br.gov.sp.cpma.desktop.client.CpmaApiClient;
import br.gov.sp.cpma.desktop.client.PenaDTO;
import br.gov.sp.cpma.desktop.client.ResumoCumprimentoDTO;
import br.gov.sp.cpma.desktop.client.UsuarioDTO;
import br.gov.sp.cpma.desktop.client.ValidarAcessoDTO;
import br.gov.sp.cpma.desktop.util.FormValidator;
import br.gov.sp.cpma.desktop.util.RelatorioImpressaoService;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class TotemKioskController implements Initializable {

    private static final String DEFAULT_TERMINAL_ID = "TOTEM_EXTERNO_01";

    @FXML private Label lblTerminalInfo;
    @FXML private Label lblStatusToken;

    @FXML private VBox cardEntradaCodigo;
    @FXML private TextField txtCodigoAcesso;
    @FXML private Button btnConfirmarCodigo;

    @FXML private VBox cardResultado;
    @FXML private Label lblResultadoTitulo;
    @FXML private Label lblResultadoDetalhe;

    @FXML private VBox boxResumoTotem;
    @FXML private Label lblTotemHorasCumpridas;
    @FXML private Label lblTotemHorasRestantes;
    @FXML private Label lblTotemHorasTotais;
    @FXML private Label lblTotemPercentual;
    @FXML private HBox boxAcoesTotem;
    @FXML private Button btnImprimirComprovante;

    private final CpmaApiClient apiClient = new CpmaApiClient();
    private Long usuarioLogadoId;
    private Long penaLogadaId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblTerminalInfo.setText("Terminal: " + DEFAULT_TERMINAL_ID);
        cardResultado.setVisible(false);
        cardResultado.setManaged(false);

        if (boxResumoTotem != null) {
            boxResumoTotem.setVisible(false);
            boxResumoTotem.setManaged(false);
        }
        if (boxAcoesTotem != null) {
            boxAcoesTotem.setVisible(false);
            boxAcoesTotem.setManaged(false);
        }

        txtCodigoAcesso.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null) return;
            String limpo = newVal.replaceAll("\\D", "");
            if (limpo.length() > 6) {
                limpo = limpo.substring(0, 6);
            }
            if (!newVal.equals(limpo)) {
                txtCodigoAcesso.setText(limpo);
            }
            if (limpo.length() == 6 && (oldVal == null || oldVal.length() < 6)) {
                Platform.runLater(this::handleValidarCodigo);
            }
        });
    }

    @FXML
    public void handleTecladoDigito(ActionEvent event) {
        Button btn = (Button) event.getSource();
        String digito = btn.getText();
        String atual = txtCodigoAcesso.getText() != null ? txtCodigoAcesso.getText() : "";
        if (atual.length() < 6) {
            txtCodigoAcesso.setText(atual + digito);
        }
    }

    @FXML
    public void handleTecladoLimpar() {
        txtCodigoAcesso.clear();
    }

    @FXML
    public void handleTecladoApagar() {
        String atual = txtCodigoAcesso.getText() != null ? txtCodigoAcesso.getText() : "";
        if (!atual.isEmpty()) {
            txtCodigoAcesso.setText(atual.substring(0, atual.length() - 1));
        }
    }

    @FXML
    public void handleValidarCodigo() {
        String codigo = txtCodigoAcesso.getText().trim();
        if (codigo.length() != 6) {
            exibirResultado(false, "CÓDIGO INCOMPLETO", "O código de acesso deve conter exatamente 6 dígitos numéricos.");
            return;
        }

        btnConfirmarCodigo.setDisable(true);

        CompletableFuture.supplyAsync(() -> apiClient.validarCodigoAcesso(codigo, DEFAULT_TERMINAL_ID))
                .thenAccept(this::processarRespostaCodigo)
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btnConfirmarCodigo.setDisable(false);
                        exibirResultado(false, "FALHA DE COMUNICAÇÃO", "Não foi possível conectar com o servidor: " + ex.getMessage());
                    });
                    return null;
                });
    }

    private void processarRespostaCodigo(ApiResponse<ValidarAcessoDTO> response) {
        Platform.runLater(() -> {
            btnConfirmarCodigo.setDisable(false);

            if (response.isSuccess() && response.getData() != null && response.getData().isSucesso()) {
                ValidarAcessoDTO data = response.getData();
                this.usuarioLogadoId = data.getUsuarioId();
                cardEntradaCodigo.setVisible(false);
                cardEntradaCodigo.setManaged(false);

                exibirResultado(true, "PRESENÇA REGISTRADA COM SUCESSO!",
                        "Apenado(a): " + data.getUsuarioNome()
                                + "\nMatrícula: " + (data.getUsuarioCodigo() != null ? data.getUsuarioCodigo() : "-")
                                + "\nAtendimento validado oficialmente no sistema.");
                carregarResumoPenaTotem(data.getUsuarioId());
            } else {
                String detalhe = FormValidator.formatarMensagemErro(response.getError(), response.getStatusCode());
                exibirResultado(false, "ACESSO NÃO AUTORIZADO", detalhe + "\nVerifique o código ou solicite emissão de novo código no balcão.");
            }
        });
    }

    private void carregarResumoPenaTotem(Long usuarioId) {
        new Thread(() -> {
            ApiResponse<List<PenaDTO>> respPenas = apiClient.listarPenasPorUsuario(usuarioId);
            if (respPenas.isSuccess() && respPenas.getData() != null && !respPenas.getData().isEmpty()) {
                PenaDTO pena = respPenas.getData().get(0);
                this.penaLogadaId = pena.getIdPena();
                ApiResponse<ResumoCumprimentoDTO> respResumo = apiClient.obterResumoCumprimento(pena.getIdPena());

                Platform.runLater(() -> {
                    if (respResumo.isSuccess() && respResumo.getData() != null) {
                        ResumoCumprimentoDTO r = respResumo.getData();
                        lblTotemHorasCumpridas.setText("Cumpridas: " + String.format("%.1fh", r.getHorasCumpridas()));
                        lblTotemHorasRestantes.setText("Restantes: " + String.format("%.1fh", r.getHorasRestantes()));
                        lblTotemHorasTotais.setText("Total Pena: " + r.getHorasTotais() + "h");
                        lblTotemPercentual.setText("Progresso: " + String.format("%.1f%%", r.getPercentualConcluido()));
                    } else {
                        lblTotemHorasCumpridas.setText("Cumpridas: 0h");
                        lblTotemHorasRestantes.setText("Restantes: " + pena.getHorasTotais() + "h");
                        lblTotemHorasTotais.setText("Total Pena: " + pena.getHorasTotais() + "h");
                        lblTotemPercentual.setText("Progresso: 0%");
                    }
                    if (boxResumoTotem != null) {
                        boxResumoTotem.setVisible(true);
                        boxResumoTotem.setManaged(true);
                    }
                    if (boxAcoesTotem != null) {
                        boxAcoesTotem.setVisible(true);
                        boxAcoesTotem.setManaged(true);
                    }
                });
            } else {
                Platform.runLater(() -> {
                    if (boxAcoesTotem != null) {
                        boxAcoesTotem.setVisible(true);
                        boxAcoesTotem.setManaged(true);
                    }
                });
            }
        }).start();
    }

    @FXML
    public void handleImprimirComprovante() {
        if (usuarioLogadoId == null) {
            return;
        }

        new Thread(() -> {
            ApiResponse<UsuarioDTO> respUser = apiClient.buscarUsuarioPorId(usuarioLogadoId);
            if (respUser.isSuccess() && respUser.getData() != null) {
                Platform.runLater(() -> {
                    RelatorioImpressaoService.imprimirFichaCompleta(
                            btnConfirmarCodigo.getScene().getWindow(),
                            apiClient,
                            respUser.getData(),
                            penaLogadaId
                    );
                });
            }
        }).start();
    }

    @FXML
    public void handleNovoAcesso() {
        usuarioLogadoId = null;
        penaLogadaId = null;
        txtCodigoAcesso.clear();
        cardEntradaCodigo.setVisible(true);
        cardEntradaCodigo.setManaged(true);
        cardResultado.setVisible(false);
        cardResultado.setManaged(false);

        if (boxResumoTotem != null) {
            boxResumoTotem.setVisible(false);
            boxResumoTotem.setManaged(false);
        }
        if (boxAcoesTotem != null) {
            boxAcoesTotem.setVisible(false);
            boxAcoesTotem.setManaged(false);
        }
        btnConfirmarCodigo.setDisable(false);
    }

    private void exibirResultado(boolean sucesso, String titulo, String detalhe) {
        cardResultado.setVisible(true);
        cardResultado.setManaged(true);
        lblResultadoTitulo.setText(titulo);
        lblResultadoDetalhe.setText(detalhe);

        if (sucesso) {
            cardResultado.setStyle("-fx-background-color: rgba(34, 197, 94, 0.12); -fx-border-color: #22c55e; -fx-border-radius: 10px; -fx-padding: 20px;");
            lblResultadoTitulo.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 18px; -fx-font-weight: 900;");
        } else {
            cardResultado.setStyle("-fx-background-color: rgba(239, 68, 68, 0.12); -fx-border-color: #ef4444; -fx-border-radius: 10px; -fx-padding: 20px;");
            lblResultadoTitulo.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 18px; -fx-font-weight: 900;");
            if (boxResumoTotem != null) {
                boxResumoTotem.setVisible(false);
                boxResumoTotem.setManaged(false);
            }
            if (boxAcoesTotem != null) {
                boxAcoesTotem.setVisible(false);
                boxAcoesTotem.setManaged(false);
            }
        }
    }
}
