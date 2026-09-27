package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.*;
import br.gov.sp.cpma.desktop.util.FormValidator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;

public class PenasController {

    @FXML
    private Label lblFeedback;

    @FXML
    private TextField txtCpfApenado;

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
    private TextArea txtAtividades;

    @FXML
    private Button btnSalvarPena;

    private final CpmaApiClient apiClient = new CpmaApiClient();
    private UsuarioDTO apenadoSelecionado;

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
        carregarInstituicoes();

        FormValidator.vincularLimpezaAoInteragir(txtCpfApenado, lblCpfError);
        FormValidator.vincularLimpezaAoInteragir(cbInstituicao, lblInstituicaoError);
        FormValidator.vincularLimpezaAoInteragir(cbTipoPena, lblTipoPenaError);
        FormValidator.vincularLimpezaAoInteragir(txtHorasTotais, lblHorasTotaisError);
        FormValidator.vincularLimpezaAoInteragir(txtHorasSemanais, lblHorasSemanaisError);
        FormValidator.vincularLimpezaAoInteragir(dpDataInicio, lblDataInicioError);
    }

    private void configurarComboBoxInstituicao() {
        cbInstituicao.setConverter(new StringConverter<InstituicaoDTO>() {
            @Override
            public String toString(InstituicaoDTO inst) {
                return inst != null ? inst.getNome() : "";
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
                    cbInstituicao.setItems(FXCollections.observableArrayList(resp.getData()));
                }
            });
        }).start();
    }

    @FXML
    public void handleBuscarApenado() {
        String cpf = txtCpfApenado.getText().trim().replaceAll("\\D", "");
        if (cpf.isEmpty()) {
            FormValidator.marcarErro(txtCpfApenado, lblCpfError, "Informe o CPF para busca");
            return;
        }

        new Thread(() -> {
            ApiResponse<UsuarioDTO> resp = apiClient.buscarUsuarioPorCpf(cpf);
            Platform.runLater(() -> {
                if (resp.isSuccess() && resp.getData() != null) {
                    apenadoSelecionado = resp.getData();
                    lblNomeApenado.setText(apenadoSelecionado.getNome() + " (ID: " + apenadoSelecionado.getIdUsuario() + ")");
                    boxApenadoEncontrado.setVisible(true);
                    boxApenadoEncontrado.setManaged(true);
                    lblCpfError.setText("");
                } else {
                    apenadoSelecionado = null;
                    boxApenadoEncontrado.setVisible(false);
                    boxApenadoEncontrado.setManaged(false);
                    FormValidator.marcarErro(txtCpfApenado, lblCpfError, "Apenado nao encontrado com este CPF");
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
        if (apenadoSelecionado == null) {
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
        dto.setUsuarioId(apenadoSelecionado.getIdUsuario());
        dto.setInstituicaoId(instituicao.getIdInstituicao());
        dto.setTipoPena(tipoPena);
        dto.setHorasTotais(Integer.parseInt(txtHorasTotais.getText().trim()));
        dto.setHorasSemanais(Integer.parseInt(txtHorasSemanais.getText().trim()));
        dto.setDataInicio(dpDataInicio.getValue());
        dto.setDataTermino(dpDataTermino.getValue());
        dto.setAtividadesAcordadas(txtAtividades.getText());

        new Thread(() -> {
            ApiResponse<PenaDTO> resp = apiClient.cadastrarPena(dto);
            Platform.runLater(() -> {
                btnSalvarPena.setDisable(false);
                if (resp.isSuccess() && resp.getData() != null) {
                    lblFeedback.setText("Pena registrada com sucesso para " + apenadoSelecionado.getNome() + "!");
                    lblFeedback.getStyleClass().setAll("banner-success");
                    lblFeedback.setVisible(true);
                    limparFormulario();
                } else {
                    String mensagemErro = FormValidator.formatarMensagemErro(resp.getError(), resp.getStatusCode());
                    lblFeedback.setText(mensagemErro);
                    lblFeedback.getStyleClass().setAll("banner-error");
                    lblFeedback.setVisible(true);
                }
            });
        }).start();
    }

    private void limparFormulario() {
        txtCpfApenado.clear();
        txtHorasTotais.clear();
        txtHorasSemanais.clear();
        txtAtividades.clear();
        cbTipoPena.getSelectionModel().selectFirst();
        boxApenadoEncontrado.setVisible(false);
        boxApenadoEncontrado.setManaged(false);
        apenadoSelecionado = null;
        dpDataInicio.setValue(LocalDate.now());
        dpDataTermino.setValue(null);
        lblEstimativaTexto.setText("");

        FormValidator.limparErro(txtCpfApenado, lblCpfError);
        FormValidator.limparErro(cbInstituicao, lblInstituicaoError);
        FormValidator.limparErro(cbTipoPena, lblTipoPenaError);
        FormValidator.limparErro(txtHorasTotais, lblHorasTotaisError);
        FormValidator.limparErro(txtHorasSemanais, lblHorasSemanaisError);
        FormValidator.limparErro(dpDataInicio, lblDataInicioError);
    }
}
