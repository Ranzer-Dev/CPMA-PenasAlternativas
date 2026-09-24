package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.*;
import br.gov.sp.cpma.desktop.util.RelatorioImpressaoService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;

public class TotemKioskController implements Initializable {

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
    }

    @FXML
    public void handleValidarCodigo() {
        String codigo = txtCodigoAcesso.getText().trim();
        if (codigo.isEmpty()) {
            exibirResultado(false, "CODIGO OBRIGATORIO", "Por favor, digite o codigo numerico de 6 digitos.");
            return;
        }

        btnConfirmarCodigo.setDisable(true);

        CompletableFuture.supplyAsync(() -> apiClient.validarCodigoAcesso(codigo, "TOTEM_EXTERNO_01"))
                .thenAccept(this::processarRespostaCodigo)
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btnConfirmarCodigo.setDisable(false);
                        exibirResultado(false, "FALHA DE REDE", "Nao foi possivel conectar com o servidor: " + ex.getMessage());
                    });
                    return null;
                });
    }

    private void processarRespostaCodigo(ApiResponse<ValidarAcessoDTO> response) {
        Platform.runLater(() -> {
            btnConfirmarCodigo.setDisable(false);

            if (response.isSuccess() && response.getData().isSucesso()) {
                ValidarAcessoDTO data = response.getData();
                this.usuarioLogadoId = data.getUsuarioId();
                exibirResultado(true, "ACESSO CONFIRMADO", "Bem-vindo(a), " + data.getUsuarioNome() + "!\nMatricula: " + (data.getUsuarioCodigo() != null ? data.getUsuarioCodigo() : "-"));
                txtCodigoAcesso.clear();
                carregarResumoPenaTotem(data.getUsuarioId());
            } else if (response.getError() != null) {
                exibirResultado(false, "ACESSO NEGADO", response.getError().getMessage());
            } else {
                exibirResultado(false, "ACESSO NEGADO", "Codigo invalido ou expirado.");
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
            cardResultado.setStyle("-fx-background-color: rgba(34, 197, 94, 0.15); -fx-border-color: #22c55e; -fx-border-radius: 8px; -fx-padding: 16px;");
            lblResultadoTitulo.setStyle("-fx-text-fill: #16a34a; -fx-font-size: 16px; -fx-font-weight: 800;");
        } else {
            cardResultado.setStyle("-fx-background-color: rgba(239, 68, 68, 0.15); -fx-border-color: #ef4444; -fx-border-radius: 8px; -fx-padding: 16px;");
            lblResultadoTitulo.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 16px; -fx-font-weight: 800;");
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
