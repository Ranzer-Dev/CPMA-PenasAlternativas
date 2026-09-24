package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.*;
import br.gov.sp.cpma.desktop.util.RelatorioImpressaoService;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;

public class ApenadosController {

    @FXML
    private TextField txtFiltro;

    @FXML
    private Button btnGerarCodigoTotem;

    @FXML
    private TableView<UsuarioDTO> tblApenados;

    @FXML
    private TableColumn<UsuarioDTO, Number> colId;

    @FXML
    private TableColumn<UsuarioDTO, String> colCodigo;

    @FXML
    private TableColumn<UsuarioDTO, String> colNome;

    @FXML
    private TableColumn<UsuarioDTO, String> colCpf;

    @FXML
    private TableColumn<UsuarioDTO, String> colTelefone;

    @FXML
    private TableColumn<UsuarioDTO, String> colBairro;

    @FXML
    private TableColumn<UsuarioDTO, String> colCidade;

    @FXML
    private VBox paneDetalhes;

    @FXML
    private Label lblDetalheNome;

    @FXML
    private Label lblDetalheCpf;

    @FXML
    private Label lblDetalheNascimento;

    @FXML
    private Label lblDetalheEndereco;

    @FXML
    private Label lblDetalheTelefone;

    @FXML
    private TableView<PenaDTO> tblPenasApenado;

    @FXML
    private TableColumn<PenaDTO, Number> colPenaId;

    @FXML
    private TableColumn<PenaDTO, String> colPenaTipo;

    @FXML
    private TableColumn<PenaDTO, String> colPenaInstituicao;

    @FXML
    private TableColumn<PenaDTO, String> colPenaHoras;

    @FXML
    private TableColumn<PenaDTO, String> colPenaInicio;

    @FXML
    private TableColumn<PenaDTO, String> colPenaTermino;

    private final CpmaApiClient apiClient = new CpmaApiClient();
    private final ObservableList<UsuarioDTO> masterData = FXCollections.observableArrayList();
    private FilteredList<UsuarioDTO> filteredData;
    private UsuarioDTO apenadoSelecionado;

