package controller;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dao.InstituicaoDAO;
import dao.PenaDAO;
import dao.PenaInstituicaoDAO;
import dao.RegistroDeTrabalhoDAO;
import dao.UsuarioDAO;
import utils.FormatacaoUtils;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Instituicao;
import model.Pena;
import model.RegistroDeTrabalho;
import model.RegistroTrabalhoTemp;
import model.Usuario;

public class CadastroRegistroDeTrabalhoController {

    @FXML private TextField txtInstituicoesVinculadas;
    @FXML private ComboBox<Usuario> comboUsuario;
    @FXML private ComboBox<Pena> comboPena;
    @FXML private Button btnCadastrar;
    @FXML private Button btnAdicionarDia;
    @FXML private Button btnAdicionarMes;
    @FXML private Button btnPreencherHorariosPena;
    @FXML private Button btnRemoverTudo;
    @FXML private Button btnInfoHorarios;
    @FXML private Label lblTotalHoras;
    
    @FXML private TableView<RegistroTrabalhoTemp> tabelaRegistros;
    @FXML private TableColumn<RegistroTrabalhoTemp, LocalDate> colData;
    @FXML private TableColumn<RegistroTrabalhoTemp, Instituicao> colInst;
    @FXML private TableColumn<RegistroTrabalhoTemp, LocalTime> colInicio;
    @FXML private TableColumn<RegistroTrabalhoTemp, LocalTime> colAlmoco;
    @FXML private TableColumn<RegistroTrabalhoTemp, LocalTime> colVolta;
    @FXML private TableColumn<RegistroTrabalhoTemp, LocalTime> colSaida;
    @FXML private TableColumn<RegistroTrabalhoTemp, Double> colHoras;
    @FXML private TableColumn<RegistroTrabalhoTemp, Void> colAcao;

    private ObservableList<RegistroTrabalhoTemp> listaRegistros;
    private List<Instituicao> instituicoesDaPena = new ArrayList<>();
    private final java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("HH:mm");
    private final StringConverter<Instituicao> instituicaoConverter = new StringConverter<>() {
        @Override
        public String toString(Instituicao i) {
            return i == null ? "" : i.getNome();
        }

        @Override
        public Instituicao fromString(String s) {
            return null;
        }
    };

    @FXML
    public void initialize() {
        try {
            listaRegistros = FXCollections.observableArrayList();
            
            if (tabelaRegistros == null) {
                System.err.println("ERRO: tabelaRegistros é null!");
                return;
            }
            
            tabelaRegistros.setItems(listaRegistros);
            
            // Verifica se os componentes principais existem
            if (btnCadastrar == null) {
                System.err.println("ERRO: btnCadastrar é null!");
            }
            if (comboUsuario == null) {
                System.err.println("ERRO: comboUsuario é null!");
            }
            if (txtInstituicoesVinculadas == null) {
                System.err.println("ERRO: txtInstituicoesVinculadas é null!");
            }
            if (comboPena == null) {
                System.err.println("ERRO: comboPena é null!");
            }
            
            carregarUsuarios();
            configurarTabela();
            configurarBotoes();
            atualizarTotalHoras();
        } catch (Exception e) {
            System.err.println("ERRO ao inicializar CadastroRegistroDeTrabalhoController:");
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Erro de Inicialização");
            alert.setHeaderText("Não foi possível inicializar a tela");
            alert.setContentText("Erro: " + e.getMessage() + "\n\nVerifique o console para mais detalhes.");
            alert.showAndWait();
        }
    }

