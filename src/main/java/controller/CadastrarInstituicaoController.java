package controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import dao.DisponibilidadeInstituicaoDAO;
import dao.InstituicaoDAO;
import dao.TipoInstituicaoDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.DisponibilidadeInstituicao;
import model.Instituicao;
import model.TipoInstituicao;

public class CadastrarInstituicaoController {

    @FXML
    private ComboBox<Instituicao> comboInstituicoes;
    @FXML
    private TextField nome, endereco, cidade, uf, bairro, cep, responsavel, telefone;
    @FXML
    private ComboBox<TipoInstituicao> comboTipo;
    @FXML
    private TableView<DisponibilidadeInstituicao> tabelaHorarios;
    @FXML
    private TableColumn<DisponibilidadeInstituicao, String> colDia, colInicio1, colFim1, colInicio2, colFim2;
    @FXML
    private Button btnCadastrar, btnCancelar;
    @FXML
    private Button btnAdicionarHorario, btnRemoverHorario;
    @FXML
    private Button btnBuscarCep;
    @FXML
    private Label lblStatusCep;
    @FXML
    private TableColumn<DisponibilidadeInstituicao, Void> colAcao;

    private boolean modoEdicao = false;
    private Instituicao instituicaoAtual;
    private final List<DisponibilidadeInstituicao> novasDisponibilidades = new ArrayList<>();
    private final List<DisponibilidadeInstituicao> disponibilidadesTemp = new ArrayList<>();

