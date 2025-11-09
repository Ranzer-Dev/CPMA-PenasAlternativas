package controller;

import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import dao.InstituicaoDAO;
import dao.PenaDAO;
import dao.RegistroDeTrabalhoDAO;
import model.Pena;
import model.RegistroDeTrabalho;
import model.Usuario;
import util.CodigoPenaUtil;
import java.util.List;

public class DetalheApenadoController {

    @FXML
    private TextField txtNome, txtCpf, txtDataNasc,
            txtEndereco, txtBairro, txtCidade, txtUf,
            txtNac, txtDataCad, txtFone;
    @FXML
    private ComboBox<PenaItem> cmbCodigoPena;
    @FXML
    private TableView<RegistroDTO> tblRegistros;
    @FXML
    private TableColumn<RegistroDTO, String> colData, colInst;
    @FXML
    private TableColumn<RegistroDTO, String> colCumprida, colFalta;
    @FXML
    private Button btnVoltar, btnEditar, btnImprimir;

    private Usuario usuario;
    private List<Pena> todasPenas;
    
    /**
     * Classe interna para representar um item do ComboBox de penas
     */
    public static class PenaItem {
        private final int idPena;
        private final String codigo;
        private final Pena pena;
        
        public PenaItem(int idPena, String codigo, Pena pena) {
            this.idPena = idPena;
            this.codigo = codigo;
            this.pena = pena;
        }
        
        public int getIdPena() { return idPena; }
        public String getCodigo() { return codigo; }
        public Pena getPena() { return pena; }
        
        @Override
        public String toString() {
            return codigo;
        }
    }

    public void setUsuario(Usuario u) {
        if (u == null) {
            System.err.println("Erro: Usuario é null em setUsuario");
            return;
        }
        this.usuario = u;
        
        // Verifica se os campos FXML estão inicializados
        if (txtNome == null) {
            System.err.println("Aviso: Campos FXML ainda não foram inicializados. Aguardando initialize()...");
            // Os campos serão preenchidos no initialize() ou em uma chamada posterior
            return;
        }
        
        preencherCampos();
        carregarRegistros();
    }

    private void preencherCampos() {
        if (usuario == null) {
            System.err.println("Erro: Usuario é null em preencherCampos");
            return;
        }
        // Formata o CPF com pontos e hífens se não estiver formatado
        String cpfFormatado = formatarCPF(usuario.getCpf());
        txtCpf.setText(cpfFormatado != null ? cpfFormatado : "");
        
        txtNome.setText(usuario.getNome() != null ? usuario.getNome() : "");
        
        // Formata a data de nascimento
        String dataNasc = formatarData(usuario.getDataNascimento());
        txtDataNasc.setText(dataNasc);
        
        // Preenche o ComboBox de códigos de penas
        preencherComboBoxCodigoPenas();
        
        txtEndereco.setText(usuario.getEndereco() != null ? usuario.getEndereco() : "");
        txtBairro.setText(usuario.getBairro() != null ? usuario.getBairro() : "");
        txtCidade.setText(usuario.getCidade() != null ? usuario.getCidade() : "");
        txtUf.setText(usuario.getUf() != null ? usuario.getUf() : "");
        txtNac.setText(usuario.getNacionalidade() != null ? usuario.getNacionalidade() : "");
        
        // Formata a data de cadastro
        String dataCad = formatarData(usuario.getCriadoEm());
        txtDataCad.setText(dataCad);
        
        txtFone.setText(usuario.getTelefone() != null ? usuario.getTelefone() : "");
        // foto? -> use ImageView.setImage()
    }
    
    /**
     * Formata CPF para exibição (xxx.xxx.xxx-xx)
     */
    private String formatarCPF(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return "";
        }
        // Remove caracteres não numéricos
        String cpfNumerico = cpf.replaceAll("[^0-9]", "");
        
