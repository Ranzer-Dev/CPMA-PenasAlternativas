package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.ApiResponse;
import br.gov.sp.cpma.desktop.client.CpmaApiClient;
import br.gov.sp.cpma.desktop.client.PenaDTO;
import br.gov.sp.cpma.desktop.client.RegistroTrabalhoDTO;
import br.gov.sp.cpma.desktop.client.ResumoCumprimentoDTO;
import br.gov.sp.cpma.desktop.client.UsuarioDTO;
import br.gov.sp.cpma.desktop.client.ValidarAcessoDTO;
import br.gov.sp.cpma.desktop.util.FormValidator;
import br.gov.sp.cpma.desktop.util.RelatorioImpressaoService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    @FXML private VBox cardErro;
    @FXML private Label lblErroTitulo;
    @FXML private Label lblErroDetalhe;

    @FXML private VBox cardProntuarioApenado;
    @FXML private Label lblTimestampPresenca;

    @FXML private Label lblProntuarioNome;
    @FXML private Label lblProntuarioCpf;
    @FXML private Label lblProntuarioMatricula;
    @FXML private Label lblProntuarioMedida;
    @FXML private Label lblProntuarioInstituicao;
    @FXML private Label lblProntuarioDataInicio;

    @FXML private Label lblTotemHorasTotais;
    @FXML private Label lblTotemHorasCumpridas;
    @FXML private Label lblTotemHorasRestantes;
    @FXML private Label lblTotemPercentual;

    @FXML private TableView<RegistroTrabalhoDTO> tblRegistrosTotem;
    @FXML private TableColumn<RegistroTrabalhoDTO, String> colRegistroData;
    @FXML private TableColumn<RegistroTrabalhoDTO, String> colRegistroInicio;
    @FXML private TableColumn<RegistroTrabalhoDTO, String> colRegistroSaida;
    @FXML private TableColumn<RegistroTrabalhoDTO, String> colRegistroHoras;
    @FXML private TableColumn<RegistroTrabalhoDTO, String> colRegistroAtividades;

    @FXML private Button btnImprimirComprovante;

    private final CpmaApiClient apiClient = new CpmaApiClient();
    private Long usuarioLogadoId;
    private Long penaLogadaId;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblTerminalInfo.setText("Terminal: " + DEFAULT_TERMINAL_ID);
        cardEntradaCodigo.setVisible(true);
        cardEntradaCodigo.setManaged(true);
        cardErro.setVisible(false);
        cardErro.setManaged(false);
        cardProntuarioApenado.setVisible(false);
        cardProntuarioApenado.setManaged(false);

        configurarColunasTabela();

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

    private void configurarColunasTabela() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colRegistroData.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDataTrabalho() != null ? c.getValue().getDataTrabalho().format(dtf) : "-"
        ));
        colRegistroInicio.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getHorarioInicio() != null ? c.getValue().getHorarioInicio() : "-"
        ));
        colRegistroSaida.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getHorarioSaida() != null ? c.getValue().getHorarioSaida() : "-"
        ));
        colRegistroHoras.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getHorasCumpridas() != null ? String.format("%.1fh", c.getValue().getHorasCumpridas()) : "0.0h"
        ));
        colRegistroAtividades.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAtividades() != null ? c.getValue().getAtividades() : "-"
        ));
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
            exibirErro("CÓDIGO INCOMPLETO", "O código de acesso deve conter exatamente 6 dígitos numéricos.");
            return;
        }

        btnConfirmarCodigo.setDisable(true);

        CompletableFuture.supplyAsync(() -> apiClient.validarCodigoAcesso(codigo, DEFAULT_TERMINAL_ID))
                .thenAccept(this::processarRespostaCodigo)
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btnConfirmarCodigo.setDisable(false);
                        exibirErro("FALHA DE COMUNICAÇÃO", "Não foi possível conectar com o servidor: " + ex.getMessage());
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
                carregarProntuarioCompleto(data.getUsuarioId());
            } else {
                String detalhe = FormValidator.formatarMensagemErro(response.getError(), response.getStatusCode());
                exibirErro("ACESSO NÃO AUTORIZADO", detalhe + "\nVerifique o código ou solicite emissão de novo código no balcão.");
            }
        });
    }

    private void carregarProntuarioCompleto(Long usuarioId) {
        DateTimeFormatter horaFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm:ss");
        lblTimestampPresenca.setText("Registro efetuado em " + LocalDateTime.now().format(horaFmt));

        new Thread(() -> {
            ApiResponse<UsuarioDTO> respUser = apiClient.buscarUsuarioPorId(usuarioId);
            ApiResponse<List<PenaDTO>> respPenas = apiClient.listarPenasPorUsuario(usuarioId);

            PenaDTO pena = null;
            ResumoCumprimentoDTO resumo = null;
            List<RegistroTrabalhoDTO> registros = null;

            if (respPenas.isSuccess() && respPenas.getData() != null && !respPenas.getData().isEmpty()) {
                pena = respPenas.getData().get(0);
                this.penaLogadaId = pena.getIdPena();
                ApiResponse<ResumoCumprimentoDTO> respResumo = apiClient.obterResumoCumprimento(pena.getIdPena());
                if (respResumo.isSuccess()) {
                    resumo = respResumo.getData();
                }
                ApiResponse<List<RegistroTrabalhoDTO>> respRegs = apiClient.listarRegistrosPorPena(pena.getIdPena());
                if (respRegs.isSuccess()) {
                    registros = respRegs.getData();
                }
            }

            final UsuarioDTO finalUser = respUser.isSuccess() ? respUser.getData() : null;
            final PenaDTO finalPena = pena;
            final ResumoCumprimentoDTO finalResumo = resumo;
            final List<RegistroTrabalhoDTO> finalRegs = registros;

            Platform.runLater(() -> {
                cardEntradaCodigo.setVisible(false);
                cardEntradaCodigo.setManaged(false);
                cardErro.setVisible(false);
                cardErro.setManaged(false);
                cardProntuarioApenado.setVisible(true);
                cardProntuarioApenado.setManaged(true);

                if (finalUser != null) {
                    lblProntuarioNome.setText(finalUser.getNome() != null ? finalUser.getNome().toUpperCase() : "-");
                    lblProntuarioCpf.setText(formatarCpf(finalUser.getCpf()));
                    lblProntuarioMatricula.setText(finalUser.getCodigo() != null ? finalUser.getCodigo() : "-");
                }

                if (finalPena != null) {
                    lblProntuarioMedida.setText(finalPena.getTipoPena() != null ? finalPena.getTipoPena() : "Prestação de Serviços à Comunidade (PSC)");
                    lblProntuarioInstituicao.setText(finalPena.getInstituicaoPrincipalNome() != null ? finalPena.getInstituicaoPrincipalNome() : "-");
                    lblProntuarioDataInicio.setText(finalPena.getDataInicio() != null ? finalPena.getDataInicio().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-");
                }

                if (finalResumo != null) {
                    lblTotemHorasTotais.setText(finalResumo.getHorasTotais() != null ? finalResumo.getHorasTotais() + "h" : "-");
                    lblTotemHorasCumpridas.setText(finalResumo.getHorasCumpridas() != null ? String.format("%.1fh", finalResumo.getHorasCumpridas()) : "0.0h");
                    lblTotemHorasRestantes.setText(finalResumo.getHorasRestantes() != null ? String.format("%.1fh", finalResumo.getHorasRestantes()) : "0.0h");
                    lblTotemPercentual.setText(finalResumo.getPercentualConcluido() != null ? String.format("%.1f%%", finalResumo.getPercentualConcluido()) : "0.0%");
                } else if (finalPena != null) {
                    lblTotemHorasTotais.setText(finalPena.getHorasTotais() != null ? finalPena.getHorasTotais() + "h" : "-");
                    lblTotemHorasCumpridas.setText("0.0h");
                    lblTotemHorasRestantes.setText(finalPena.getHorasTotais() != null ? finalPena.getHorasTotais() + "h" : "-");
                    lblTotemPercentual.setText("0.0%");
                }

                if (finalRegs != null && !finalRegs.isEmpty()) {
                    tblRegistrosTotem.setItems(FXCollections.observableArrayList(finalRegs));
                } else {
                    tblRegistrosTotem.setItems(FXCollections.observableArrayList());
                }
            });
        }).start();
    }

    private String formatarCpf(String cpf) {
        if (cpf == null) return "-";
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() == 11) {
            return digits.substring(0, 3) + "." + digits.substring(3, 6) + "." + digits.substring(6, 9) + "-" + digits.substring(9, 11);
        }
        return cpf;
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
        cardErro.setVisible(false);
        cardErro.setManaged(false);
        cardProntuarioApenado.setVisible(false);
        cardProntuarioApenado.setManaged(false);
        btnConfirmarCodigo.setDisable(false);
    }

    private void exibirErro(String titulo, String detalhe) {
        cardEntradaCodigo.setVisible(false);
        cardEntradaCodigo.setManaged(false);
        cardProntuarioApenado.setVisible(false);
        cardProntuarioApenado.setManaged(false);
        cardErro.setVisible(true);
        cardErro.setManaged(true);
        lblErroTitulo.setText(titulo);
        lblErroDetalhe.setText(detalhe);
    }
}
