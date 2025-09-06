package controller;

import dao.DisponibilidadeInstituicaoDAO;
import dao.InstituicaoDAO;
import dao.TipoInstituicaoDAO;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.DisponibilidadeInstituicao;
import model.Instituicao;
import model.TipoInstituicao;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CadastrarInstituicaoController {

    @FXML
 private ComboBox<Instituicao> comboInstituicoes;
    @FXML  private TextField nome, endereco, cidade, uf, bairro, cep, responsavel, telefone;
    @FXML
 private ComboBox<TipoInstituicao> comboTipo;
    @FXML  private TableView<DisponibilidadeInstituicao> tabelaHorarios;
    @FXML
 private TableColumn<DisponibilidadeInstituicao, String> colDia, colInicio1, colFim1, colInicio2, colFim2;
    @FXML  private Button btnCadastrar, btnCancelar;
    @FXML
 private Button btnAdicionarHorario, btnRemoverHorario;
    @FXML  private TableColumn<DisponibilidadeInstituicao, Void> colAcao;

    private boolean modoEdicao = false;
    private Instituicao instituicaoAtual;
    private final List<DisponibilidadeInstituicao> novasDisponibilidades = new ArrayList<>();
    private final List<DisponibilidadeInstituicao> disponibilidadesTemp = new ArrayList<>();

    @FXML
    public void initialize() {
        limitarUF();
        configurarListenersRemocaoErro();
        carregarTipos();
        configurarTabelaDisponibilidades();

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

        comboInstituicoes.setVisible(false);
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
            if (inst != null) 
        {
            preencherCampos(inst);
            }
        
        

    private void carregarInstituicoesNaComboBox() {
        List<Instituicao> lista = InstituicaoDAO.buscarTodasInstituicoes();
        comboInstituicoes.setItems(FXCollections.observableArrayList(lista));
        comboInstituicoes.setConverter(new StringConverter<>() {
            @Override  public String 
        
            toString(Instituicao i) {
                return i == null ? "" : i.getNome();
    }

    public Instituicao fromString(String s) {
                return null;
            }
        });  sta.isEmpty()) {

     comboInstituicoes.getSelectionModel().selectFirst();
            preencherCampos(comboInstituicoes.getValue());
        }
    }

    private void carregarTipos() {
        List<TipoInstituicao> tipos = TipoInstituicaoDAO.buscarTodos();
        tipos.add(new TipoInstituicao(-1, "Adicionar outros..."));
        comboTipo.setItems(FXCollections.observableArrayList(tipos));
        comboTipo.setConverter(new StringConverter<>() {
            @Override  public S

            ring toString(TipoInstituicao t) {
                return t == null ? "" : t.getTipo();
            }

            @Override
            public TipoInstituicao fromString(String s) {
         }
        }); 
             
            
    private void configurarTabelaDisponibilidades() {
        colDia.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getDiaSemana()));
        colInicio1.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHoraInicio1() != null ? d.getValue().getHoraInicio1().toString() : ""));
        colFim1.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHoraFim1() != null ? d.getValue().getHoraFim1().toString() : ""));
        colInicio2.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHoraInicio2() != null ? d.getValue().getHoraInicio2().toString() : ""));
        colFim2.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getHoraFim2() != null ? d.getValue().getHoraFim2().toString() : ""));
        colAcao.setCellFactory(tc -> new TableCell<>() {
            private final Button btn = new Button("🗑");

            {
                btn.setStyle("-fx-background-color: transparent; -fx-font-size: 14; -fx-cursor: hand;");
                btn.setOnAction(e -> {
                    DisponibilidadeInstituicao item = getTableView().getItems().get(getIndex());
                    novasDisponibilidades.remove(item);
                    tabelaHorarios.getItems().setAll(novasDisponibilidades);
                    tabelaHorarios.refresh();
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

        tabelaHorarios.setItems(FXCollections.observableArrayList(novasDisponibilidades));

        tabelaHorarios.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && !tabelaHorarios.getSelectionModel().isEmpty()) {
                novasDisponibilidades.remove(tabelaHorarios.getSelectionModel().getSelectedItem());
                tabelaHorarios.refresh();
            }
        });

        tabelaHorarios.setPlaceholder(new Label("Clique no botão abaixo para adicionar horários."));
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

        mostrarAlerta(ok ? "Sucesso" : "Erro", ok ?
         (modoEdicao ? "Instituição atualizada com sucesso." : "Instituição cadastrada com sucesso.")
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

        List<DisponibilidadeInstituicao> disps = DisponibilidadeInstituicaoDAO.buscarPorInstituicaoId(inst.getIdInstituicao());
        tabelaHorarios.setItems(FXCollections.observableArrayList(disps));
    }

    private void abrirCadastroDisponibilidadeTemp() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/cpma/cadastroDisponibilidade.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(loader.load()));
            CadastroDisponibilidadeContr
         oller controller = loader.getController();

            int tamanhoAntes = novasDisponibilidades.size();

            controller.setCallback(novasDisponibilidades::add);
            stage.setTitle("Nova Disponibilidade");
            stage.showAndWait();

            int tamanhoDepois = novasDisponibilidades.size();

            if (tamanhoDepois > tamanhoAntes) {
                tabelaHorarios.getItems().setAll(novasDisponibilidades);
                tabelaHorarios.refresh();
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
        } else {
            mostrarAlerta("Atenção", "Selecione um horário para remover.");
        }
    }

    private boolean validar() {
        boolean ok = true;
        if (nome.getText().trim().isEmpty())   marcarErro(nome);
 false;
                  i
                f (endereco.getText().trim().isEmpty()) {
            marcarErro(endereco
                );
            ok = false;
        }
        if (cidade.getText().trim().isEmpty()) {
      marcarErro(cidade);
            false;
        }
        i
           marcarErro(      ok
            
         = false;
        }
        if (bairok = f sponsavel.getText().trim().isEmpty()) {
             marcarErro(responsavel);
            ok = false;
        
       if (telefone.getText().trim().isEmpty()) {
    rcarErro(telefone);
            ok = false;
    rErro(comboTipo);
     ok = false;
             }
        return ok;  d marc
                r c.setStyle("-fx-border-color: red;");
         }

    private void limparErro(Control c) {
        c.setStyle("");
              private voi
            d limparErros() {
        limparErro(nome);  
            limparErro(endereco);
        limparErro(cidade)
            ;
        limparErro(uf);
        limparErro(bairro);
                     limparErro(responsavel);
        limpar limparErro(comboTipo);
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
            if (nv == null) 
            {
                ret
            
            urn;
            }
            if (!nv.matches("[a-zA-Z]*")) {
                uf.setText(ov); 
     

        }else if (nv.length() > 2) {
        f.setText(nv.sbstrin g
            
        
            
        
    }

    private void mostrarAlerta(String titulo, String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK) {
            {
                setTitle(titulo);
                      showAndWait();
            }
    

     
         
}
 