        // Se tem 11 dígitos, formata
        if (cpfNumerico.length() == 11) {
            return cpfNumerico.substring(0, 3) + "." + 
                   cpfNumerico.substring(3, 6) + "." + 
                   cpfNumerico.substring(6, 9) + "-" + 
                   cpfNumerico.substring(9, 11);
        }
        // Se já está formatado ou tem tamanho diferente, retorna como está
        return cpf;
    }
    
    /**
     * Formata data para exibição (dd/MM/yyyy)
     */
    private String formatarData(java.util.Date data) {
        if (data == null) {
            return "";
        }
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(data);
        } catch (Exception e) {
            return "";
        }
    }
    
    /**
     * Preenche o ComboBox com as penas disponíveis do usuário.
     * Cada item mostra o código da pena e permite selecionar para ver os registros.
     */
    private void preencherComboBoxCodigoPenas() {
        if (cmbCodigoPena == null) {
            System.err.println("ComboBox cmbCodigoPena é null");
            return;
        }
        
        // Busca todas as penas do usuário (ativas e inativas)
        todasPenas = PenaDAO.buscarPenasPorUsuario(usuario.getIdUsuario());
        
        if (todasPenas.isEmpty()) {
            cmbCodigoPena.setItems(FXCollections.observableArrayList());
            System.out.println("Nenhuma pena encontrada para o usuário");
            return;
        }
        
        // Ordena as penas por data de início (mais recente primeiro)
        todasPenas.sort((p1, p2) -> {
            if (p1.getDataInicio() == null && p2.getDataInicio() == null) return 0;
            if (p1.getDataInicio() == null) return 1;
            if (p2.getDataInicio() == null) return -1;
            return p2.getDataInicio().compareTo(p1.getDataInicio());
        });
        
        // Cria itens para o ComboBox
        var items = new java.util.ArrayList<PenaItem>();
        int numeroPena = 1;
        for (Pena pena : todasPenas) {
            String codigo = CodigoPenaUtil.calcularCodigoAtual(numeroPena);
            // Adiciona informações adicionais no texto de exibição
            String dataInicio = formatarData(pena.getDataInicio());
            String textoExibicao = codigo + " (" + dataInicio + ")";
            items.add(new PenaItem(pena.getIdPena(), textoExibicao, pena));
            numeroPena++;
        }
        
        cmbCodigoPena.setItems(FXCollections.observableArrayList(items));
        
        // Configura o StringConverter para mostrar o código no ComboBox
        cmbCodigoPena.setConverter(new StringConverter<PenaItem>() {
            @Override
            public String toString(PenaItem item) {
                return item != null ? item.getCodigo() : "";
            }
            
            @Override
            public PenaItem fromString(String string) {
                return null; // Não usado para ComboBox simples
            }
        });
        
        // Adiciona listener para quando a seleção mudar (deve ser ANTES de selecionar)
        cmbCodigoPena.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                System.out.println("Pena selecionada: ID=" + newVal.getIdPena() + ", Código=" + newVal.getCodigo());
                carregarRegistrosPorPena(newVal.getIdPena());
            }
        });
        
        // Seleciona a primeira pena (mais recente) por padrão
        // Isso vai disparar o listener e carregar os registros automaticamente
        if (!items.isEmpty()) {
            cmbCodigoPena.getSelectionModel().select(0);
        }
        
        System.out.println("ComboBox preenchido com " + items.size() + " penas");
    }

    /**
     * Carrega os registros quando uma pena é selecionada no ComboBox.
     * Se nenhuma pena for especificada, carrega da primeira pena (mais recente).
     */
    private void carregarRegistros() {
        PenaItem itemSelecionado = cmbCodigoPena.getSelectionModel().getSelectedItem();
        if (itemSelecionado != null) {
            carregarRegistrosPorPena(itemSelecionado.getIdPena());
        } else if (todasPenas != null && !todasPenas.isEmpty()) {
            // Se nenhuma pena está selecionada, carrega a primeira (mais recente)
            carregarRegistrosPorPena(todasPenas.get(0).getIdPena());
        } else {
            tblRegistros.setItems(FXCollections.observableArrayList());
        }
    }
    
    /**
     * Carrega os registros de trabalho para uma pena específica.
     * 
     * @param idPena ID da pena para buscar os registros
     */
    private void carregarRegistrosPorPena(int idPena) {
        System.out.println("=== Carregando registros de trabalho ===");
        System.out.println("ID do usuário: " + usuario.getIdUsuario());
        System.out.println("ID da pena selecionada: " + idPena);
        
        if (tblRegistros == null) {
            System.err.println("Erro: tblRegistros é null");
            return;
        }
        
        // Garante que as colunas estão configuradas
        configurarColunasTabela();
        
        // Busca a pena selecionada
        Pena pena = PenaDAO.buscarPorId(idPena);
        if (pena == null) {
            System.out.println("Pena não encontrada: ID=" + idPena);
            tblRegistros.setItems(FXCollections.observableArrayList());
            return;
        }
        
        System.out.println("Pena encontrada: ID=" + pena.getIdPena() + ", Horas totais=" + pena.getHorasTotais());
        
        // Busca registros apenas da pena selecionada
        var lista = RegistroDeTrabalhoDAO.buscarPorUsuarioEPena(usuario.getIdUsuario(), idPena);
        
        System.out.println("Registros encontrados no banco: " + lista.size());
        
        if (lista.isEmpty()) {
            System.out.println("Lista de registros está vazia para esta pena");
            tblRegistros.setItems(FXCollections.observableArrayList());
            return;
        }
        
        double totPena = pena.getHorasTotais();
        
        // Ordena os registros por data
        lista.sort((r1, r2) -> {
            if (r1.getDataTrabalho() == null && r2.getDataTrabalho() == null) return 0;
            if (r1.getDataTrabalho() == null) return 1;
            if (r2.getDataTrabalho() == null) return -1;
            return r1.getDataTrabalho().compareTo(r2.getDataTrabalho());
        });
        
        // Busca o nome da instituição
        String inst = InstituicaoDAO.buscarNomePorId(pena.getFkInstituicaoIdInstituicao());
        String instituicao = inst != null ? inst : "";
        System.out.println("Instituição: " + instituicao);
        
        // Calcula o total acumulado de horas
        double acumulado = 0;
        for (RegistroDeTrabalho r : lista) {
            if (r != null) {
                acumulado += r.getHorasCumpridas();
            }
        }
        
        System.out.println("Total de horas acumuladas: " + acumulado);
        System.out.println("Total de horas da pena: " + totPena);
        
        // Cria os DTOs com o cálculo correto de horas restantes
        var tabela = new java.util.ArrayList<RegistroDTO>();
        double acumuladoParcial = 0;

        for (RegistroDeTrabalho r : lista){
            if (r != null) {
                acumuladoParcial += r.getHorasCumpridas();
                double falta = Math.max(totPena - acumuladoParcial, 0);
                
                System.out.println("Processando registro: Data=" + r.getDataTrabalho() + 
                                 ", Horas=" + r.getHorasCumpridas() + 
                                 ", Acumulado=" + acumuladoParcial + 
                                 ", Falta=" + falta);

                // Formata a data do registro
                String dataTrabalho = "";
                if (r.getDataTrabalho() != null) {
                    // Converte java.sql.Date para java.util.Date
                    java.util.Date dataUtil = new java.util.Date(r.getDataTrabalho().getTime());
                    dataTrabalho = formatarData(dataUtil);
                }
                
                RegistroDTO dto = new RegistroDTO(
                        dataTrabalho,
                        String.format("%.2f", r.getHorasCumpridas()),
                        String.format("%.2f", falta),
                        instituicao);
                
                tabela.add(dto);
                System.out.println("DTO criado: Data=" + dto.data + ", Cumprida=" + dto.cumprida + ", Falta=" + dto.falta + ", Inst=" + dto.inst);
            }
        }

        System.out.println("Total de registros DTO criados: " + tabela.size());
        
        if (tabela.isEmpty()) {
            System.out.println("Nenhum registro DTO foi criado, limpando tabela");
            tblRegistros.setItems(FXCollections.observableArrayList());
            return;
        }
        
        System.out.println("Configurando items da tabela...");

        // Garante que as colunas estão configuradas ANTES de adicionar os dados
        configurarColunasTabela();
        
        // Limpa a tabela primeiro
        tblRegistros.getItems().clear();
        
        // Cria a lista observável e adiciona os itens
        javafx.collections.ObservableList<RegistroDTO> items = FXCollections.observableArrayList(tabela);
        
        // Configura os itens na tabela
        tblRegistros.setItems(items);
        
        // Força atualização da tabela
        tblRegistros.refresh();
        
        System.out.println("Items na tabela após setItems: " + tblRegistros.getItems().size());
        
        // Verifica se a tabela está visível
        System.out.println("Tabela visível: " + tblRegistros.isVisible());
        System.out.println("Tabela desabilitada: " + tblRegistros.isDisable());
        
        // Debug: imprime alguns itens da tabela
        if (tblRegistros.getItems().size() > 0) {
            System.out.println("Primeiro item da tabela: " + tblRegistros.getItems().get(0));
        }
    }
    private record RegistroDTO(String data, String cumprida, String falta, String inst) {

    }

    @FXML
    private void initialize() {
        System.out.println("=== Initialize DetalheApenadoController ===");
        System.out.println("tblRegistros é null? " + (tblRegistros == null));
        System.out.println("colData é null? " + (colData == null));
        System.out.println("colCumprida é null? " + (colCumprida == null));
        System.out.println("colFalta é null? " + (colFalta == null));
        System.out.println("colInst é null? " + (colInst == null));
        
        // Configura os botões
        btnVoltar.setOnAction(e -> ((Stage) btnVoltar.getScene().getWindow()).close());
        btnEditar.setOnAction(e -> {/* abrir tela de edição (futuramente) */
        });
        btnImprimir.setOnAction(e -> imprimir());
        
        // Configura as colunas da tabela imediatamente (sempre, mesmo que vazio)
        configurarColunasTabela();
        
        // Se o usuário já foi definido antes do initialize(), preenche os campos agora
        if (usuario != null) {
            System.out.println("Usuario já definido, preenchendo campos...");
            preencherCampos();
            // carregarRegistros() será chamado automaticamente quando a pena for selecionada no ComboBox
        }
    }
    
    /**
     * Configura as colunas da tabela de registros
     */
    private void configurarColunasTabela() {
        if (colData == null || colCumprida == null || colFalta == null || colInst == null) {
            System.err.println("Aviso: Algumas colunas da tabela são null");
            System.err.println("colData: " + colData);
            System.err.println("colCumprida: " + colCumprida);
            System.err.println("colFalta: " + colFalta);
            System.err.println("colInst: " + colInst);
            return;
        }
        
        // Configura usando PropertyValueFactory pode não funcionar com records
        // Então usamos lambda expressions que acessam os campos do record
        colData.setCellValueFactory(cellData -> {
            RegistroDTO item = cellData.getValue();
            if (item != null && item.data != null) {
                return new javafx.beans.property.SimpleStringProperty(item.data);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        colCumprida.setCellValueFactory(cellData -> {
            RegistroDTO item = cellData.getValue();
            if (item != null && item.cumprida != null) {
                return new javafx.beans.property.SimpleStringProperty(item.cumprida);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        colFalta.setCellValueFactory(cellData -> {
            RegistroDTO item = cellData.getValue();
            if (item != null && item.falta != null) {
                return new javafx.beans.property.SimpleStringProperty(item.falta);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        colInst.setCellValueFactory(cellData -> {
            RegistroDTO item = cellData.getValue();
            if (item != null && item.inst != null) {
                return new javafx.beans.property.SimpleStringProperty(item.inst);
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        
        System.out.println("Colunas da tabela configuradas com sucesso");
        
        // Verifica se as colunas têm cell value factory configurado
        System.out.println("colData cellValueFactory: " + (colData.getCellValueFactory() != null));
        System.out.println("colCumprida cellValueFactory: " + (colCumprida.getCellValueFactory() != null));
        System.out.println("colFalta cellValueFactory: " + (colFalta.getCellValueFactory() != null));
        System.out.println("colInst cellValueFactory: " + (colInst.getCellValueFactory() != null));
    }

    private void imprimir() {
        new Alert(Alert.AlertType.INFORMATION, "Funcionalidade de impressão ainda não implementada.",
                ButtonType.OK).showAndWait();
    }

    private void alerta(String titulo, String mensagem) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensagem, ButtonType.OK);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }

}