    @FXML
    public void initialize() {
        limitarUF();
        configurarListenersRemocaoErro();
        configurarBuscaCEP();
        carregarTipos();
        configurarTabelaDisponibilidades();
        // Configura altura inicial da tabela
        ajustarAlturaTabela();

        btnAdicionarHorario.setOnAction(e -> abrirCadastroDisponibilidadeTemp());
        btnRemoverHorario.setOnAction(e -> removerHorarioSelecionado());
        btnCadastrar.setOnAction(e -> salvar());
        btnCancelar.setOnAction(e -> fecharJanela());

        comboTipo.setOnAction(e -> {
            TipoInstituicao tipo = comboTipo.getValue();
            if (tipo != null && "Adicionar outros...".equals(tipo.getTipo())) {
                abrirCadastroTipoInstituicao();
                carregarTipos();
            }
        });

            // Máscara para telefone ((00) 00000-0000)
            telefone.setTextFormatter(new TextFormatter<>(change -> {
                String newText = change.getControlNewText();
                if (newText.length() <= 15) { // 11 dígitos + 4 caracteres especiais = 15 caracteres
                    return change;
                }
                return null;
            }));
    
            // Listener para aplicar máscara do telefone automaticamente
            telefone.textProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue.equals(oldValue)) {
                    String digitsOnly = newValue.replaceAll("[^0-9]", "");
                    if (digitsOnly.length() <= 11) {
                        String masked = aplicarMascaraTelefone(digitsOnly);
                        if (!masked.equals(newValue)) {
                            telefone.setText(masked);
                            telefone.positionCaret(masked.length());
                        }
                    }
                }
            });

        comboInstituicoes.setVisible(false);
    }
        /**
     * Aplica máscara no formato telefone ((00) 00000-0000)
     */
    private String aplicarMascaraTelefone(String digits) {
        if (digits == null || digits.isEmpty()) {
            return "";
        }

        // Remove caracteres não numéricos
        digits = digits.replaceAll("[^0-9]", "");

        // Aplica a máscara conforme o tamanho
        if (digits.length() <= 2) {
            return digits;
        } else if (digits.length() <= 6) {
            return "(" + digits.substring(0, 2) + ") " + digits.substring(2);
        } else if (digits.length() <= 10) {
            return "(" + digits.substring(0, 2) + ") " + digits.substring(2, 7) + "-" + digits.substring(7);
        } else {
            return "(" + digits.substring(0, 2) + ") " + digits.substring(2, 7) + "-" + digits.substring(7, 11);
        }
    }

    public void ativarModoEdicao() {
        modoEdicao = true;

        nome.setPrefWidth(257);
        nome.setLayoutX(30);
        comboInstituicoes.setPrefWidth(266);
        comboInstituicoes.setLayoutX(307);
        comboInstituicoes.setVisible(true);

        btnCadastrar.setText("Salvar alterações");

        carregarInstituicoesNaComboBox();
        comboInstituicoes.setOnAction(e -> {
            Instituicao inst = comboInstituicoes.getValue();
            if (inst != null) {
                preencherCampos(inst);
            }
        });
    }

    public void carregarInstituicaoParaEdicao(Instituicao instituicao) {
        modoEdicao = true;
        btnCadastrar.setText("Salvar alterações");
        
        // Não mostra o combobox, carrega diretamente a instituição
        comboInstituicoes.setVisible(false);
        
        if (instituicao != null) {
            preencherCampos(instituicao);
        }
    }

    private void carregarInstituicoesNaComboBox() {
        List<Instituicao> lista = InstituicaoDAO.buscarTodasInstituicoes();
        comboInstituicoes.setItems(FXCollections.observableArrayList(lista));
        comboInstituicoes.setConverter(new StringConverter<>() {
            @Override
            public String toString(Instituicao i) {
                return i == null ? "" : i.getNome();
            }

            @Override
            public Instituicao fromString(String s) {
                return null;
            }
        });

        if (!lista.isEmpty()) {
            comboInstituicoes.getSelectionModel().selectFirst();
            preencherCampos(comboInstituicoes.getValue());
        }
    }

    private void carregarTipos() {
        List<TipoInstituicao> tipos = TipoInstituicaoDAO.buscarTodos();
        tipos.add(new TipoInstituicao(-1, "Adicionar outros..."));
        comboTipo.setItems(FXCollections.observableArrayList(tipos));
        comboTipo.setConverter(new StringConverter<>() {
            @Override
            public String toString(TipoInstituicao t) {
                return t == null ? "" : t.getTipo();
            }

            @Override
            public TipoInstituicao fromString(String s) {
                return null;
            }
        });
    }

    private void configurarTabelaDisponibilidades() {
        // Configuração das colunas com formatação melhorada
        colDia.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDiaSemana()));
        colDia.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText("📅 " + item);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #1e40af;");
                }
            }
        });
        
        colInicio1.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHoraInicio1() != null ? formatarHora(d.getValue().getHoraInicio1()) : ""));
        colFim1.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHoraFim1() != null ? formatarHora(d.getValue().getHoraFim1()) : ""));
        colInicio2.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHoraInicio2() != null ? formatarHora(d.getValue().getHoraInicio2()) : ""));
        colFim2.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHoraFim2() != null ? formatarHora(d.getValue().getHoraFim2()) : ""));
        
        // Configuração das colunas de hora
        configurarColunaHora(colInicio1);
        configurarColunaHora(colFim1);
        configurarColunaHora(colInicio2);
        configurarColunaHora(colFim2);
        
        // Coluna de ação com botão melhorado
        colAcao.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("🗑️");

            {
                btn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 12px; -fx-font-weight: bold; -fx-cursor: hand; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-padding: 4px 8px;");
                btn.setOnAction(e -> {
                    DisponibilidadeInstituicao item = getTableView().getItems().get(getIndex());
                    novasDisponibilidades.remove(item);
                    atualizarTabela();
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

        // Configuração da tabela
        tabelaHorarios.setItems(FXCollections.observableArrayList(novasDisponibilidades));
        tabelaHorarios.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        // Auto-redimensionamento das colunas
        tabelaHorarios.widthProperty().addListener((obs, oldWidth, newWidth) -> {
            double tableWidth = newWidth.doubleValue();
            colDia.setPrefWidth(tableWidth * 0.25);
            colInicio1.setPrefWidth(tableWidth * 0.15);
            colFim1.setPrefWidth(tableWidth * 0.15);
            colInicio2.setPrefWidth(tableWidth * 0.15);
            colFim2.setPrefWidth(tableWidth * 0.15);
            colAcao.setPrefWidth(tableWidth * 0.15);
        });

        tabelaHorarios.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && !tabelaHorarios.getSelectionModel().isEmpty()) {
                novasDisponibilidades.remove(tabelaHorarios.getSelectionModel().getSelectedItem());
                atualizarTabela();
            }
        });

        tabelaHorarios.setPlaceholder(new Label("📅 Clique no botão abaixo para adicionar horários de funcionamento."));
    }
    
    /**
     * Configura uma coluna de hora com formatação especial
     */
    private void configurarColunaHora(TableColumn<DisponibilidadeInstituicao, String> coluna) {
        coluna.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.isEmpty()) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("🕐 " + item);
                    setStyle("-fx-font-weight: 500; -fx-text-fill: #059669;");
                }
            }
        });
    }
    
    /**
     * Formata LocalTime para string HH:mm
     */
    private String formatarHora(LocalTime hora) {
        if (hora == null) {
            return "";
        }
        return hora.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    /**
     * Atualiza a tabela de horários e ajusta a altura automaticamente
     */
    private void atualizarTabela() {
        tabelaHorarios.getItems().clear();
        tabelaHorarios.getItems().addAll(novasDisponibilidades);
        tabelaHorarios.refresh();
        
        // Ajusta a altura da tabela baseada no número de itens
        ajustarAlturaTabela();
    }
    
    /**
     * Ajusta a altura da tabela dinamicamente baseada no número de itens
     */
    private void ajustarAlturaTabela() {
        int numItens = novasDisponibilidades.size();
        
        if (numItens == 0) {
            // Altura mínima quando não há dados
            tabelaHorarios.setPrefHeight(150.0);
        } else {
            // Calcula altura baseada no número de itens
            // Cabeçalho (40px) + cada linha (35px) + bordas (10px)
            double alturaCalculada = 40 + (numItens * 35) + 10;
            
            // Define limites mínimos e máximos
            double alturaMinima = 150.0;
            double alturaMaxima = 400.0;
            
            // Aplica os limites
            double alturaFinal = Math.max(alturaMinima, Math.min(alturaCalculada, alturaMaxima));
            
            tabelaHorarios.setPrefHeight(alturaFinal);
            
            // Se exceder a altura máxima, habilita scroll
            if (alturaCalculada > alturaMaxima) {
                tabelaHorarios.setPrefHeight(alturaMaxima);
            }
        }
    }

    private void abrirCadastroTipoInstituicao() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/cpma/cadastroTipoInstituicao.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            stage.setTitle("Novo Tipo de Instituição");
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void abrirCadastroDisponibilidade(int idInstituicao) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/cpma/cadastroDisponibilidade.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            CadastroDisponibilidadeController controller = loader.getController();
            controller.setInstituicaoId(idInstituicao);
            stage.setTitle("Nova Disponibilidade");
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void salvar() {
        limparErros();
        if (!validar()) {
            mostrarAlerta("Atenção", "Preencha todos os campos obrigatórios corretamente.");
            return;
        }

        if (!modoEdicao && novasDisponibilidades.isEmpty()) {
            Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
            alerta.setTitle("Confirmação");
            alerta.setHeaderText("Nenhuma disponibilidade cadastrada.");
            alerta.setContentText("Deseja cadastrar a instituição mesmo sem horários disponíveis?");
            ButtonType sim = new ButtonType("Sim", ButtonBar.ButtonData.YES);
            ButtonType nao = new ButtonType("Não", ButtonBar.ButtonData.NO);
            alerta.getButtonTypes().setAll(sim, nao);

            if (alerta.showAndWait().orElse(nao) == nao) {
                return;
            }
        }

        Instituicao inst = modoEdicao ? instituicaoAtual : new Instituicao();
        inst.setNome(nome.getText().trim());
        inst.setEndereco(endereco.getText().trim());
        inst.setCidade(cidade.getText().trim());
        inst.setUf(uf.getText().trim().toUpperCase());
        inst.setBairro(bairro.getText().trim());
        inst.setCep(cep.getText().trim());
        inst.setResponsavel(responsavel.getText().trim());
        inst.setTelefone(telefone.getText().trim());
        inst.setTipo(comboTipo.getValue().getIdTipo());

        boolean ok;
        if (modoEdicao) {
            ok = InstituicaoDAO.atualizar(inst);
            if (ok) {
                // Remove horários antigos e adiciona os novos
                DisponibilidadeInstituicaoDAO.removerPorInstituicaoId(inst.getIdInstituicao());
                for (DisponibilidadeInstituicao d : novasDisponibilidades) {
                    d.setFkInstituicaoId(inst.getIdInstituicao());
                    DisponibilidadeInstituicaoDAO.inserir(d);
                }
            }
        } else {
            int novoId = InstituicaoDAO.inserirEPegarID(inst);
            ok = novoId != -1;
            if (ok) {
                for (DisponibilidadeInstituicao d : novasDisponibilidades) {
                    d.setFkInstituicaoId(novoId);
                    DisponibilidadeInstituicaoDAO.inserir(d);
                }
            }
        }

        mostrarAlerta(ok ? "Sucesso" : "Erro", ok
                ? (modoEdicao ? "Instituição atualizada com sucesso." : "Instituição cadastrada com sucesso.")
                : "Erro ao salvar instituição.");

        if (ok) {
            novasDisponibilidades.clear();
            tabelaHorarios.getItems().clear();
            limparCampos();
            fecharJanela();
        }
    }

    private void preencherCampos(Instituicao inst) {
        instituicaoAtual = inst;
        nome.setText(inst.getNome());
        endereco.setText(inst.getEndereco());
        cidade.setText(inst.getCidade());
        uf.setText(inst.getUf());
        bairro.setText(inst.getBairro());
        cep.setText(inst.getCep());
        responsavel.setText(inst.getResponsavel());
        telefone.setText(inst.getTelefone());

        comboTipo.getSelectionModel().select(TipoInstituicaoDAO.buscarPorId(inst.getTipo()));

        // Carrega os horários existentes
        List<DisponibilidadeInstituicao> disps = DisponibilidadeInstituicaoDAO.buscarPorInstituicaoId(inst.getIdInstituicao());
        
        // Limpa e recarrega a lista de disponibilidades
        novasDisponibilidades.clear();
        novasDisponibilidades.addAll(disps);
        
        // Atualiza a tabela
        tabelaHorarios.getItems().clear();
        tabelaHorarios.getItems().addAll(novasDisponibilidades);
        tabelaHorarios.refresh();
        
        // Ajusta a altura da tabela
        ajustarAlturaTabela();
    }

    private void abrirCadastroDisponibilidadeTemp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/cpma/cadastroDisponibilidade.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            CadastroDisponibilidadeController controller = loader.getController();

            int tamanhoAntes = novasDisponibilidades.size();

            controller.setCallback(novasDisponibilidades::add);
            stage.setTitle("Nova Disponibilidade");
            stage.showAndWait();

            int tamanhoDepois = novasDisponibilidades.size();

            if (tamanhoDepois > tamanhoAntes) {
                tabelaHorarios.getItems().setAll(novasDisponibilidades);
                tabelaHorarios.refresh();
                // Ajusta a altura da tabela automaticamente
                ajustarAlturaTabela();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removerHorarioSelecionado() {
        DisponibilidadeInstituicao selecionado = tabelaHorarios.getSelectionModel().getSelectedItem();
        if (selecionado != null) {
            novasDisponibilidades.remove(selecionado);
            tabelaHorarios.getItems().setAll(novasDisponibilidades);
            tabelaHorarios.refresh();
            // Ajusta a altura da tabela automaticamente
            ajustarAlturaTabela();
        } else {
            mostrarAlerta("Atenção", "Selecione um horário para remover.");
        }
    }

    private boolean validar() {
        boolean ok = true;
        if (nome.getText().trim().isEmpty()) {
            marcarErro(nome);
            ok = false;
        }
        if (endereco.getText().trim().isEmpty()) {
            marcarErro(endereco);
            ok = false;
        }
        if (cidade.getText().trim().isEmpty()) {
            marcarErro(cidade);
            ok = false;
        }
        if (!uf.getText().trim().matches("^[A-Z]{2}$")) {
            marcarErro(uf);
            ok = false;
        }
        if (bairro.getText().trim().isEmpty()) {
            marcarErro(bairro);
            ok = false;
        }
        if (responsavel.getText().trim().isEmpty()) {
            marcarErro(responsavel);
            ok = false;
        }
        if (telefone.getText().trim().isEmpty()) {
            marcarErro(telefone);
            ok = false;
        }
        if (comboTipo.getValue() == null || comboTipo.getValue().getIdTipo() == -1) {
            marcarErro(comboTipo);
            ok = false;
        }
        return ok;
    }

    private void marcarErro(Control c) {
        c.setStyle("-fx-border-color: red;");
    }

    private void limparErro(Control c) {
        c.setStyle("");
    }

    private void limparErros() {
        limparErro(nome);
        limparErro(endereco);
        limparErro(cidade);
        limparErro(uf);
        limparErro(bairro);
        limparErro(responsavel);
        limparErro(telefone);
        limparErro(comboTipo);
    }

    private void limparCampos() {
        nome.clear();
        endereco.clear();
        cidade.clear();
        uf.clear();
        bairro.clear();
        cep.clear();
        responsavel.clear();
        telefone.clear();
        comboTipo.getSelectionModel().clearSelection();
    }

    private void fecharJanela() {
        Stage stage = (Stage) btnCancelar.getScene().getWindow();
        stage.close();
    }

    private void configurarListenersRemocaoErro() {
        nome.textProperty().addListener((o, ov, nv) -> limparErro(nome));
        endereco.textProperty().addListener((o, ov, nv) -> limparErro(endereco));
        cidade.textProperty().addListener((o, ov, nv) -> limparErro(cidade));
        uf.textProperty().addListener((o, ov, nv) -> limparErro(uf));
        bairro.textProperty().addListener((o, ov, nv) -> limparErro(bairro));
        responsavel.textProperty().addListener((o, ov, nv) -> limparErro(responsavel));
        telefone.textProperty().addListener((o, ov, nv) -> limparErro(telefone));
    }

    private void limitarUF() {
        uf.textProperty().addListener((obs, ov, nv) -> {
            if (nv == null) {
                return;
            }
            if (!nv.matches("[a-zA-Z]*")) {
                uf.setText(ov);
            } else if (nv.length() > 2) {
                uf.setText(nv.substring(0, 2));
            }
        });
    }

    /**
     * Configura a busca de CEP
     */
    private void configurarBuscaCEP() {
        // Listener para busca automática quando o CEP perde o foco
        cep.focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal) { // Quando perde o foco
                String cepLimpo = cep.getText().replaceAll("\\D", "");
                if (cepLimpo.length() == 8) {
                    buscarCEPComTask(cepLimpo);
                }
            }
        });

        // Botão de busca manual
        btnBuscarCep.setOnAction(e -> {
            String cepLimpo = cep.getText().replaceAll("\\D", "");
            if (cepLimpo.length() == 8) {
                buscarCEPComTask(cepLimpo);
            } else {
                lblStatusCep.setText("CEP deve ter 8 dígitos");
                lblStatusCep.setStyle("-fx-text-fill: #ef4444;");
            }
        });

        // Formatação do CEP usando TextFormatter
        cep.setTextFormatter(new javafx.scene.control.TextFormatter<>(change -> {
            String newText = change.getControlNewText();
            if (newText.matches("\\d{0,5}-?\\d{0,3}")) {
                return change;
            }
            return null;
        }));
    }

    /**
     * Busca informações do CEP na API ViaCEP
     */
    private void buscarCEPComTask(String cep) {
        lblStatusCep.setText("Buscando CEP...");
        lblStatusCep.setStyle("-fx-text-fill: #06b6d4;");
        btnBuscarCep.setDisable(true);

        Task<JSONObject> task = new Task<>() {
            @Override
            protected JSONObject call() throws Exception {
                URL url = new URL("https://viacep.com.br/ws/" + cep + "/json/");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Falha : HTTP error code : " + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();

                return new JSONObject(sb.toString());
            }
        };

        task.setOnSucceeded(e -> {
            JSONObject json = task.getValue();
            if (json.has("erro")) {
                lblStatusCep.setText("CEP não encontrado");
                lblStatusCep.setStyle("-fx-text-fill: #ef4444;");
            } else {
                preencherCamposEndereco(json);
                lblStatusCep.setText("CEP encontrado com sucesso!");
                lblStatusCep.setStyle("-fx-text-fill: #10b981;");
            }
            btnBuscarCep.setDisable(false);
        });

        task.setOnFailed(e -> {
            lblStatusCep.setText("Erro ao buscar CEP");
            lblStatusCep.setStyle("-fx-text-fill: #ef4444;");
            btnBuscarCep.setDisable(false);
            mostrarAlerta("Erro de Rede", "Não foi possível conectar à API do ViaCEP.");
        });

        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Preenche os campos de endereço com os dados do CEP
     */
    private void preencherCamposEndereco(JSONObject json) {
        endereco.setText(json.optString("logradouro", ""));
        bairro.setText(json.optString("bairro", ""));
        cidade.setText(json.optString("localidade", ""));
        uf.setText(json.optString("uf", ""));
    }

    private void mostrarAlerta(String titulo, String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK) {
            {
                setTitle(titulo);
                setHeaderText(null);
                showAndWait();
            }
        };
    }
}