    @FXML
    public void initialize() {
        configurarColunas();

        filteredData = new FilteredList<>(masterData, p -> true);
        tblApenados.setItems(filteredData);

        txtFiltro.textProperty().addListener((observable, oldValue, newValue) -> {
            filtrar(newValue);
        });

        tblApenados.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                apenadoSelecionado = newSel;
                exibirDetalhes(newSel);
            }
        });

        carregarApenados();
    }

    private void configurarColunas() {
        colId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getIdUsuario()));
        colCodigo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodigo() != null ? c.getValue().getCodigo() : "-"));
        colNome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));
        colCpf.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCpf()));
        colTelefone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefone() != null ? c.getValue().getTelefone() : "-"));
        colBairro.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBairro() != null ? c.getValue().getBairro() : "-"));
        colCidade.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCidade() != null ? c.getValue().getCidade() : "-"));

        colPenaId.setCellValueFactory(c -> new SimpleLongProperty(c.getValue().getIdPena()));
        colPenaTipo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTipoPena()));
        colPenaInstituicao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInstituicaoPrincipalNome() != null ? c.getValue().getInstituicaoPrincipalNome() : "-"));
        colPenaHoras.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getHorasTotais()) + "h"));
        colPenaInicio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataInicio() != null ? c.getValue().getDataInicio().toString() : "-"));
        colPenaTermino.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataTermino() != null ? c.getValue().getDataTermino().toString() : "-"));
    }

    private void carregarApenados() {
        new Thread(() -> {
            ApiResponse<List<UsuarioDTO>> response = apiClient.listarUsuarios();
            Platform.runLater(() -> {
                if (response.isSuccess() && response.getData() != null) {
                    masterData.setAll(response.getData());
                }
            });
        }).start();
    }

    private void filtrar(String termo) {
        if (termo == null || termo.isBlank()) {
            filteredData.setPredicate(u -> true);
            return;
        }
        String lower = termo.toLowerCase().trim();
        String num = termo.replaceAll("\\D", "");

        filteredData.setPredicate(u -> {
            boolean matchNome = u.getNome() != null && u.getNome().toLowerCase().contains(lower);
            boolean matchCpf = u.getCpf() != null && (u.getCpf().contains(lower) || (!num.isEmpty() && u.getCpf().replaceAll("\\D", "").contains(num)));
            return matchNome || matchCpf;
        });
    }

    private void exibirDetalhes(UsuarioDTO u) {
        this.apenadoSelecionado = u;
        lblDetalheNome.setText(u.getNome());
        lblDetalheCpf.setText(u.getCpf());
        lblDetalheNascimento.setText(u.getDataNascimento() != null ? u.getDataNascimento().toString() : "Nao informada");
        lblDetalheEndereco.setText((u.getEndereco() != null ? u.getEndereco() : "") + " " + (u.getBairro() != null ? "- " + u.getBairro() : ""));
        lblDetalheTelefone.setText(u.getTelefone() != null ? u.getTelefone() : "Nao informado");

        paneDetalhes.setVisible(true);
        paneDetalhes.setManaged(true);

        new Thread(() -> {
            ApiResponse<List<PenaDTO>> respPenas = apiClient.listarPenasPorUsuario(u.getIdUsuario());
            Platform.runLater(() -> {
                if (respPenas.isSuccess() && respPenas.getData() != null) {
                    tblPenasApenado.setItems(FXCollections.observableArrayList(respPenas.getData()));
                } else {
                    tblPenasApenado.getItems().clear();
                }
            });
        }).start();
    }

    @FXML
    public void handleLimparFiltro() {
        txtFiltro.clear();
    }

    @FXML
    public void handleRecarregar() {
        carregarApenados();
    }

    @FXML
    public void handleNovoApenado() {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/br/gov/sp/cpma/desktop/view/cadastroApenadoView.fxml"));
            Stage dialog = new Stage();
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setTitle("CPMA - Cadastrar Novo Apenado");
            dialog.setScene(new Scene(root, 750, 680));
            dialog.showAndWait();
            carregarApenados();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleImprimirDadosTrabalho() {
        UsuarioDTO selecionado = apenadoSelecionado != null ? apenadoSelecionado : tblApenados.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atencao");
            alert.setHeaderText("Nenhum Apenado Selecionado");
            alert.setContentText("Selecione um apenado na lista para imprimir os dados e registros de trabalho.");
            alert.showAndWait();
            return;
        }

        PenaDTO penaSelecionada = tblPenasApenado.getSelectionModel().getSelectedItem();
        Long penaId = penaSelecionada != null ? penaSelecionada.getIdPena() : null;

        RelatorioImpressaoService.imprimirFichaCompleta(
                tblApenados.getScene().getWindow(),
                apiClient,
                selecionado,
                penaId
        );
    }

    @FXML
    public void handleGerarCodigoTotem() {
        UsuarioDTO selecionado = apenadoSelecionado != null ? apenadoSelecionado : tblApenados.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Atencao");
            alert.setHeaderText("Nenhum Apenado Selecionado");
            alert.setContentText("Selecione um apenado na lista para gerar o codigo de acesso ao Totem/Kiosk.");
            alert.showAndWait();
            return;
        }

        if (btnGerarCodigoTotem != null) {
            btnGerarCodigoTotem.setDisable(true);
        }

        new Thread(() -> {
            ApiResponse<CodigoAcessoDTO> resp = apiClient.gerarCodigoAcesso(selecionado.getIdUsuario(), 1L, 1440);
            Platform.runLater(() -> {
                if (btnGerarCodigoTotem != null) {
                    btnGerarCodigoTotem.setDisable(false);
                }
                if (resp.isSuccess() && resp.getData() != null) {
                    String codigo = resp.getData().getCodigo();
                    ClipboardContent content = new ClipboardContent();
                    content.putString(codigo);
                    Clipboard.getSystemClipboard().setContent(content);

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Codigo de Acesso Kiosk / Totem");
                    alert.setHeaderText("Codigo Gerado com Sucesso!");
                    alert.setContentText("Apenado: " + selecionado.getNome() + "\n"
                            + "Codigo de Acesso: " + codigo + "\n"
                            + "Validade: 24 horas\n\n"
                            + "O codigo foi copiado automaticamente para a area de transferencia!");
                    alert.showAndWait();
                } else {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro");
                    alert.setHeaderText("Falha ao Gerar Codigo de Acesso");
                    alert.setContentText(resp.getError() != null ? resp.getError().getMessage() : "Erro desconhecido");
                    alert.showAndWait();
                }
            });
        }).start();
    }
}
