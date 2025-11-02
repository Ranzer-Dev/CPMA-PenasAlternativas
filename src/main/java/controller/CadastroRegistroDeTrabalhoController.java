package controller;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import dao.InstituicaoDAO;
import dao.PenaDAO;
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
import javafx.util.converter.LocalDateStringConverter;
import model.Instituicao;
import model.Pena;
import model.RegistroDeTrabalho;
import model.RegistroTrabalhoTemp;
import model.Usuario;

public class CadastroRegistroDeTrabalhoController {

    @FXML private ComboBox<Instituicao> instituicao;
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
    @FXML private TableColumn<RegistroTrabalhoTemp, LocalTime> colInicio;
    @FXML private TableColumn<RegistroTrabalhoTemp, LocalTime> colAlmoco;
    @FXML private TableColumn<RegistroTrabalhoTemp, LocalTime> colVolta;
    @FXML private TableColumn<RegistroTrabalhoTemp, LocalTime> colSaida;
    @FXML private TableColumn<RegistroTrabalhoTemp, Double> colHoras;
    @FXML private TableColumn<RegistroTrabalhoTemp, Void> colAcao;

    private ObservableList<RegistroTrabalhoTemp> listaRegistros;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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
            if (instituicao == null) {
                System.err.println("ERRO: instituicao é null!");
            }
            if (comboPena == null) {
                System.err.println("ERRO: comboPena é null!");
            }
            
            carregarUsuarios();
            carregarInstituicoes();
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
                // Quando selecionar pena, buscar última data e continuar de onde parou
                comboPena.setOnAction(e2 -> {
                    continuarDeOndeParou();
                });
            } else {
                comboPena.getItems().clear();
                comboPena.setDisable(true);
            }
        });
        
        // Inicializa comboPena como desabilitado
        comboPena.setDisable(true);
        
        // Inicializa botão "Adicionar Mês" como invisível
        btnAdicionarMes.setVisible(false);
        btnAdicionarMes.setManaged(false);
    }
    
    private void atualizarVisibilidadeBotoes() {
        boolean temRegistros = !listaRegistros.isEmpty();
        int quantidadeRegistros = listaRegistros.size();
        boolean mesInteiroAdicionado = quantidadeRegistros >= 22; // Mês inteiro = ~22 dias úteis
        
        btnAdicionarMes.setVisible(temRegistros);
        btnAdicionarMes.setManaged(temRegistros);
        
        // Habilita botão de preencher horários apenas se tiver mês inteiro
        btnPreencherHorariosPena.setDisable(!mesInteiroAdicionado);
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
        
        // Busca última data cadastrada
        java.sql.Date ultimaData = RegistroDeTrabalhoDAO.buscarUltimaDataPorPena(pena.getIdPena());
        
        if (ultimaData != null) {
            // Se houver última data, sugere adicionar a partir do dia seguinte
            LocalDate dataInicio = ultimaData.toLocalDate().plusDays(1);
            if (listaRegistros.isEmpty()) {
                // Adiciona um dia começando do próximo dia após a última data
                RegistroTrabalhoTemp novoRegistro = new RegistroTrabalhoTemp(dataInicio);
                listaRegistros.add(novoRegistro);
                atualizarTotalHoras();
                atualizarVisibilidadeBotoes();
            }
        }
    }

    private void configurarTabela() {
        // Verifica se as colunas existem
        if (colData == null || colInicio == null || colAlmoco == null || 
            colVolta == null || colSaida == null || colHoras == null || colAcao == null) {
            System.err.println("ERRO: Uma ou mais colunas da tabela são null!");
            System.err.println("colData: " + (colData != null) + 
                             ", colInicio: " + (colInicio != null) +
                             ", colAlmoco: " + (colAlmoco != null) +
                             ", colVolta: " + (colVolta != null) +
                             ", colSaida: " + (colSaida != null) +
                             ", colHoras: " + (colHoras != null) +
                             ", colAcao: " + (colAcao != null));
            return;
        }
        
        // Coluna Data (DatePicker editável)
        colData.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getData()));
        colData.setCellFactory(column -> new TableCell<RegistroTrabalhoTemp, LocalDate>() {
            private DatePicker datePicker = new DatePicker();

            {
                datePicker.setConverter(new LocalDateStringConverter(dateFormatter, dateFormatter));
                datePicker.setOnAction(e -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        getTableRow().getItem().setData(datePicker.getValue());
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
        
        listaRegistros.add(new RegistroTrabalhoTemp(dataInicio));
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
                listaRegistros.add(new RegistroTrabalhoTemp(dataAtual));
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

    private void carregarInstituicoes() {
        List<Instituicao> lista = InstituicaoDAO.buscarTodasInstituicoes();
        instituicao.setItems(FXCollections.observableArrayList(lista));
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
            Instituicao inst = instituicao.getValue();

            if (user == null || pena == null || inst == null) {
                alert("Escolha usuário, pena e instituição.");
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
            int registrosDescartados = 0;
            
            for (RegistroTrabalhoTemp temp : listaRegistros) {
                // Só salva registros válidos (com data E horários completos)
                if (!temp.isValid()) {
                    registrosDescartados++;
                    continue;
                }
                
                RegistroDeTrabalho registro = new RegistroDeTrabalho();
                registro.setFkPenaId(pena.getIdPena());
                registro.setDataTrabalho(Date.valueOf(temp.getData()));
                registro.setHorasCumpridas(temp.getHorasCalculadas());
                registro.setAtividades(""); // Campo removido, sempre vazio
                registro.setHorarioInicio(Time.valueOf(temp.getHorarioInicio()));
                registro.setHorarioAlmoco(Time.valueOf(temp.getHorarioAlmoco()));
                registro.setHorarioVolta(Time.valueOf(temp.getHorarioVolta()));
                registro.setHorarioSaida(Time.valueOf(temp.getHorarioSaida()));
                
                registrosParaSalvar.add(registro);
            }
            
            if (registrosParaSalvar.isEmpty()) {
                alert("Nenhum registro válido para cadastrar. Preencha os horários.");
                return;
            }

            // Salva todos os registros em batch
            RegistroDeTrabalhoDAO dao = new RegistroDeTrabalhoDAO();
            boolean ok = dao.inserirBatch(registrosParaSalvar);
            
            String mensagem = ok 
                ? String.format("Registro gravado! %d dia(s) cadastrado(s).", registrosParaSalvar.size())
                : "Falha ao gravar.";
            
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
