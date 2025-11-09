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
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.print.PageOrientation;
import javafx.stage.FileChooser;
import javafx.print.Printer;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;

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
        if (usuario == null) {
            alerta("Erro", "Nenhum usuário selecionado para impressão.");
            return;
        }
        
        PenaItem penaSelecionada = cmbCodigoPena.getSelectionModel().getSelectedItem();
        if (penaSelecionada == null) {
            alerta("Aviso", "Selecione uma pena para imprimir os detalhes.");
            return;
        }
        
        try {
            // Verifica se há impressoras disponíveis
            boolean temImpressora = !Printer.getAllPrinters().isEmpty();
            
            if (temImpressora) {
                // Tenta imprimir normalmente
                Node documento = criarDocumentoImpressao(penaSelecionada);
                PrinterJob job = PrinterJob.createPrinterJob();
                
                if (job != null) {
                    boolean showDialog = job.showPrintDialog(btnImprimir.getScene().getWindow());
                    
                    if (showDialog) {
                        Printer printer = job.getPrinter();
                        javafx.print.PageLayout pageLayout = printer.createPageLayout(
                            javafx.print.Paper.A4,
                            PageOrientation.PORTRAIT,
                            javafx.print.Printer.MarginType.DEFAULT
                        );
                        
                        boolean success = job.printPage(pageLayout, documento);
                        
                        if (success) {
                            job.endJob();
                            alerta("Sucesso", "Documento enviado para impressão com sucesso!");
                        } else {
                            alerta("Erro", "Falha ao imprimir o documento.");
                        }
                    }
                } else {
                    // Se o job é null, gera PDF
                    gerarPDF(penaSelecionada);
                }
            } else {
                // Não há impressoras disponíveis, gera PDF
                gerarPDF(penaSelecionada);
            }
        } catch (Exception e) {
            e.printStackTrace();
            alerta("Erro", "Erro ao preparar impressão: " + e.getMessage());
            // Em caso de erro, tenta gerar PDF como fallback
            try {
                gerarPDF(penaSelecionada);
            } catch (Exception ex) {
                alerta("Erro", "Erro ao gerar PDF: " + ex.getMessage());
            }
        }
    }
    
    /**
     * Gera um arquivo PDF com os detalhes do apenado
     */
    private void gerarPDF(PenaItem penaItem) {
        try {
            // Abre diálogo para salvar arquivo
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Salvar PDF");
            fileChooser.setInitialFileName("Detalhe_Apenado_" + usuario.getNome().replaceAll("[^a-zA-Z0-9]", "_") + ".pdf");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF Files", "*.pdf")
            );
            
            File arquivo = fileChooser.showSaveDialog(btnImprimir.getScene().getWindow());
            
            if (arquivo == null) {
                // Usuário cancelou
                return;
            }
            
            // Cria o PDF
            criarPDF(penaItem, arquivo);
            
            alerta("Sucesso", "PDF gerado com sucesso!\nArquivo salvo em: " + arquivo.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            alerta("Erro", "Erro ao gerar PDF: " + e.getMessage());
        }
    }
    
    /**
     * Cria o arquivo PDF com todas as informações
     */
    private void criarPDF(PenaItem penaItem, File arquivo) throws IOException {
        PDDocument document = new PDDocument();
        PDPageContentStream contentStream = null;
        
        try {
            float margin = 50;
            float pageWidth = PDRectangle.A4.getWidth();
            float pageHeight = PDRectangle.A4.getHeight();
            float yPosition = pageHeight - margin;
            float lineHeight = 15;
            float sectionSpacing = 25;
            
            // Fontes
            PDType1Font fontTitle = PDType1Font.HELVETICA_BOLD;
            PDType1Font fontSubtitle = PDType1Font.HELVETICA;
            PDType1Font fontBold = PDType1Font.HELVETICA_BOLD;
            PDType1Font fontNormal = PDType1Font.HELVETICA;
            
            // Cria primeira página
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            contentStream = new PDPageContentStream(document, page);
            
            // Cabeçalho
            yPosition = adicionarTextoCentrado(contentStream, "CPMA", fontTitle, 20, yPosition, pageWidth);
            yPosition -= lineHeight;
            yPosition = adicionarTextoCentrado(contentStream, "Central de Penas e Medidas Alternativas", fontSubtitle, 12, yPosition, pageWidth);
            yPosition -= lineHeight * 2;
            yPosition = adicionarTextoCentrado(contentStream, "Detalhes do Apenado", fontTitle, 16, yPosition, pageWidth);
            yPosition -= sectionSpacing;
            
            // Linha separadora
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(pageWidth - margin, yPosition);
            contentStream.stroke();
            yPosition -= sectionSpacing;
            
            // Informações Pessoais
            yPosition = adicionarTexto(contentStream, "INFORMAÇÕES PESSOAIS", fontBold, 14, margin, yPosition);
            yPosition -= lineHeight;
            yPosition = adicionarLinhaPDF(contentStream, "Nome Completo:", txtNome.getText(), fontBold, fontNormal, margin, yPosition);
            yPosition = adicionarLinhaPDF(contentStream, "CPF:", txtCpf.getText(), fontBold, fontNormal, margin, yPosition);
            yPosition = adicionarLinhaPDF(contentStream, "Data de Nascimento:", txtDataNasc.getText(), fontBold, fontNormal, margin, yPosition);
            yPosition = adicionarLinhaPDF(contentStream, "Telefone:", txtFone.getText(), fontBold, fontNormal, margin, yPosition);
            yPosition = adicionarLinhaPDF(contentStream, "Nacionalidade:", txtNac.getText(), fontBold, fontNormal, margin, yPosition);
            yPosition = adicionarLinhaPDF(contentStream, "Data de Cadastro:", txtDataCad.getText(), fontBold, fontNormal, margin, yPosition);
            yPosition -= sectionSpacing;
            
            // Endereço
            yPosition = adicionarTexto(contentStream, "ENDEREÇO", fontBold, 14, margin, yPosition);
            yPosition -= lineHeight;
            yPosition = adicionarLinhaPDF(contentStream, "Endereço:", txtEndereco.getText(), fontBold, fontNormal, margin, yPosition);
            yPosition = adicionarLinhaPDF(contentStream, "Bairro:", txtBairro.getText(), fontBold, fontNormal, margin, yPosition);
            yPosition = adicionarLinhaPDF(contentStream, "Cidade:", txtCidade.getText() + " - " + txtUf.getText(), fontBold, fontNormal, margin, yPosition);
            yPosition -= sectionSpacing;
            
            // Informações da Pena
            Pena pena = penaItem.getPena();
            String nomeInstituicao = InstituicaoDAO.buscarNomePorId(pena.getFkInstituicaoIdInstituicao());
            String codigoPena = penaItem.getCodigo();
            if (codigoPena.contains(" (")) {
                codigoPena = codigoPena.substring(0, codigoPena.indexOf(" ("));
            }
            
            yPosition = adicionarTexto(contentStream, "INFORMAÇÕES DA PENA", fontBold, 14, margin, yPosition);
            yPosition -= lineHeight;
            yPosition = adicionarLinhaPDF(contentStream, "Código da Pena:", codigoPena, fontBold, fontNormal, margin, yPosition);
            yPosition = adicionarLinhaPDF(contentStream, "Tipo:", pena.getTipoPena() != null ? pena.getTipoPena() : "N/A", fontBold, fontNormal, margin, yPosition);
            yPosition = adicionarLinhaPDF(contentStream, "Data de Início:", formatarData(pena.getDataInicio()), fontBold, fontNormal, margin, yPosition);
            yPosition = adicionarLinhaPDF(contentStream, "Data de Término:", formatarData(pena.getDataTermino()), fontBold, fontNormal, margin, yPosition);
            yPosition = adicionarLinhaPDF(contentStream, "Horas Totais:", String.format("%.2f", pena.getHorasTotais()), fontBold, fontNormal, margin, yPosition);
            yPosition = adicionarLinhaPDF(contentStream, "Instituição:", nomeInstituicao != null ? nomeInstituicao : "N/A", fontBold, fontNormal, margin, yPosition);
            if (pena.getDescricao() != null && !pena.getDescricao().trim().isEmpty()) {
                yPosition = adicionarLinhaPDF(contentStream, "Descrição:", pena.getDescricao(), fontBold, fontNormal, margin, yPosition);
            }
            yPosition -= sectionSpacing;
            
            // Registros de Trabalho
            yPosition = adicionarTexto(contentStream, "REGISTROS DE TRABALHO", fontBold, 14, margin, yPosition);
            yPosition -= lineHeight;
            
            List<RegistroDeTrabalho> registros = RegistroDeTrabalhoDAO.buscarPorUsuarioEPena(usuario.getIdUsuario(), penaItem.getIdPena());
            
            if (registros.isEmpty()) {
                yPosition = adicionarTexto(contentStream, "Nenhum registro de trabalho encontrado.", fontNormal, 11, margin, yPosition);
            } else {
                // Ordena por data
                registros.sort((r1, r2) -> {
                    if (r1.getDataTrabalho() == null && r2.getDataTrabalho() == null) return 0;
                    if (r1.getDataTrabalho() == null) return 1;
                    if (r2.getDataTrabalho() == null) return -1;
                    return r1.getDataTrabalho().compareTo(r2.getDataTrabalho());
                });
                
                // Cabeçalho da tabela
                float tableMargin = margin;
                float col1Width = 100;
                float col2Width = 120;
                float col3Width = 120;
                
                yPosition = adicionarTexto(contentStream, "Data", fontBold, 11, tableMargin, yPosition);
                yPosition = adicionarTexto(contentStream, "Horas Cumpridas", fontBold, 11, tableMargin + col1Width, yPosition);
                yPosition = adicionarTexto(contentStream, "Horas Restantes", fontBold, 11, tableMargin + col1Width + col2Width, yPosition);
                yPosition -= lineHeight;
                
                // Linha separadora
                contentStream.moveTo(margin, yPosition);
                contentStream.lineTo(pageWidth - margin, yPosition);
                contentStream.stroke();
                yPosition -= lineHeight;
                
                double totPena = pena.getHorasTotais();
                double acumulado = 0;
                
                for (RegistroDeTrabalho reg : registros) {
                    // Verifica se precisa de nova página
                    if (yPosition < margin + 100) {
                        contentStream.close();
                        PDPage newPage = new PDPage(PDRectangle.A4);
                        document.addPage(newPage);
                        contentStream = new PDPageContentStream(document, newPage);
                        yPosition = pageHeight - margin;
                    }
                    
                    acumulado += reg.getHorasCumpridas();
                    double falta = Math.max(totPena - acumulado, 0);
                    
                    String data = formatarData(reg.getDataTrabalho());
                    String horas = String.format("%.2f", reg.getHorasCumpridas());
                    String horasFalta = String.format("%.2f", falta);
                    
                    yPosition = adicionarTexto(contentStream, data, fontNormal, 10, tableMargin, yPosition);
                    yPosition = adicionarTexto(contentStream, horas, fontNormal, 10, tableMargin + col1Width, yPosition);
                    yPosition = adicionarTexto(contentStream, horasFalta, fontNormal, 10, tableMargin + col1Width + col2Width, yPosition);
                    yPosition -= lineHeight;
                }
                
                // Totais
                yPosition -= lineHeight;
                yPosition = adicionarTexto(contentStream, "Total de Horas Cumpridas: " + String.format("%.2f", acumulado), fontBold, 11, margin, yPosition);
                yPosition = adicionarTexto(contentStream, "Total de Horas da Pena: " + String.format("%.2f", totPena), fontBold, 11, margin, yPosition);
                yPosition = adicionarTexto(contentStream, "Horas Restantes: " + String.format("%.2f", Math.max(totPena - acumulado, 0)), fontBold, 11, margin, yPosition);
            }
            
            // Rodapé
            yPosition = margin + 30;
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(pageWidth - margin, yPosition);
            contentStream.stroke();
            yPosition -= lineHeight;
            
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
            String dataImpressao = sdf.format(new java.util.Date());
            adicionarTextoCentrado(contentStream, "Documento gerado em: " + dataImpressao, fontNormal, 9, yPosition, pageWidth);
            
            contentStream.close();
            document.save(arquivo);
        } finally {
            if (contentStream != null) {
                try {
                    contentStream.close();
                } catch (IOException e) {
                    // Ignora erro ao fechar
                }
            }
            if (document != null) {
                document.close();
            }
        }
    }
    
    /**
     * Adiciona texto ao PDF
     */
    private float adicionarTexto(PDPageContentStream contentStream, String texto, PDType1Font font, float fontSize, float x, float y) throws IOException {
        if (texto == null || texto.trim().isEmpty()) {
            texto = "N/A";
        }
        contentStream.setFont(font, fontSize);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(texto);
        contentStream.endText();
        return y - fontSize - 5;
    }
    
    /**
     * Adiciona texto centralizado ao PDF
     */
    private float adicionarTextoCentrado(PDPageContentStream contentStream, String texto, PDType1Font font, float fontSize, float y, float pageWidth) throws IOException {
        if (texto == null || texto.trim().isEmpty()) {
            texto = "N/A";
        }
        // Calcula largura do texto aproximada (em pontos)
        float textWidth = texto.length() * fontSize * 0.6f; // Aproximação: cada caractere ocupa ~0.6 * fontSize
        float x = (pageWidth - textWidth) / 2;
        return adicionarTexto(contentStream, texto, font, fontSize, x, y);
    }
    
    /**
     * Adiciona uma linha com label e valor ao PDF
     */
    private float adicionarLinhaPDF(PDPageContentStream contentStream, String label, String valor, PDType1Font fontLabel, PDType1Font fontValor, float x, float y) throws IOException {
        float labelWidth = 150;
        float currentY = y;
        
        // Adiciona label
        contentStream.setFont(fontLabel, 11);
        contentStream.beginText();
        contentStream.newLineAtOffset(x, currentY);
        contentStream.showText(label);
        contentStream.endText();
        
        // Adiciona valor
        if (valor == null || valor.trim().isEmpty()) {
            valor = "N/A";
        }
        contentStream.setFont(fontValor, 11);
        contentStream.beginText();
        contentStream.newLineAtOffset(x + labelWidth, currentY);
        contentStream.showText(valor);
        contentStream.endText();
        
        return currentY - 15;
    }
    
    /**
     * Cria o documento Node para impressão com todas as informações do apenado
     */
    private Node criarDocumentoImpressao(PenaItem penaItem) {
        VBox documento = new VBox(20);
        documento.setPadding(new Insets(40, 50, 40, 50));
        documento.setStyle("-fx-background-color: white;");
        
        // Cabeçalho
        VBox cabecalho = criarCabecalho();
        documento.getChildren().add(cabecalho);
        
        // Informações pessoais
        VBox infoPessoais = criarSecaoInformacoesPessoais();
        documento.getChildren().add(infoPessoais);
        
        // Informações de endereço
        VBox infoEndereco = criarSecaoEndereco();
        documento.getChildren().add(infoEndereco);
        
        // Informações da pena
        VBox infoPena = criarSecaoPena(penaItem);
        documento.getChildren().add(infoPena);
        
        // Registros de trabalho
        VBox registros = criarSecaoRegistros(penaItem.getIdPena());
        documento.getChildren().add(registros);
        
        // Rodapé
        VBox rodape = criarRodape();
        documento.getChildren().add(rodape);
        
        return documento;
    }
    
    private VBox criarCabecalho() {
        VBox cabecalho = new VBox(10);
        cabecalho.setAlignment(Pos.CENTER);
        cabecalho.setPadding(new Insets(0, 0, 20, 0));
        
        Text titulo = new Text("CPMA");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        titulo.setFill(Color.DARKBLUE);
        
        Text subtitulo = new Text("Central de Penas e Medidas Alternativas");
        subtitulo.setFont(Font.font("Arial", 12));
        subtitulo.setFill(Color.GRAY);
        
        Text tituloDoc = new Text("Detalhes do Apenado");
        tituloDoc.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        tituloDoc.setFill(Color.BLACK);
        
        // Linha separadora
        Line linha = new Line(0, 0, 500, 0);
        linha.setStroke(Color.GRAY);
        linha.setStrokeWidth(1);
        
        cabecalho.getChildren().addAll(titulo, subtitulo, tituloDoc, linha);
        return cabecalho;
    }
    
    private VBox criarSecaoInformacoesPessoais() {
        VBox secao = new VBox(15);
        secao.setPadding(new Insets(10, 0, 10, 0));
        
        Text titulo = new Text("INFORMAÇÕES PESSOAIS");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titulo.setUnderline(true);
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 0, 0));
        
        // Nome
        adicionarLinhaGrid(grid, 0, "Nome Completo:", txtNome.getText());
        // CPF
        adicionarLinhaGrid(grid, 1, "CPF:", txtCpf.getText());
        // Data de Nascimento
        adicionarLinhaGrid(grid, 2, "Data de Nascimento:", txtDataNasc.getText());
        // Telefone
        adicionarLinhaGrid(grid, 3, "Telefone:", txtFone.getText());
        // Nacionalidade
        adicionarLinhaGrid(grid, 4, "Nacionalidade:", txtNac.getText());
        // Data de Cadastro
        adicionarLinhaGrid(grid, 5, "Data de Cadastro:", txtDataCad.getText());
        
        secao.getChildren().addAll(titulo, grid);
        return secao;
    }
    
    private VBox criarSecaoEndereco() {
        VBox secao = new VBox(15);
        secao.setPadding(new Insets(10, 0, 10, 0));
        
        Text titulo = new Text("ENDEREÇO");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titulo.setUnderline(true);
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 0, 0));
        
        // Endereço
        adicionarLinhaGrid(grid, 0, "Endereço:", txtEndereco.getText());
        // Bairro
        adicionarLinhaGrid(grid, 1, "Bairro:", txtBairro.getText());
        // Cidade / UF
        adicionarLinhaGrid(grid, 2, "Cidade:", txtCidade.getText() + " - " + txtUf.getText());
        
        secao.getChildren().addAll(titulo, grid);
        return secao;
    }
    
    private VBox criarSecaoPena(PenaItem penaItem) {
        VBox secao = new VBox(15);
        secao.setPadding(new Insets(10, 0, 10, 0));
        
        Text titulo = new Text("INFORMAÇÕES DA PENA");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titulo.setUnderline(true);
        
        Pena pena = penaItem.getPena();
        String nomeInstituicao = InstituicaoDAO.buscarNomePorId(pena.getFkInstituicaoIdInstituicao());
        
        GridPane grid = new GridPane();
        grid.setHgap(20);
        grid.setVgap(10);
        grid.setPadding(new Insets(10, 0, 0, 0));
        
        // Código da Pena
        String codigoPena = penaItem.getCodigo();
        // Remove a data do código se estiver presente (formato: "1a (dd/MM/yyyy)")
        if (codigoPena.contains(" (")) {
            codigoPena = codigoPena.substring(0, codigoPena.indexOf(" ("));
        }
        adicionarLinhaGrid(grid, 0, "Código da Pena:", codigoPena);
        // Tipo da Pena
        adicionarLinhaGrid(grid, 1, "Tipo:", pena.getTipoPena() != null ? pena.getTipoPena() : "N/A");
        // Data de Início
        adicionarLinhaGrid(grid, 2, "Data de Início:", formatarData(pena.getDataInicio()));
        // Data de Término
        adicionarLinhaGrid(grid, 3, "Data de Término:", formatarData(pena.getDataTermino()));
        // Horas Totais
        adicionarLinhaGrid(grid, 4, "Horas Totais:", String.format("%.2f", pena.getHorasTotais()));
        // Instituição
        adicionarLinhaGrid(grid, 5, "Instituição:", nomeInstituicao != null ? nomeInstituicao : "N/A");
        // Descrição
        if (pena.getDescricao() != null && !pena.getDescricao().trim().isEmpty()) {
            adicionarLinhaGrid(grid, 6, "Descrição:", pena.getDescricao());
        }
        
        secao.getChildren().addAll(titulo, grid);
        return secao;
    }
    
    private VBox criarSecaoRegistros(int idPena) {
        VBox secao = new VBox(15);
        secao.setPadding(new Insets(10, 0, 10, 0));
        
        Text titulo = new Text("REGISTROS DE TRABALHO");
        titulo.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titulo.setUnderline(true);
        
        // Busca os registros
        List<RegistroDeTrabalho> registros = RegistroDeTrabalhoDAO.buscarPorUsuarioEPena(usuario.getIdUsuario(), idPena);
        Pena pena = PenaDAO.buscarPorId(idPena);
        
        if (registros.isEmpty()) {
            Text semRegistros = new Text("Nenhum registro de trabalho encontrado.");
            semRegistros.setFont(Font.font("Arial", 12));
            semRegistros.setFill(Color.GRAY);
            secao.getChildren().addAll(titulo, semRegistros);
            return secao;
        }
        
        // Ordena por data
        registros.sort((r1, r2) -> {
            if (r1.getDataTrabalho() == null && r2.getDataTrabalho() == null) return 0;
            if (r1.getDataTrabalho() == null) return 1;
            if (r2.getDataTrabalho() == null) return -1;
            return r1.getDataTrabalho().compareTo(r2.getDataTrabalho());
        });
        
        // Cria tabela de registros
        GridPane tabela = new GridPane();
        tabela.setHgap(15);
        tabela.setVgap(8);
        tabela.setPadding(new Insets(10, 0, 0, 0));
        
        // Cabeçalho da tabela
        Text headerData = new Text("Data");
        headerData.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        Text headerHoras = new Text("Horas Cumpridas");
        headerHoras.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        Text headerFalta = new Text("Horas Restantes");
        headerFalta.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        tabela.add(headerData, 0, 0);
        tabela.add(headerHoras, 1, 0);
        tabela.add(headerFalta, 2, 0);
        
        double totPena = pena != null ? pena.getHorasTotais() : 0;
        double acumulado = 0;
        
        int linha = 1;
        for (RegistroDeTrabalho reg : registros) {
            acumulado += reg.getHorasCumpridas();
            double falta = Math.max(totPena - acumulado, 0);
            
            String data = formatarData(reg.getDataTrabalho());
            String horas = String.format("%.2f", reg.getHorasCumpridas());
            String horasFalta = String.format("%.2f", falta);
            
            Text txtData = new Text(data);
            txtData.setFont(Font.font("Arial", 11));
            Text txtHoras = new Text(horas);
            txtHoras.setFont(Font.font("Arial", 11));
            Text txtFalta = new Text(horasFalta);
            txtFalta.setFont(Font.font("Arial", 11));
            
            tabela.add(txtData, 0, linha);
            tabela.add(txtHoras, 1, linha);
            tabela.add(txtFalta, 2, linha);
            linha++;
        }
        
        // Total acumulado
        VBox totais = new VBox(5);
        totais.setPadding(new Insets(15, 0, 0, 0));
        
        Text totalAcumulado = new Text("Total de Horas Cumpridas: " + String.format("%.2f", acumulado));
        totalAcumulado.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        Text totalPena = new Text("Total de Horas da Pena: " + String.format("%.2f", totPena));
        totalPena.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        Text totalFalta = new Text("Horas Restantes: " + String.format("%.2f", Math.max(totPena - acumulado, 0)));
        totalFalta.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        
        totais.getChildren().addAll(totalAcumulado, totalPena, totalFalta);
        
        secao.getChildren().addAll(titulo, tabela, totais);
        return secao;
    }
    
    private VBox criarRodape() {
        VBox rodape = new VBox(10);
        rodape.setPadding(new Insets(20, 0, 0, 0));
        rodape.setAlignment(Pos.CENTER);
        
        // Linha separadora
        Line linha = new Line(0, 0, 500, 0);
        linha.setStroke(Color.GRAY);
        linha.setStrokeWidth(1);
        
        // Data de impressão
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        String dataImpressao = sdf.format(new java.util.Date());
        Text txtData = new Text("Documento impresso em: " + dataImpressao);
        txtData.setFont(Font.font("Arial", 10));
        txtData.setFill(Color.GRAY);
        
        rodape.getChildren().addAll(linha, txtData);
        return rodape;
    }
    
    private void adicionarLinhaGrid(GridPane grid, int linha, String label, String valor) {
        Text lbl = new Text(label);
        lbl.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        lbl.setWrappingWidth(150);
        
        Text val = new Text(valor != null && !valor.trim().isEmpty() ? valor : "N/A");
        val.setFont(Font.font("Arial", 11));
        val.setWrappingWidth(300);
        
        grid.add(lbl, 0, linha);
        grid.add(val, 1, linha);
    }

    private void alerta(String titulo, String mensagem) {
        Alert a = new Alert(Alert.AlertType.INFORMATION, mensagem, ButtonType.OK);
        a.setTitle(titulo);
        a.setHeaderText(null);
        a.showAndWait();
    }

}
