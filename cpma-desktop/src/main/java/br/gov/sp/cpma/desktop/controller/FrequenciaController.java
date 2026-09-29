package br.gov.sp.cpma.desktop.controller;

import br.gov.sp.cpma.desktop.client.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.time.*;
import java.util.ArrayList;
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
    private VBox boxFolhaMensal;

    @FXML
    private Label lblTermosContrato;

    @FXML
    private Label lblFeedbackFolhaMensal;

    @FXML
    private ComboBox<String> cbMesReferencia;

    @FXML
    private ComboBox<Integer> cbAnoReferencia;

    @FXML
    private TableView<ItemFolhaPontoMensal> tblFolhaMensal;

    @FXML
    private TableColumn<ItemFolhaPontoMensal, Boolean> colLotePresente;

    @FXML
    private TableColumn<ItemFolhaPontoMensal, String> colLoteData;

    @FXML
    private TableColumn<ItemFolhaPontoMensal, String> colLoteDia;

    @FXML
    private TableColumn<ItemFolhaPontoMensal, String> colLoteEntrada;

    @FXML
    private TableColumn<ItemFolhaPontoMensal, String> colLoteAlmoco;

    @FXML
    private TableColumn<ItemFolhaPontoMensal, String> colLoteVolta;

    @FXML
    private TableColumn<ItemFolhaPontoMensal, String> colLoteSaida;

    @FXML
    private TableColumn<ItemFolhaPontoMensal, String> colLoteHoras;

    @FXML
    private TableColumn<ItemFolhaPontoMensal, String> colLoteStatus;

    @FXML
    private TableColumn<ItemFolhaPontoMensal, String> colLoteObs;

    @FXML
    private Label lblResumoFolhaMensal;

    @FXML
    private Button btnConfirmarFolhaMensal;

    @FXML
    private TitledPane paneAvulso;

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
    private final ObservableList<ItemFolhaPontoMensal> listaGradeMensal = FXCollections.observableArrayList();
    private UsuarioDTO apenadoAtual;

    @FXML
    public void initialize() {
        dpDataTrabalho.setValue(LocalDate.now());
        configurarMesesEAno();
        configurarColunasHistorico();
        configurarColunasGradeMensal();
        configurarComboBoxPena();
        configurarComboBoxInstituicao();
        carregarInstituicoes();
        configurarListenersCalculoHoras();

        cbPenas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                carregarResumoEHistorico(newVal.getIdPena());
                preSelecionarInstituicao(newVal);
                exibirTermosContrato(newVal);
            }
        });
    }

    private void configurarMesesEAno() {
        cbMesReferencia.setItems(FXCollections.observableArrayList(
                "01 - Janeiro", "02 - Fevereiro", "03 - Março", "04 - Abril",
                "05 - Maio", "06 - Junho", "07 - Julho", "08 - Agosto",
                "09 - Setembro", "10 - Outubro", "11 - Novembro", "12 - Dezembro"
        ));
        int mesAtual = LocalDate.now().getMonthValue();
        cbMesReferencia.getSelectionModel().select(mesAtual - 1);

        int anoAtual = LocalDate.now().getYear();
        cbAnoReferencia.setItems(FXCollections.observableArrayList(anoAtual - 1, anoAtual, anoAtual + 1));
        cbAnoReferencia.setValue(anoAtual);
    }

    private void configurarColunasGradeMensal() {
        tblFolhaMensal.setEditable(true);

        colLotePresente.setCellValueFactory(cellData -> cellData.getValue().presenteProperty());
        colLotePresente.setCellFactory(tc -> new CheckBoxTableCell<ItemFolhaPontoMensal, Boolean>() {
            @Override
            public void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && getTableRow() != null && getTableRow().getItem() != null) {
                    ItemFolhaPontoMensal rowItem = getTableRow().getItem();
                    rowItem.presenteProperty().removeListener((o, oldV, newV) -> atualizarTotaisFolhaMensal());
                    rowItem.presenteProperty().addListener((o, oldV, newV) -> atualizarTotaisFolhaMensal());
                }
            }
        });

        colLoteData.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getData().toString()));
        colLoteDia.setCellValueFactory(c -> c.getValue().diaSemanaProperty());

        colLoteEntrada.setCellValueFactory(c -> c.getValue().horarioInicioProperty());
        colLoteEntrada.setCellFactory(TextFieldTableCell.forTableColumn());
        colLoteEntrada.setOnEditCommit(ev -> {
            ItemFolhaPontoMensal it = ev.getRowValue();
            it.setHorarioInicio(ev.getNewValue());
            it.recalcularHorasPelosHorarios();
            tblFolhaMensal.refresh();
            atualizarTotaisFolhaMensal();
        });

        colLoteAlmoco.setCellValueFactory(c -> c.getValue().horarioAlmocoProperty());
        colLoteAlmoco.setCellFactory(TextFieldTableCell.forTableColumn());
        colLoteAlmoco.setOnEditCommit(ev -> {
            ItemFolhaPontoMensal it = ev.getRowValue();
            it.setHorarioAlmoco(ev.getNewValue());
            it.recalcularHorasPelosHorarios();
            tblFolhaMensal.refresh();
            atualizarTotaisFolhaMensal();
        });

        colLoteVolta.setCellValueFactory(c -> c.getValue().horarioVoltaProperty());
        colLoteVolta.setCellFactory(TextFieldTableCell.forTableColumn());
        colLoteVolta.setOnEditCommit(ev -> {
            ItemFolhaPontoMensal it = ev.getRowValue();
            it.setHorarioVolta(ev.getNewValue());
            it.recalcularHorasPelosHorarios();
            tblFolhaMensal.refresh();
            atualizarTotaisFolhaMensal();
        });

        colLoteSaida.setCellValueFactory(c -> c.getValue().horarioSaidaProperty());
        colLoteSaida.setCellFactory(TextFieldTableCell.forTableColumn());
        colLoteSaida.setOnEditCommit(ev -> {
            ItemFolhaPontoMensal it = ev.getRowValue();
            it.setHorarioSaida(ev.getNewValue());
            it.recalcularHorasPelosHorarios();
            tblFolhaMensal.refresh();
            atualizarTotaisFolhaMensal();
        });

        colLoteHoras.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.1fh", c.getValue().getHorasCumpridas())));

        colLoteObs.setCellValueFactory(c -> c.getValue().atividadesProperty());
        colLoteObs.setCellFactory(TextFieldTableCell.forTableColumn());
        colLoteObs.setOnEditCommit(ev -> {
            ItemFolhaPontoMensal it = ev.getRowValue();
            it.setAtividades(ev.getNewValue());
        });

        colLoteStatus.setCellValueFactory(c -> c.getValue().statusTextoProperty());
        colLoteStatus.setCellFactory(col -> new TableCell<ItemFolhaPontoMensal, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("PRESENTE".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
                    }
                }
            }
        });

        tblFolhaMensal.setItems(listaGradeMensal);
    }

    private void exibirTermosContrato(PenaDTO pena) {
        if (pena == null) return;
        String termos = pena.getDiasSemanaEHorariosDisponivel();
        if (termos == null || termos.isBlank()) {
            lblTermosContrato.setText("Contrato: Seg a Sex • 4.0h/dia (08:00 às 12:00)");
        } else {
            lblTermosContrato.setText("Contrato: " + termos);
        }
    }

    @FXML
    public void handleGerarGradeMes() {
        PenaDTO pena = cbPenas.getValue();
        if (pena == null) {
            lblFeedbackFolhaMensal.setText("Selecione um apenado e uma pena ativa primeiro.");
            lblFeedbackFolhaMensal.getStyleClass().setAll("banner-error");
            lblFeedbackFolhaMensal.setVisible(true);
            return;
        }

        int mesIndex = cbMesReferencia.getSelectionModel().getSelectedIndex() + 1;
        Integer ano = cbAnoReferencia.getValue();
        if (ano == null) ano = LocalDate.now().getYear();

        YearMonth ym = YearMonth.of(ano, mesIndex);
        int totalDiasMes = ym.lengthOfMonth();

        String contrato = pena.getDiasSemanaEHorariosDisponivel();
        double horasPadrao = 4.0;
        String hInicio = "08:00";
        String hAlmoco = "";
        String hVolta = "";
        String hSaida = "12:00";

        if (contrato != null && contrato.contains("h/dia")) {
            try {
                String[] p = contrato.split("\\|");
                if (p.length > 1) {
                    String dadosH = p[1].trim();
                    String[] tokens = dadosH.split("h/dia");
                    horasPadrao = Double.parseDouble(tokens[0].trim());
                    if (tokens.length > 1) {
                        String turno = tokens[1].replace("(", "").replace(")", "").trim();
                        if (turno.contains("às")) {
                            String[] horasTurno = turno.split("às");
                            hInicio = horasTurno[0].trim();
                            hSaida = horasTurno[1].trim();
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        listaGradeMensal.clear();

        for (int dia = 1; dia <= totalDiasMes; dia++) {
            LocalDate data = ym.atDay(dia);
            DayOfWeek dow = data.getDayOfWeek();

            boolean diaContratado = isDiaContratado(dow, contrato);
            if (diaContratado) {
                String diaNome = formatarDiaSemana(dow);
                ItemFolhaPontoMensal item = new ItemFolhaPontoMensal(
                        data, diaNome, hInicio, hAlmoco, hVolta, hSaida, horasPadrao,
                        "Cumprimento de Medida Alternativa (" + pena.getTipoPena() + ")"
                );
                item.presenteProperty().addListener((obs, oldV, newV) -> {
                    atualizarTotaisFolhaMensal();
                    tblFolhaMensal.refresh();
                });
                listaGradeMensal.add(item);
            }
        }

        lblFeedbackFolhaMensal.setVisible(false);
        atualizarTotaisFolhaMensal();
    }

    private boolean isDiaContratado(DayOfWeek dow, String contrato) {
        if (contrato == null || contrato.isBlank()) {
            return dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
        }

        boolean seg = contrato.contains("Seg");
        boolean ter = contrato.contains("Ter");
        boolean qua = contrato.contains("Qua");
        boolean qui = contrato.contains("Qui");
        boolean sex = contrato.contains("Sex");
        boolean sab = contrato.contains("Sáb") || contrato.contains("Sab");
        boolean dom = contrato.contains("Dom");

        switch (dow) {
            case MONDAY: return seg;
            case TUESDAY: return ter;
            case WEDNESDAY: return qua;
            case THURSDAY: return qui;
            case FRIDAY: return sex;
            case SATURDAY: return sab;
            case SUNDAY: return dom;
            default: return false;
        }
    }

    private String formatarDiaSemana(DayOfWeek dow) {
        switch (dow) {
            case MONDAY: return "Segunda-feira";
            case TUESDAY: return "Terça-feira";
            case WEDNESDAY: return "Quarta-feira";
            case THURSDAY: return "Quinta-feira";
            case FRIDAY: return "Sexta-feira";
            case SATURDAY: return "Sábado";
            case SUNDAY: return "Domingo";
            default: return dow.toString();
        }
    }

    private void atualizarTotaisFolhaMensal() {
        int totalDias = listaGradeMensal.size();
        int presencas = 0;
        int faltas = 0;
        double horasTotais = 0.0;

        for (ItemFolhaPontoMensal item : listaGradeMensal) {
            if (item.isPresente()) {
                presencas++;
                horasTotais += item.getHorasCumpridas();
            } else {
                faltas++;
            }
        }

        lblResumoFolhaMensal.setText(String.format(
                "Dias previstos no mês: %d | Presenças a confirmar: %d | Faltas: %d | Horas apuradas: %.1fh",
                totalDias, presencas, faltas, horasTotais
        ));
    }

    @FXML
    public void handleMarcarTodos() {
        for (ItemFolhaPontoMensal item : listaGradeMensal) {
            item.setPresente(true);
        }
        atualizarTotaisFolhaMensal();
        tblFolhaMensal.refresh();
    }

    @FXML
    public void handleDesmarcarTodos() {
        for (ItemFolhaPontoMensal item : listaGradeMensal) {
            item.setPresente(false);
        }
        atualizarTotaisFolhaMensal();
        tblFolhaMensal.refresh();
    }

    private String[] decomporTurnoContrato(PenaDTO pena) {
        double horasPadrao = 4.0;
        String hInicio = "08:00";
        String hAlmoco = "";
        String hVolta = "";
        String hSaida = "12:00";

        if (pena != null) {
            String contrato = pena.getDiasSemanaEHorariosDisponivel();
            if (contrato != null && contrato.contains("h/dia")) {
                try {
                    String[] p = contrato.split("\\|");
                    if (p.length > 1) {
                        String dadosH = p[1].trim();
                        String[] tokens = dadosH.split("h/dia");
                        horasPadrao = Double.parseDouble(tokens[0].trim());
                        if (tokens.length > 1) {
                            String turno = tokens[1].replace("(", "").replace(")", "").trim();
                            if (turno.contains("às")) {
                                String[] horasTurno = turno.split("às");
                                hInicio = horasTurno[0].trim();
                                hSaida = horasTurno[1].trim();
                            }
                        }
                    }
                } catch (Exception ignored) {}
            }
        }
        return new String[]{hInicio, hAlmoco, hVolta, hSaida, String.valueOf(horasPadrao)};
    }

    @FXML
    public void handleAdicionarDia() {
        PenaDTO pena = cbPenas.getValue();
        LocalDate proxima = LocalDate.now();
        if (!listaGradeMensal.isEmpty()) {
            proxima = listaGradeMensal.get(listaGradeMensal.size() - 1).getData().plusDays(1);
        }
        String[] t = decomporTurnoContrato(pena);
        double horas = Double.parseDouble(t[4]);
        ItemFolhaPontoMensal it = new ItemFolhaPontoMensal(
                proxima, formatarDiaSemana(proxima.getDayOfWeek()), t[0], t[1], t[2], t[3], horas,
                pena != null && pena.getAtividadesAcordadas() != null ? pena.getAtividadesAcordadas() : "Prestação de Serviços à Comunidade"
        );
        it.presenteProperty().addListener((o, oldV, newV) -> {
            atualizarTotaisFolhaMensal();
            tblFolhaMensal.refresh();
        });
        listaGradeMensal.add(it);
        tblFolhaMensal.refresh();
        atualizarTotaisFolhaMensal();
    }

    @FXML
    public void handleAdicionarMes() {
        PenaDTO pena = cbPenas.getValue();
        LocalDate cur = LocalDate.now();
        if (!listaGradeMensal.isEmpty()) {
            cur = listaGradeMensal.get(listaGradeMensal.size() - 1).getData().plusDays(1);
        }
        String[] t = decomporTurnoContrato(pena);
        double horas = Double.parseDouble(t[4]);
        String ativ = pena != null && pena.getAtividadesAcordadas() != null ? pena.getAtividadesAcordadas() : "Prestação de Serviços à Comunidade";

        int adicionados = 0;
        while (adicionados < 22) {
            DayOfWeek dow = cur.getDayOfWeek();
            if (isDiaContratado(dow, pena != null ? pena.getDiasSemanaEHorariosDisponivel() : null)) {
                ItemFolhaPontoMensal it = new ItemFolhaPontoMensal(
                        cur, formatarDiaSemana(dow), t[0], t[1], t[2], t[3], horas, ativ
                );
                it.presenteProperty().addListener((o, oldV, newV) -> {
                    atualizarTotaisFolhaMensal();
                    tblFolhaMensal.refresh();
                });
                listaGradeMensal.add(it);
                adicionados++;
            }
            cur = cur.plusDays(1);
        }
        tblFolhaMensal.refresh();
        atualizarTotaisFolhaMensal();
    }

    @FXML
    public void handlePreencherHorariosPena() {
        PenaDTO pena = cbPenas.getValue();
        if (pena == null || listaGradeMensal.isEmpty()) return;
        String[] t = decomporTurnoContrato(pena);

        for (ItemFolhaPontoMensal it : listaGradeMensal) {
            it.setHorarioInicio(t[0]);
            it.setHorarioAlmoco(t[1]);
            it.setHorarioVolta(t[2]);
            it.setHorarioSaida(t[3]);
            if (it.isPresente()) {
                it.recalcularHorasPelosHorarios();
            }
        }
        tblFolhaMensal.refresh();
        atualizarTotaisFolhaMensal();
    }

    @FXML
    public void handleContinuarDeOndeParou() {
        PenaDTO pena = cbPenas.getValue();
        if (pena == null) {
            lblFeedbackFolhaMensal.setText("Selecione um apenado e uma pena primeiro.");
            lblFeedbackFolhaMensal.getStyleClass().setAll("banner-error");
            lblFeedbackFolhaMensal.setVisible(true);
            return;
        }

        int mesIndex = cbMesReferencia.getSelectionModel().getSelectedIndex() + 1;
        Integer ano = cbAnoReferencia.getValue();
        if (ano == null) ano = LocalDate.now().getYear();

        final int finalAno = ano;
        final int finalMes = mesIndex;

        new Thread(() -> {
            ApiResponse<List<RegistroTrabalhoDTO>> respMes = apiClient.listarRegistrosPorPenaEMes(pena.getIdPena(), finalAno, finalMes);
            if (respMes.isSuccess() && respMes.getData() != null && !respMes.getData().isEmpty()) {
                Platform.runLater(() -> {
                    listaGradeMensal.clear();
                    for (RegistroTrabalhoDTO reg : respMes.getData()) {
                        LocalDate d = reg.getDataTrabalho();
                        String diaSemana = d != null ? formatarDiaSemana(d.getDayOfWeek()) : "-";
                        ItemFolhaPontoMensal it = new ItemFolhaPontoMensal(
                                d, diaSemana, reg.getHorarioInicio(), reg.getHorarioAlmoco(), reg.getHorarioVolta(), reg.getHorarioSaida(),
                                reg.getHorasCumpridas() != null ? reg.getHorasCumpridas() : 4.0, reg.getAtividades()
                        );
                        it.presenteProperty().addListener((o, oldV, newV) -> {
                            atualizarTotaisFolhaMensal();
                            tblFolhaMensal.refresh();
                        });
                        listaGradeMensal.add(it);
                    }
                    tblFolhaMensal.refresh();
                    atualizarTotaisFolhaMensal();
                    lblFeedbackFolhaMensal.setText("Registros existentes do mês carregados para conferência e edição.");
                    lblFeedbackFolhaMensal.getStyleClass().setAll("banner-success");
                    lblFeedbackFolhaMensal.setVisible(true);
                });
            } else {
                ApiResponse<RegistroTrabalhoDTO> respUltimo = apiClient.buscarUltimoRegistroPorPena(pena.getIdPena());
                Platform.runLater(() -> {
                    LocalDate proximaData = LocalDate.now();
                    if (respUltimo.isSuccess() && respUltimo.getData() != null && respUltimo.getData().getDataTrabalho() != null) {
                        proximaData = respUltimo.getData().getDataTrabalho().plusDays(1);
                    }
                    listaGradeMensal.clear();
                    String[] t = decomporTurnoContrato(pena);
                    double horas = Double.parseDouble(t[4]);
                    ItemFolhaPontoMensal it = new ItemFolhaPontoMensal(
                            proximaData, formatarDiaSemana(proximaData.getDayOfWeek()), t[0], t[1], t[2], t[3], horas,
                            pena.getAtividadesAcordadas() != null ? pena.getAtividadesAcordadas() : "Prestação de Serviços à Comunidade"
                    );
                    it.presenteProperty().addListener((o, oldV, newV) -> {
                        atualizarTotaisFolhaMensal();
                        tblFolhaMensal.refresh();
                    });
                    listaGradeMensal.add(it);
                    tblFolhaMensal.refresh();
                    atualizarTotaisFolhaMensal();
                    lblFeedbackFolhaMensal.setText("Iniciando a partir do dia seguinte ao último registro (" + proximaData + ").");
                    lblFeedbackFolhaMensal.getStyleClass().setAll("banner-success");
                    lblFeedbackFolhaMensal.setVisible(true);
                });
            }
        }).start();
    }

    @FXML
    public void handleRemoverTudo() {
        listaGradeMensal.clear();
        tblFolhaMensal.refresh();
        atualizarTotaisFolhaMensal();
    }

    @FXML
    public void handleConfirmarFolhaMensal() {
        PenaDTO pena = cbPenas.getValue();
        if (pena == null) {
            return;
        }

        List<RegistroTrabalhoDTO> lote = new ArrayList<>();
        InstituicaoDTO inst = cbInstituicaoFrequencia.getValue();
        Long instId = inst != null ? inst.getIdInstituicao() : pena.getInstituicaoPrincipalId();

        for (ItemFolhaPontoMensal item : listaGradeMensal) {
            if (item.isPresente() && item.getHorasCumpridas() > 0) {
                RegistroTrabalhoDTO r = new RegistroTrabalhoDTO();
                r.setPenaId(pena.getIdPena());
                r.setInstituicaoId(instId);
                r.setDataTrabalho(item.getData());
                r.setHorasCumpridas(item.getHorasCumpridas());
                r.setHorarioInicio(item.getHorarioInicio());
                r.setHorarioAlmoco(item.getHorarioAlmoco());
                r.setHorarioVolta(item.getHorarioVolta());
                r.setHorarioSaida(item.getHorarioSaida());
                r.setAtividades(item.getAtividades());
                lote.add(r);
            }
        }

        if (lote.isEmpty()) {
            lblFeedbackFolhaMensal.setText("Nenhum dia de presença marcado para lançamento.");
            lblFeedbackFolhaMensal.getStyleClass().setAll("banner-error");
            lblFeedbackFolhaMensal.setVisible(true);
            return;
        }

        btnConfirmarFolhaMensal.setDisable(true);

        new Thread(() -> {
            ApiResponse<List<RegistroTrabalhoDTO>> resp = apiClient.registrarTrabalhoEmLote(lote);
            Platform.runLater(() -> {
                btnConfirmarFolhaMensal.setDisable(false);
                if (resp.isSuccess()) {
                    int salvos = resp.getData() != null ? resp.getData().size() : lote.size();
                    lblFeedbackFolhaMensal.setText("Folha de ponto mensal confirmada: " + salvos + " comparecimentos registrados com sucesso!");
                    lblFeedbackFolhaMensal.getStyleClass().setAll("banner-success");
                    lblFeedbackFolhaMensal.setVisible(true);

                    listaGradeMensal.clear();
                    carregarResumoEHistorico(pena.getIdPena());
                } else {
                    lblFeedbackFolhaMensal.setText("Erro ao salvar folha mensal: " + (resp.getError() != null ? resp.getError().getMessage() : ""));
                    lblFeedbackFolhaMensal.getStyleClass().setAll("banner-error");
                    lblFeedbackFolhaMensal.setVisible(true);
                }
            });
        }).start();
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
        boxFolhaMensal.setVisible(true);
        boxFolhaMensal.setManaged(true);
        paneAvulso.setVisible(true);
        paneAvulso.setManaged(true);
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
                    lblFeedbackLancamento.setText("Presenca avulsa registrada com sucesso (" + horas + "h)!");
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
        boxFolhaMensal.setVisible(false);
        boxFolhaMensal.setManaged(false);
        paneAvulso.setVisible(false);
        paneAvulso.setManaged(false);
        boxHistorico.setVisible(false);
        boxHistorico.setManaged(false);
    }
}