    private void configurarBotoes() {
        btnCadastrar.setOnAction(e -> cadastrar());
        btnAdicionarDia.setOnAction(e -> {
            adicionarDia();
            // Mostra botão "Adicionar Mês" após adicionar primeiro dia
            if (!listaRegistros.isEmpty()) {
                btnAdicionarMes.setVisible(true);
                btnAdicionarMes.setManaged(true);
            }
        });
        btnAdicionarMes.setOnAction(e -> {
            adicionarMes();
            atualizarVisibilidadeBotoes();
        });
        btnPreencherHorariosPena.setOnAction(e -> preencherHorariosDaPena());
        btnRemoverTudo.setOnAction(e -> removerTudoComConfirmacao());
        btnInfoHorarios.setOnAction(e -> mostrarInformacoesHorarios());
        
        // Listener para atualizar visibilidade do botão quando a lista mudar
        listaRegistros.addListener((javafx.collections.ListChangeListener.Change<? extends RegistroTrabalhoTemp> change) -> {
            atualizarVisibilidadeBotoes();
        });
        
                comboUsuario.setOnAction(e -> {
            Usuario u = comboUsuario.getValue();
            if (u != null) {
                carregarPenasDoUsuario(u.getIdUsuario());
                comboPena.setDisable(false);
                // Quando selecionar pena, verificar se há registros do mês e carregar
                comboPena.setOnAction(e2 -> {
                    // Limpa registros anteriores e reseta modo de edição
                    listaRegistros.clear();
                    modoEdicao = false;
                    atualizarVisibilidadeBotoes();
                    atualizarTotalHoras();
                    Pena penaSel = comboPena.getValue();
                    if (penaSel != null) {
                        carregarInstituicoesDaPena(penaSel.getIdPena());
                    }
                    continuarDeOndeParou();
                });
            } else {
                comboPena.getItems().clear();
                comboPena.setDisable(true);
                listaRegistros.clear();
                limparInstituicoesDaPena();
                modoEdicao = false;
                atualizarVisibilidadeBotoes();
                atualizarTotalHoras();
            }
        });
        
        // Inicializa comboPena como desabilitado
        comboPena.setDisable(true);
        
        // Inicializa botão "Adicionar Mês" como invisível
        btnAdicionarMes.setVisible(false);
        btnAdicionarMes.setManaged(false);
    }
    
    private boolean modoEdicao = false; // Indica se está editando registros existentes
    
    private void atualizarVisibilidadeBotoes() {
        boolean temRegistros = !listaRegistros.isEmpty();
        int quantidadeRegistros = listaRegistros.size();
        boolean mesInteiroAdicionado = quantidadeRegistros >= 22; // Mês inteiro = ~22 dias úteis
        
        btnAdicionarMes.setVisible(temRegistros);
        btnAdicionarMes.setManaged(temRegistros);
        
        // Habilita botão de preencher horários apenas se tiver mês inteiro
        btnPreencherHorariosPena.setDisable(!mesInteiroAdicionado);
        
        // Se está em modo de edição, desabilita botões de adicionar
        if (modoEdicao) {
            btnAdicionarDia.setDisable(true);
            btnAdicionarMes.setDisable(true);
        } else {
            btnAdicionarDia.setDisable(false);
            if (btnAdicionarMes.isVisible()) {
                btnAdicionarMes.setDisable(false);
            }
        }
    }
    
