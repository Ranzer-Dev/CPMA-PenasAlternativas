package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.ApiResponse;
import br.gov.sp.cpma.desktop.client.CpmaApiClient;
import br.gov.sp.cpma.desktop.client.PenaDTO;
import br.gov.sp.cpma.desktop.client.RegistroTrabalhoDTO;
import br.gov.sp.cpma.desktop.client.ResumoCumprimentoDTO;
import br.gov.sp.cpma.desktop.client.UsuarioDTO;
import br.gov.sp.cpma.desktop.client.ValidarAcessoDTO;
import br.gov.sp.cpma.desktop.client.ValidarCodigoEtapa1DTO;
import br.gov.sp.cpma.desktop.util.FormValidator;
import br.gov.sp.cpma.desktop.util.MjpegStreamReader;
import br.gov.sp.cpma.desktop.util.RelatorioImpressaoService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
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

    @FXML private VBox cardEtapa2Biometria;
    @FXML private Label lblCodigoBadgeEtapa2;
    @FXML private Label lblApenadoIdentificadoNome;
    @FXML private ImageView imgTotemCameraLive;
    @FXML private VBox paneCameraCarregando;
    @FXML private Label lblStatusCameraTotem;
    @FXML private Label lblBadgeCameraTotem;
    @FXML private Label lblFeedbackEtapa2;
    @FXML private Button btnCapturarEValidarFacial;
    @FXML private Button btnUploadFotoTeste;

    @FXML private VBox cardErro;
    @FXML private Label lblErroTitulo;
    @FXML private Label lblErroDetalhe;

    @FXML private VBox cardProntuarioApenado;
    @FXML private Label lblTimestampPresenca;
    @FXML private ImageView imgProntuarioFoto;

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

    @FXML private Label lblTimeoutContador;
    @FXML private ProgressBar progressoTimeout;
    @FXML private Button btnImprimirComprovante;

    private final CpmaApiClient apiClient = new CpmaApiClient();
    private String codigoValidadoEtapa1;
    private String nomeUsuarioEtapa1;
    private Long idUsuarioEtapa1;
    private MjpegStreamReader streamReader;
    private Thread streamThread;

    private Long usuarioLogadoId;
    private Long penaLogadaId;
    private Timeline timelineTimeout;
    private int segundosRestantes = 30;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        lblTerminalInfo.setText("Terminal: " + DEFAULT_TERMINAL_ID);
        cardEntradaCodigo.setVisible(true);
        cardEntradaCodigo.setManaged(true);
        cardEtapa2Biometria.setVisible(false);
        cardEtapa2Biometria.setManaged(false);
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

        CompletableFuture.supplyAsync(() -> apiClient.validarCodigoEtapa1(codigo, DEFAULT_TERMINAL_ID))
                .thenAccept(this::processarRespostaEtapa1)
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btnConfirmarCodigo.setDisable(false);
                        exibirErro("FALHA DE COMUNICAÇÃO", "Não foi possível conectar com o servidor: " + ex.getMessage());
                    });
                    return null;
                });
    }

    private void processarRespostaEtapa1(ApiResponse<ValidarCodigoEtapa1DTO> response) {
        Platform.runLater(() -> {
            btnConfirmarCodigo.setDisable(false);

            if (response.isSuccess() && response.getData() != null && response.getData().isSucesso()) {
                ValidarCodigoEtapa1DTO data = response.getData();
                this.codigoValidadoEtapa1 = data.getCodigo();
                this.nomeUsuarioEtapa1 = data.getUsuarioNome();
                this.idUsuarioEtapa1 = data.getUsuarioId();

                lblCodigoBadgeEtapa2.setText("CÓDIGO: " + codigoValidadoEtapa1);
                lblApenadoIdentificadoNome.setText(nomeUsuarioEtapa1);

                cardEntradaCodigo.setVisible(false);
                cardEntradaCodigo.setManaged(false);
                cardErro.setVisible(false);
                cardErro.setManaged(false);
                cardProntuarioApenado.setVisible(false);
                cardProntuarioApenado.setManaged(false);

                lblFeedbackEtapa2.setVisible(false);
                lblFeedbackEtapa2.setManaged(false);
                btnCapturarEValidarFacial.setDisable(false);
                btnCapturarEValidarFacial.setText("📸 Confirmar Identidade Facial");

                cardEtapa2Biometria.setVisible(true);
                cardEtapa2Biometria.setManaged(true);

                iniciarStreamTotem();
            } else {
                String detalhe = FormValidator.formatarMensagemErro(response.getError(), response.getStatusCode());
                exibirErro("ACESSO NÃO AUTORIZADO", detalhe + "\nVerifique o código ou solicite nova emissão ao atendente no balcão.");
            }
        });
    }

    private void iniciarStreamTotem() {
        pararStreamTotem();
        paneCameraCarregando.setVisible(true);
        paneCameraCarregando.setManaged(true);
        lblStatusCameraTotem.setText("Iniciando câmera do totem...");
        lblBadgeCameraTotem.setText("● Conectando...");
        lblBadgeCameraTotem.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-background-color: #fef3c7; -fx-text-fill: #d97706; -fx-border-color: #fde68a; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 3px 12px;");

        String streamUrl = apiClient.getFacialServiceUrl() + "/camera/stream";
        this.streamReader = new MjpegStreamReader(
                streamUrl,
                this::processarFrameCamera,
                this::processarErroCamera
        );

        this.streamThread = new Thread(streamReader, "TotemCameraStreamThread");
        this.streamThread.setDaemon(true);
        this.streamThread.start();
    }

    private void processarFrameCamera(Image frame) {
        if (paneCameraCarregando.isVisible()) {
            paneCameraCarregando.setVisible(false);
            paneCameraCarregando.setManaged(false);
            lblBadgeCameraTotem.setText("● Câmera Ativa | Rosto Rastreado");
            lblBadgeCameraTotem.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-background-color: #ecfdf5; -fx-text-fill: #059669; -fx-border-color: #a7f3d0; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 3px 12px;");
        }
        imgTotemCameraLive.setImage(frame);
    }

    private void processarErroCamera(Throwable erro) {
        paneCameraCarregando.setVisible(true);
        paneCameraCarregando.setManaged(true);
        lblStatusCameraTotem.setText("Sensor de câmera não detectado.");
        lblBadgeCameraTotem.setText("● Câmera Indisponível");
        lblBadgeCameraTotem.setStyle("-fx-font-size: 11px; -fx-font-weight: 700; -fx-background-color: #fee2e2; -fx-text-fill: #dc2626; -fx-border-color: #fca5a5; -fx-border-radius: 12px; -fx-background-radius: 12px; -fx-padding: 3px 12px;");
    }

    private void pararStreamTotem() {
        if (streamReader != null) {
            streamReader.stop();
            streamReader = null;
        }
        if (streamThread != null) {
            streamThread.interrupt();
            streamThread = null;
        }
        if (imgTotemCameraLive != null) {
            imgTotemCameraLive.setImage(null);
        }
    }

    @FXML
    public void handleValidarBiometriaFacial() {
        if (codigoValidadoEtapa1 == null || codigoValidadoEtapa1.isBlank()) {
            exibirErro("FLUXO INTERROMPIDO", "O código da etapa 1 não foi localizado.");
            return;
        }

        btnCapturarEValidarFacial.setDisable(true);
        btnCapturarEValidarFacial.setText("Capturando e Verificando Identidade...");
        lblFeedbackEtapa2.setVisible(false);
        lblFeedbackEtapa2.setManaged(false);

        CompletableFuture.supplyAsync(apiClient::capturarFotoWebcam)
                .thenAccept(respCam -> {
                    if (respCam.isSuccess() && respCam.getData() != null && respCam.getData().getRawBase64() != null) {
                        submeterValidacaoBiometrica(respCam.getData().getRawBase64());
                    } else {
                        Platform.runLater(() -> {
                            btnCapturarEValidarFacial.setDisable(false);
                            btnCapturarEValidarFacial.setText("📸 Confirmar Identidade Facial");
                            lblFeedbackEtapa2.setText("Não foi possível capturar imagem da câmera. Utilize a opção 'Enviar Foto'.");
                            lblFeedbackEtapa2.setVisible(true);
                            lblFeedbackEtapa2.setManaged(true);
                        });
                    }
                })
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btnCapturarEValidarFacial.setDisable(false);
                        btnCapturarEValidarFacial.setText("📸 Confirmar Identidade Facial");
                        lblFeedbackEtapa2.setText("Falha na câmera: " + ex.getMessage());
                        lblFeedbackEtapa2.setVisible(true);
                        lblFeedbackEtapa2.setManaged(true);
                    });
                    return null;
                });
    }

    @FXML
    public void handleSelecionarFotoTeste() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Reconhecimento Facial 2FA - Selecionar Foto de Identificação");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens (JPG, PNG)", "*.jpg", "*.jpeg", "*.png")
        );

        Stage stage = (Stage) btnConfirmarCodigo.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);

        if (file != null) {
            try {
                byte[] bytes = Files.readAllBytes(file.toPath());
                String base64 = Base64.getEncoder().encodeToString(bytes);
                btnCapturarEValidarFacial.setDisable(true);
                btnCapturarEValidarFacial.setText("Processando Biometria Facial...");
                lblFeedbackEtapa2.setVisible(false);
                lblFeedbackEtapa2.setManaged(false);

                submeterValidacaoBiometrica(base64);
            } catch (Exception e) {
                lblFeedbackEtapa2.setText("Falha ao ler arquivo: " + e.getMessage());
                lblFeedbackEtapa2.setVisible(true);
                lblFeedbackEtapa2.setManaged(true);
            }
        }
    }

    private void submeterValidacaoBiometrica(String fotoBase64) {
        CompletableFuture.supplyAsync(() -> apiClient.validarBiometriaEtapa2(codigoValidadoEtapa1, fotoBase64, DEFAULT_TERMINAL_ID, 0.65))
                .thenAccept(this::processarRespostaBiometria)
                .exceptionally(ex -> {
                    Platform.runLater(() -> {
                        btnCapturarEValidarFacial.setDisable(false);
                        btnCapturarEValidarFacial.setText("📸 Confirmar Identidade Facial");
                        lblFeedbackEtapa2.setText("Erro de comunicação com o serviço: " + ex.getMessage());
                        lblFeedbackEtapa2.setVisible(true);
                        lblFeedbackEtapa2.setManaged(true);
                    });
                    return null;
                });
    }

    private void processarRespostaBiometria(ApiResponse<ValidarAcessoDTO> response) {
        Platform.runLater(() -> {
            btnCapturarEValidarFacial.setDisable(false);
            btnCapturarEValidarFacial.setText("📸 Confirmar Identidade Facial");

            if (response.isSuccess() && response.getData() != null && response.getData().isSucesso()) {
                pararStreamTotem();
                cardEtapa2Biometria.setVisible(false);
                cardEtapa2Biometria.setManaged(false);

                ValidarAcessoDTO data = response.getData();
                this.usuarioLogadoId = data.getUsuarioId();
                carregarProntuarioCompleto(data.getUsuarioId());
            } else {
                String detalhe = FormValidator.formatarMensagemErro(response.getError(), response.getStatusCode());
                lblFeedbackEtapa2.setText("❌ " + detalhe + "\nO rosto apresentado não corresponde ao apenado titular (" + nomeUsuarioEtapa1 + ").");
                lblFeedbackEtapa2.setVisible(true);
                lblFeedbackEtapa2.setManaged(true);
            }
        });
    }

    @FXML
    public void handleVoltarParaCodigo() {
        pararStreamTotem();
        cardEtapa2Biometria.setVisible(false);
        cardEtapa2Biometria.setManaged(false);
        cardEntradaCodigo.setVisible(true);
        cardEntradaCodigo.setManaged(true);
        txtCodigoAcesso.clear();
        btnConfirmarCodigo.setDisable(false);
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
                cardEtapa2Biometria.setVisible(false);
                cardEtapa2Biometria.setManaged(false);
                cardErro.setVisible(false);
                cardErro.setManaged(false);
                cardProntuarioApenado.setVisible(true);
                cardProntuarioApenado.setManaged(true);

                preencherDadosProntuario(finalUser, finalPena, finalResumo, finalRegs);
                iniciarContadorRegressivo();
            });
        }).start();
    }

    private void preencherDadosProntuario(UsuarioDTO usuario, PenaDTO pena, ResumoCumprimentoDTO resumo, List<RegistroTrabalhoDTO> registros) {
        if (usuario != null) {
            lblProntuarioNome.setText(usuario.getNome());
            lblProntuarioCpf.setText(formatarCpf(usuario.getCpf()));
            lblProntuarioMatricula.setText(usuario.getCodigo() != null ? usuario.getCodigo() : "PEN-" + usuario.getIdUsuario());

            if (usuario.getFoto() != null && !usuario.getFoto().isBlank()) {
                try {
                    byte[] bytes = Base64.getDecoder().decode(usuario.getFoto());
                    imgProntuarioFoto.setImage(new Image(new ByteArrayInputStream(bytes)));
                } catch (Exception e) {
                    imgProntuarioFoto.setImage(null);
                }
            } else {
                imgProntuarioFoto.setImage(null);
            }
        }

        if (pena != null) {
            lblProntuarioMedida.setText("Prestação de Serviços à Comunidade (PSC)");
            lblProntuarioInstituicao.setText(pena.getInstituicaoPrincipalNome() != null ? pena.getInstituicaoPrincipalNome() : "Instituição Cadastrada");
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            lblProntuarioDataInicio.setText(pena.getDataInicio() != null ? pena.getDataInicio().format(dtf) : "-");
        }

        if (resumo != null) {
            lblTotemHorasTotais.setText(String.format("%dh", resumo.getHorasTotais() != null ? resumo.getHorasTotais() : 0));
            lblTotemHorasCumpridas.setText(String.format("%.1fh", resumo.getHorasCumpridas() != null ? resumo.getHorasCumpridas() : 0.0));
            lblTotemHorasRestantes.setText(String.format("%.1fh", resumo.getHorasRestantes() != null ? resumo.getHorasRestantes() : 0.0));
            lblTotemPercentual.setText(String.format("%.1f%%", resumo.getPercentualConcluido() != null ? resumo.getPercentualConcluido() : 0.0));
        } else if (pena != null) {
            lblTotemHorasTotais.setText(String.format("%dh", pena.getHorasTotais() != null ? pena.getHorasTotais() : 0));
            lblTotemHorasCumpridas.setText("0.0h");
            lblTotemHorasRestantes.setText(String.format("%dh", pena.getHorasTotais() != null ? pena.getHorasTotais() : 0));
            lblTotemPercentual.setText("0.0%");
        }

        if (registros != null) {
            tblRegistrosTotem.setItems(FXCollections.observableArrayList(registros));
        } else {
            tblRegistrosTotem.setItems(FXCollections.observableArrayList());
        }
    }

    private void iniciarContadorRegressivo() {
        pararContadorRegressivo();
        segundosRestantes = 30;
        lblTimeoutContador.setText(segundosRestantes + "s");
        progressoTimeout.setProgress(1.0);

        timelineTimeout = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            segundosRestantes--;
            lblTimeoutContador.setText(segundosRestantes + "s");
            progressoTimeout.setProgress((double) segundosRestantes / 30.0);

            if (segundosRestantes <= 0) {
                handleNovoAcesso();
            }
        }));
        timelineTimeout.setCycleCount(30);
        timelineTimeout.play();
    }

    private void pararContadorRegressivo() {
        if (timelineTimeout != null) {
            timelineTimeout.stop();
            timelineTimeout = null;
        }
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
        pararStreamTotem();
        pararContadorRegressivo();
        if (imgProntuarioFoto != null) {
            imgProntuarioFoto.setImage(null);
        }
        usuarioLogadoId = null;
        penaLogadaId = null;
        codigoValidadoEtapa1 = null;
        nomeUsuarioEtapa1 = null;
        idUsuarioEtapa1 = null;
        txtCodigoAcesso.clear();
        cardEntradaCodigo.setVisible(true);
        cardEntradaCodigo.setManaged(true);
        cardEtapa2Biometria.setVisible(false);
        cardEtapa2Biometria.setManaged(false);
        cardErro.setVisible(false);
        cardErro.setManaged(false);
        cardProntuarioApenado.setVisible(false);
        cardProntuarioApenado.setManaged(false);
        btnConfirmarCodigo.setDisable(false);
    }

    private void exibirErro(String titulo, String detalhe) {
        pararStreamTotem();
        pararContadorRegressivo();
        cardEntradaCodigo.setVisible(false);
        cardEntradaCodigo.setManaged(false);
        cardEtapa2Biometria.setVisible(false);
        cardEtapa2Biometria.setManaged(false);
        cardProntuarioApenado.setVisible(false);
        cardProntuarioApenado.setManaged(false);
        cardErro.setVisible(true);
        cardErro.setManaged(true);
        lblErroTitulo.setText(titulo);
        lblErroDetalhe.setText(detalhe);
    }
}