    private void removerTudoComConfirmacao() {
        if (listaRegistros.isEmpty()) {
            alert("Não há registros para remover.");
            return;
        }
        
        Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacao.setTitle("Confirmar Remoção");
        confirmacao.setHeaderText("Remover todos os registros?");
        confirmacao.setContentText("Esta ação irá remover todos os " + listaRegistros.size() + " registro(s) da tabela. Esta ação não pode ser desfeita.");
        
        confirmacao.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                listaRegistros.clear();
                atualizarTotalHoras();
                atualizarVisibilidadeBotoes();
            }
        });
    }

    private void continuarDeOndeParou() {
        Pena pena = comboPena.getValue();
        if (pena == null) return;
        
        LocalDate hoje = LocalDate.now();
        int mesAtual = hoje.getMonthValue();
        int anoAtual = hoje.getYear();
        
        // Verifica se já existem registros do mês atual
        List<RegistroDeTrabalho> registrosDoMes = RegistroDeTrabalhoDAO.buscarPorPenaEMes(
            pena.getIdPena(), mesAtual, anoAtual);
        
        if (!registrosDoMes.isEmpty()) {
            // Se já existem registros do mês atual, carrega eles para edição
            carregarRegistrosExistentes(registrosDoMes);
            alert("Registros do mês atual já existem. Carregados para edição.");
        } else {
            // Se não existem, busca última data cadastrada e sugere continuar
            java.sql.Date ultimaData = RegistroDeTrabalhoDAO.buscarUltimaDataPorPena(pena.getIdPena());
            
            if (ultimaData != null) {
                LocalDate dataUltima = ultimaData.toLocalDate();
                // Só sugere continuar se a última data não for do mês atual
                if (dataUltima.getMonthValue() != mesAtual || dataUltima.getYear() != anoAtual) {
                    LocalDate dataInicio = ultimaData.toLocalDate().plusDays(1);
                    if (listaRegistros.isEmpty()) {
                        RegistroTrabalhoTemp novoRegistro = new RegistroTrabalhoTemp(dataInicio);
                        aplicarInstituicaoPadraoNoRegistro(novoRegistro);
                        listaRegistros.add(novoRegistro);
                        atualizarTotalHoras();
                        atualizarVisibilidadeBotoes();
                    }
                }
            }
        }
    }
    
    /**
     * Carrega registros existentes na tabela para edição.
     */
    private void carregarRegistrosExistentes(List<RegistroDeTrabalho> registros) {
        modoEdicao = true;
        listaRegistros.clear();
        
        Pena pena = comboPena.getValue();
        for (RegistroDeTrabalho reg : registros) {
            RegistroTrabalhoTemp temp = new RegistroTrabalhoTemp(reg.getDataTrabalho().toLocalDate());
            temp.setIdRegistro(reg.getIdRegistro()); // Armazena o ID para UPDATE posterior
            temp.setHorarioInicio(reg.getHorarioInicio() != null ? reg.getHorarioInicio().toLocalTime() : null);
            temp.setHorarioAlmoco(reg.getHorarioAlmoco() != null ? reg.getHorarioAlmoco().toLocalTime() : null);
            temp.setHorarioVolta(reg.getHorarioVolta() != null ? reg.getHorarioVolta().toLocalTime() : null);
            temp.setHorarioSaida(reg.getHorarioSaida() != null ? reg.getHorarioSaida().toLocalTime() : null);
            preencherInstituicaoNoRegistro(temp, reg, pena);
            listaRegistros.add(temp);
        }

        atualizarTotalHoras();
        atualizarVisibilidadeBotoes();
        
        // Mostra botão de adicionar mês se necessário, mas desabilitado
        if (!listaRegistros.isEmpty()) {
            btnAdicionarMes.setVisible(true);
            btnAdicionarMes.setManaged(true);
        }
    }

    private void configurarTabela() {
        // Verifica se as colunas existem
        if (colData == null || colInst == null || colInicio == null || colAlmoco == null || 
            colVolta == null || colSaida == null || colHoras == null || colAcao == null) {
            System.err.println("ERRO: Uma ou mais colunas da tabela são null!");
            System.err.println("colData: " + (colData != null) + 
                             ", colInst: " + (colInst != null) +
                             ", colInicio: " + (colInicio != null) +
                             ", colAlmoco: " + (colAlmoco != null) +
                             ", colVolta: " + (colVolta != null) +
                             ", colSaida: " + (colSaida != null) +
                             ", colHoras: " + (colHoras != null) +
                             ", colAcao: " + (colAcao != null));
            return;
        }

        colInst.setCellValueFactory(data -> {
            RegistroTrabalhoTemp temp = data.getValue();
            return new SimpleObjectProperty<>(temp != null ? buscarInstituicaoPorId(temp.getFkInstituicaoIdInstituicao()) : null);
        });
        colInst.setCellFactory(column -> new TableCell<RegistroTrabalhoTemp, Instituicao>() {
            private final ComboBox<Instituicao> comboInst = new ComboBox<>();

            {
                comboInst.setConverter(instituicaoConverter);
                comboInst.setMaxWidth(Double.MAX_VALUE);
                comboInst.setOnAction(e -> {
                    RegistroTrabalhoTemp temp = getTableRow() != null ? getTableRow().getItem() : null;
                    Instituicao selecionada = comboInst.getValue();
                    if (temp != null && selecionada != null) {
                        temp.setInstituicao(selecionada.getIdInstituicao(), selecionada.getNome());
                    }
                });
            }

            @Override
            protected void updateItem(Instituicao item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    comboInst.setItems(FXCollections.observableArrayList(instituicoesDaPena));
                    boolean unica = instituicoesDaPena.size() == 1;
                    comboInst.setDisable(unica || instituicoesDaPena.isEmpty());
                    comboInst.setPromptText(unica ? null : "Selecione...");
                    if (item == null && unica) {
                        item = instituicoesDaPena.get(0);
                        getTableRow().getItem().setInstituicao(item.getIdInstituicao(), item.getNome());
                    }
                    comboInst.setValue(item);
                    setGraphic(comboInst);
                }
            }
        });
        
        // Coluna Data (DatePicker editável)
        colData.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData()));
        colData.setCellFactory(column -> new TableCell<RegistroTrabalhoTemp, LocalDate>() {
            private DatePicker datePicker = new DatePicker();

            {
                FormatacaoUtils.configurarDatePickerBrasileiro(datePicker);
                datePicker.setOnAction(e -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        getTableRow().getItem().setData(FormatacaoUtils.obterDataValida(datePicker));
                        tabelaRegistros.refresh();
                    }
                });
            }

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    datePicker.setValue(item);
                    setGraphic(datePicker);
                }
            }
        });

        // Colunas de horário (TextField editável)
        configurarColunaHora(colInicio, "inicio");
        configurarColunaHora(colAlmoco, "almoco");
        configurarColunaHora(colVolta, "volta");
        configurarColunaHora(colSaida, "saida");

        // Coluna Horas (readonly, calculada)
        colHoras.setCellValueFactory(data -> 
            new SimpleObjectProperty<>(data.getValue().getHorasCalculadas()));
        colHoras.setCellFactory(column -> new TableCell<RegistroTrabalhoTemp, Double>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f", item));
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #059669;");
                }
            }
        });

        // Coluna Ação (botão remover)
        colAcao.setCellFactory(column -> new TableCell<RegistroTrabalhoTemp, Void>() {
            private final Button btn = new Button("🗑️");

            {
                btn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 12px; -fx-cursor: hand; -fx-padding: 5px 10px;");
                btn.setOnAction(e -> {
                    RegistroTrabalhoTemp item = getTableView().getItems().get(getIndex());
                    listaRegistros.remove(item);
                    atualizarTotalHoras();
                    atualizarVisibilidadeBotoes();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });

        tabelaRegistros.setPlaceholder(new Label("📅 Clique em 'Adicionar Dia' ou 'Adicionar Mês' para começar."));
    }
    
    /**
     * Mostra informações sobre a funcionalidade dos botões relacionados a horários
     */
    private void mostrarInformacoesHorarios() {
        StringBuilder info = new StringBuilder();
        info.append("ℹ️ INFORMAÇÕES SOBRE OS BOTÕES\n\n");
        info.append("➕ ADICIONAR DIA\n");
        info.append("Adiciona uma nova linha na tabela com uma data.\n");
        info.append("A data será sugerida automaticamente baseada no último registro.\n\n");
        info.append("📅 ADICIONAR MÊS\n");
        info.append("Adiciona aproximadamente 22 dias úteis (segunda a sexta)\n");
        info.append("a partir do último dia cadastrado na tabela.\n");
        info.append("Este botão só aparece após adicionar o primeiro dia.\n\n");
        info.append("⏰ PREENCHER HORÁRIOS DA PENA\n");
        info.append("Preenche automaticamente os horários de todos os registros\n");
        info.append("baseado nos horários cadastrados na pena.\n");
        info.append("Este botão só fica habilitado após adicionar o mês completo (22 dias).\n\n");
        info.append("🗑️ REMOVER TUDO\n");
        info.append("Remove todos os registros da tabela após confirmação.\n\n");
        info.append("🗑️ REMOVER (na tabela)\n");
        info.append("Remove uma linha específica da tabela.\n\n");
        info.append("⚠️ IMPORTANTE:\n");
        info.append("Registros com data mas sem horários completos serão\n");
        info.append("automaticamente descartados ao cadastrar.");
        
        Alert infoAlert = new Alert(Alert.AlertType.INFORMATION);
        infoAlert.setTitle("Informações sobre Horários");
        infoAlert.setHeaderText(null);
        infoAlert.setContentText(info.toString());
        infoAlert.getDialogPane().setPrefWidth(450);
        infoAlert.showAndWait();
    }

    private void configurarColunaHora(TableColumn<RegistroTrabalhoTemp, LocalTime> coluna, String tipo) {
        coluna.setCellValueFactory(data -> {
            RegistroTrabalhoTemp registro = data.getValue();
            if (registro == null) return new SimpleObjectProperty<>(null);
            switch (tipo) {
                case "inicio": return new SimpleObjectProperty<>(registro.getHorarioInicio());
                case "almoco": return new SimpleObjectProperty<>(registro.getHorarioAlmoco());
                case "volta": return new SimpleObjectProperty<>(registro.getHorarioVolta());
                case "saida": return new SimpleObjectProperty<>(registro.getHorarioSaida());
                default: return new SimpleObjectProperty<>(null);
            }
        });

        coluna.setCellFactory(column -> new TableCell<RegistroTrabalhoTemp, LocalTime>() {
            private TextField textField = new TextField();

            {
                FormatacaoUtils.aplicarFormatacaoHora(textField);
                
                textField.setOnAction(e -> atualizarHora());
                
                textField.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
                    if (!isNowFocused) {
                        atualizarHora();
                    }
                });
            }
            
            private void atualizarHora() {
                if (getTableRow() == null || getTableRow().getItem() == null) {
                    return;
                }
                
                LocalTime hora = FormatacaoUtils.getHoraValue(textField);
                RegistroTrabalhoTemp registro = getTableRow().getItem();
                
                switch (tipo) {
                    case "inicio": registro.setHorarioInicio(hora); break;
                    case "almoco": registro.setHorarioAlmoco(hora); break;
                    case "volta": registro.setHorarioVolta(hora); break;
                    case "saida": registro.setHorarioSaida(hora); break;
                }
                
                atualizarTotalHoras();
                // Força atualização da coluna de horas
                tabelaRegistros.refresh();
            }

            @Override
            protected void updateItem(LocalTime item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    if (item != null) {
                        textField.setText(item.format(fmt));
                    } else {
                        textField.clear();
                    }
                    setGraphic(textField);
                }
            }
        });
    }

    private void adicionarDia() {
        Pena pena = comboPena.getValue();
        LocalDate dataInicio;
        
        if (pena != null) {
            // Busca última data cadastrada ou última da lista
            if (listaRegistros.isEmpty()) {
                java.sql.Date ultimaData = RegistroDeTrabalhoDAO.buscarUltimaDataPorPena(pena.getIdPena());
                dataInicio = ultimaData != null ? ultimaData.toLocalDate().plusDays(1) : LocalDate.now();
            } else {
                // Pega a última data da lista e adiciona 1 dia
                LocalDate ultimaData = listaRegistros.stream()
                    .map(RegistroTrabalhoTemp::getData)
                    .filter(d -> d != null)
                    .max(LocalDate::compareTo)
                    .orElse(LocalDate.now());
                dataInicio = ultimaData.plusDays(1);
            }
        } else {
            dataInicio = listaRegistros.isEmpty() ? LocalDate.now() : 
                listaRegistros.stream()
                    .map(RegistroTrabalhoTemp::getData)
                    .filter(d -> d != null)
                    .max(LocalDate::compareTo)
                    .orElse(LocalDate.now())
                    .plusDays(1);
        }
        
        RegistroTrabalhoTemp novo = new RegistroTrabalhoTemp(dataInicio);
        aplicarInstituicaoPadraoNoRegistro(novo);
        listaRegistros.add(novo);
        atualizarTotalHoras();
    }

    private void adicionarMes() {
        if (listaRegistros.isEmpty()) {
            alert("Adicione primeiro um dia antes de adicionar o mês.");
            return;
        }
        
        // Pega a última data da lista para começar a partir dela
        LocalDate dataInicio = listaRegistros.stream()
            .map(RegistroTrabalhoTemp::getData)
            .filter(d -> d != null)
            .max(LocalDate::compareTo)
            .orElse(LocalDate.now());
        
        // Adiciona a partir do dia seguinte ao último dia na lista
        dataInicio = dataInicio.plusDays(1);
        
        // Adiciona todos os dias úteis do mês (segunda a sexta)
        LocalDate dataAtual = dataInicio;
        int diasAdicionados = 0;
        
        while (diasAdicionados < 22) { // Aproximadamente um mês de dias úteis
            int diaSemana = dataAtual.getDayOfWeek().getValue();
            // Segunda=1 até Sexta=5
            if (diaSemana >= 1 && diaSemana <= 5) {
                RegistroTrabalhoTemp novo = new RegistroTrabalhoTemp(dataAtual);
                aplicarInstituicaoPadraoNoRegistro(novo);
                listaRegistros.add(novo);
                diasAdicionados++;
            }
            dataAtual = dataAtual.plusDays(1);
            
            // Limite de segurança para evitar loop infinito
            if (dataAtual.isAfter(dataInicio.plusMonths(2))) {
                break;
            }
        }
        
        atualizarTotalHoras();
    }

    /**
     * Preenche os horários de todos os registros da tabela baseado na pena cadastrada.
     * Os horários são aplicados conforme o dia da semana de cada data.
     */
    private void preencherHorariosDaPena() {
        Pena pena = comboPena.getValue();
        if (pena == null) {
            alert("Selecione uma pena primeiro.");
            return;
        }
        
        String horariosStr = pena.getDiasSemanaEHorariosDisponivel();
        if (horariosStr == null || horariosStr.trim().isEmpty()) {
            alert("A pena selecionada não possui horários cadastrados.");
            return;
        }
        
        // Mapeia os horários por dia da semana
        java.util.Map<String, HorariosDia> horariosPorDia = parsearHorariosDaPena(horariosStr);
        
        if (horariosPorDia.isEmpty()) {
            alert("Não foi possível interpretar os horários da pena.");
            return;
        }
        
        int preenchidos = 0;
        for (RegistroTrabalhoTemp registro : listaRegistros) {
            if (registro.getData() == null) continue;
            
            // Obtém o dia da semana (segunda, terça, etc.)
            String diaSemana = obterNomeDiaSemana(registro.getData());
            
            HorariosDia horarios = horariosPorDia.get(diaSemana);
            if (horarios != null) {
                // Mapeia os horários da pena para o registro de trabalho
                // Pena: inicio1, fim1, inicio2, fim2
                // Registro: inicio, almoco, volta, saida
                registro.setHorarioInicio(horarios.inicio1);
                registro.setHorarioAlmoco(horarios.fim1);
                
                // Se tiver segundo turno, usa os horários da tarde
                if (horarios.inicio2 != null && horarios.fim2 != null) {
                    registro.setHorarioVolta(horarios.inicio2);
                    registro.setHorarioSaida(horarios.fim2);
                } else {
                    // Se não tiver segundo turno, usa os mesmos horários da manhã
                    registro.setHorarioVolta(horarios.fim1);
                    registro.setHorarioSaida(horarios.fim1);
                }
                preenchidos++;
            }
        }
        
        atualizarTotalHoras();
        tabelaRegistros.refresh();
        alert(String.format("Horários preenchidos para %d dia(s) baseado na pena cadastrada.", preenchidos));
    }
    
    /**
     * Classe auxiliar para armazenar horários de um dia
     */
    private static class HorariosDia {
        LocalTime inicio1;
        LocalTime fim1;
        LocalTime inicio2;
        LocalTime fim2;
    }
    
    /**
     * Parseia a string de horários da pena e retorna um mapa por dia da semana.
     * Formato esperado: "segunda 08:00 12:00 14:00 18:00, terça 08:00 12:00, ..."
     */
    private java.util.Map<String, HorariosDia> parsearHorariosDaPena(String horariosStr) {
        java.util.Map<String, HorariosDia> mapa = new java.util.HashMap<>();
        
        if (horariosStr == null || horariosStr.trim().isEmpty()) {
            return mapa;
        }
        
        // Divide por vírgula para separar os dias
        String[] dias = horariosStr.split(",\\s*");
        
        for (String diaStr : dias) {
            String[] partes = diaStr.trim().split("\\s+");
            if (partes.length < 1) continue;
            
            String diaSemana = partes[0].toLowerCase();
            HorariosDia horarios = new HorariosDia();
            
            // Tenta parsear os horários (formato: dia inicio1 fim1 [inicio2 fim2])
            // Exemplo: "segunda 08:00 12:00 14:00 18:00" ou "terça 08:00 12:00"
            try {
                if (partes.length >= 3) {
                    // Tem pelo menos início e fim do primeiro turno
                    horarios.inicio1 = LocalTime.parse(partes[1], fmt);
                    horarios.fim1 = LocalTime.parse(partes[2], fmt);
                    
                    // Se tiver mais horários, são do segundo turno
                    if (partes.length >= 5) {
                        // Tem dois turnos completos
                        horarios.inicio2 = LocalTime.parse(partes[3], fmt);
                        horarios.fim2 = LocalTime.parse(partes[4], fmt);
                    } else if (partes.length >= 4) {
                        // Tem apenas início do segundo turno, usa o mesmo horário como fim
                        horarios.inicio2 = LocalTime.parse(partes[3], fmt);
                        horarios.fim2 = horarios.inicio2;
                    }
                }
                
                // Só adiciona ao mapa se tiver pelo menos inicio1 e fim1
                if (horarios.inicio1 != null && horarios.fim1 != null) {
                    mapa.put(diaSemana, horarios);
                }
            } catch (Exception e) {
                System.err.println("Erro ao parsear horários para " + diaSemana + ": " + e.getMessage());
            }
        }
        
        return mapa;
    }
    
    /**
     * Obtém o nome do dia da semana em português
     */
    private String obterNomeDiaSemana(LocalDate data) {
        int diaSemana = data.getDayOfWeek().getValue();
        switch (diaSemana) {
            case 1: return "segunda";
            case 2: return "terça";
            case 3: return "quarta";
            case 4: return "quinta";
            case 5: return "sexta";
            case 6: return "sábado";
            case 7: return "domingo";
            default: return "";
        }
    }

    private void atualizarTotalHoras() {
        double total = listaRegistros.stream()
            .mapToDouble(RegistroTrabalhoTemp::getHorasCalculadas)
            .sum();
        lblTotalHoras.setText(String.format("Total: %.2f horas", total));
    }

    private void carregarInstituicoesDaPena(int idPena) {
        instituicoesDaPena = new ArrayList<>(PenaInstituicaoDAO.buscarInstituicoesPorPena(idPena));
        if (txtInstituicoesVinculadas != null) {
            if (instituicoesDaPena.isEmpty()) {
                txtInstituicoesVinculadas.setText("Nenhuma instituição vinculada à pena");
            } else {
                String nomes = instituicoesDaPena.stream()
                        .map(Instituicao::getNome)
                        .collect(Collectors.joining(", "));
                txtInstituicoesVinculadas.setText(nomes);
            }
        }
        tabelaRegistros.refresh();
    }

    private void limparInstituicoesDaPena() {
        instituicoesDaPena.clear();
        if (txtInstituicoesVinculadas != null) {
            txtInstituicoesVinculadas.clear();
        }
    }

    private Instituicao buscarInstituicaoPorId(Integer id) {
        if (id == null || id <= 0) {
            return null;
        }
        return instituicoesDaPena.stream()
                .filter(i -> i.getIdInstituicao() == id)
                .findFirst()
                .orElse(null);
    }

    private void aplicarInstituicaoPadraoNoRegistro(RegistroTrabalhoTemp temp) {
        if (instituicoesDaPena.size() == 1) {
            Instituicao unica = instituicoesDaPena.get(0);
            temp.setInstituicao(unica.getIdInstituicao(), unica.getNome());
        }
    }

    private String formatarDataLinha(RegistroTrabalhoTemp temp) {
        if (temp == null || temp.getData() == null) {
            return "data não informada";
        }
        return temp.getData().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    private void preencherInstituicaoNoRegistro(RegistroTrabalhoTemp temp, RegistroDeTrabalho reg, Pena pena) {
        Integer idInst = reg.getFkInstituicaoIdInstituicao();
        if (idInst == null || idInst <= 0) {
            idInst = pena != null ? pena.getFkInstituicaoIdInstituicao() : null;
        }
        if (idInst != null && idInst > 0) {
            String nome = InstituicaoDAO.buscarNomePorId(idInst);
            temp.setInstituicao(idInst, nome != null ? nome : "");
        }
    }

    private void carregarUsuarios() {
        comboUsuario.setItems(FXCollections.observableArrayList(UsuarioDAO.buscarTodosUsuarios()));
        
        comboUsuario.setConverter(new StringConverter<Usuario>() {
            @Override
            public String toString(Usuario usuario) {
                if (usuario == null) {
                    return "";
                }
                return usuario.getCpf() + " - " + usuario.getNome();
            }

            @Override
            public Usuario fromString(String string) {
                return null;
            }
        });
    }

    private void carregarPenasDoUsuario(int idUsuario) {
        comboPena.setItems(FXCollections.observableArrayList(PenaDAO.buscarPenasPorUsuario(idUsuario)));
        comboPena.setConverter(new StringConverter<Pena>() {
            @Override
            public String toString(Pena pena) {
                return pena == null ? "" : pena.getTipoPena();
            }

            @Override
            public Pena fromString(String string) {
                return null;
            }
        });
    }

    private void cadastrar() {
        try {
            Usuario user = comboUsuario.getValue();
            Pena pena = comboPena.getValue();

            if (user == null || pena == null) {
                alert("Escolha o apenado e a pena.");
                return;
            }

            if (instituicoesDaPena.isEmpty()) {
                alert("Esta pena não possui instituições vinculadas. Cadastre-as no cadastro de pena.");
                return;
            }

            if (listaRegistros.isEmpty()) {
                alert("Adicione pelo menos um registro de trabalho.");
                return;
            }

            // Verifica registros com data mas sem horários completos
            List<RegistroTrabalhoTemp> registrosIncompletos = new ArrayList<>();
            for (RegistroTrabalhoTemp temp : listaRegistros) {
                if (temp.getData() != null && !temp.isValid()) {
                    registrosIncompletos.add(temp);
                }
            }
            
            // Se houver registros incompletos, mostra aviso
            if (!registrosIncompletos.isEmpty()) {
                Alert aviso = new Alert(Alert.AlertType.WARNING);
                aviso.setTitle("Aviso sobre Registros Incompletos");
                aviso.setHeaderText("Registros com data mas sem horários completos");
                aviso.setContentText(String.format(
                    "Encontrados %d registro(s) com data mas sem horários completos.\n\n" +
                    "Esses registros serão automaticamente descartados e NÃO serão cadastrados.\n\n" +
                    "Deseja continuar mesmo assim?",
                    registrosIncompletos.size()
                ));
                
                aviso.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
                java.util.Optional<ButtonType> resultado = aviso.showAndWait();
                
                if (resultado.isEmpty() || resultado.get() != ButtonType.YES) {
                    return; // Usuário cancelou
                }
            }

            // Converte registros temporários válidos para registros definitivos
            List<RegistroDeTrabalho> registrosParaSalvar = new ArrayList<>();
            List<RegistroDeTrabalho> registrosParaAtualizar = new ArrayList<>();
            int registrosDescartados = 0;
            
            for (RegistroTrabalhoTemp temp : listaRegistros) {
                // Só salva registros válidos (com data E horários completos)
                if (!temp.isValid()) {
                    registrosDescartados++;
                    continue;
                }

                Integer idInstRegistro = temp.getFkInstituicaoIdInstituicao();
                if (idInstRegistro == null || idInstRegistro <= 0) {
                    alert("Selecione a instituição em cada linha da tabela antes de cadastrar.");
                    return;
                }
                if (!PenaInstituicaoDAO.vinculada(pena.getIdPena(), idInstRegistro)) {
                    alert("Instituição inválida na linha de " + formatarDataLinha(temp) + ".");
                    return;
                }
                
                RegistroDeTrabalho registro = new RegistroDeTrabalho();
                registro.setFkPenaId(pena.getIdPena());
                registro.setFkInstituicaoIdInstituicao(idInstRegistro);
                registro.setDataTrabalho(Date.valueOf(temp.getData()));
                registro.setHorasCumpridas(temp.getHorasCalculadas());
                registro.setAtividades(""); // Campo removido, sempre vazio
                registro.setHorarioInicio(Time.valueOf(temp.getHorarioInicio()));
                registro.setHorarioAlmoco(Time.valueOf(temp.getHorarioAlmoco()));
                registro.setHorarioVolta(Time.valueOf(temp.getHorarioVolta()));
                registro.setHorarioSaida(Time.valueOf(temp.getHorarioSaida()));
                
                if (modoEdicao && !temp.isNovoRegistro()) {
                    // É um registro existente que precisa ser atualizado
                    registro.setIdRegistro(temp.getIdRegistro());
                    registrosParaAtualizar.add(registro);
                } else {
                    // É um novo registro
                    registrosParaSalvar.add(registro);
                }
            }
            
            if (registrosParaSalvar.isEmpty() && registrosParaAtualizar.isEmpty()) {
                alert("Nenhum registro válido para salvar. Preencha os horários.");
                return;
            }

            RegistroDeTrabalhoDAO dao = new RegistroDeTrabalhoDAO();
            boolean ok = true;
            String mensagem = "";
            
            // Atualiza registros existentes
            if (!registrosParaAtualizar.isEmpty()) {
                int atualizados = 0;
                for (RegistroDeTrabalho reg : registrosParaAtualizar) {
                    if (dao.atualizar(reg)) {
                        atualizados++;
                    } else {
                        ok = false;
                    }
                }
                if (atualizados > 0) {
                    mensagem = String.format("%d registro(s) atualizado(s). ", atualizados);
                }
            }
            
            // Insere novos registros
            if (!registrosParaSalvar.isEmpty()) {
                boolean inseridos = dao.inserirBatch(registrosParaSalvar);
                if (inseridos) {
                    mensagem += String.format("%d registro(s) cadastrado(s).", registrosParaSalvar.size());
                } else {
                    ok = false;
                }
            }
            
            if (!ok) {
                mensagem = "Falha ao gravar as alterações.";
            }
            
            if (registrosDescartados > 0) {
                mensagem += String.format("\n%d registro(s) foram descartados por estarem incompletos.", registrosDescartados);
            }
            
            alert(mensagem);
            
            if (ok) {
                fecharJanela();
            }
        } catch (Exception ex) {
            alert("Erro: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK).showAndWait();
    }

    private void fecharJanela() {
        Stage stage = (Stage) btnCadastrar.getScene().getWindow();
        stage.close();
    }
}
