package controller;

import java.awt.image.BufferedImage;
import java.io.IOException;

import dao.DadosFaciaisDAO;
import dao.InstituicaoDAO;
import dao.PenaDAO;
import dao.RegistroDeTrabalhoDAO;
import model.DadosFaciais;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Pena;
import model.RegistroDeTrabalho;
import model.Usuario;
import util.CodigoPenaUtil;
import util.ReconhecimentoFacial;

public class ConsultaApenadoController {

    @FXML
    private VBox paneReconhecimento;
    @FXML
    private ScrollPane paneDados;
    @FXML
    private Button btnCapturar;
    @FXML
    private Label lblStatusReconhecimento;
    @FXML
    private ProgressIndicator progressReconhecimento;
    
    // Campos de dados
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
    private Button btnVoltar, btnImprimir;
    @FXML
    private ImageView imgFoto;

    private Usuario usuario;
    private java.util.List<Pena> todasPenas;
    private DadosFaciaisDAO dadosFaciaisDAO;
    private ReconhecimentoFacial reconhecimentoFacial;

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

    @FXML
    private void initialize() {
        // Inicializa DAOs
        dadosFaciaisDAO = new DadosFaciaisDAO();
        reconhecimentoFacial = new ReconhecimentoFacial();
        reconhecimentoFacial.inicializar();
        
        // Configura visibilidade inicial
        paneReconhecimento.setVisible(true);
        paneDados.setVisible(false);
        
        // Configura botões
        btnVoltar.setOnAction(e -> voltar());
        btnImprimir.setOnAction(e -> imprimirDados());
        
        // Configura colunas da tabela
        configurarColunasTabela();
    }

    @FXML
    private void iniciarCaptura() {
        try {
            btnCapturar.setDisable(true);
            progressReconhecimento.setVisible(true);
            lblStatusReconhecimento.setText("Abrindo câmera...");
            
            // Carrega a view da câmera
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/mycompany/cpma/cameraView.fxml"));
            Parent root = loader.load();
            
            // Obtém o controller da câmera
            CameraController cameraController = loader.getController();
            
            // Cria uma nova janela para a câmera
            Stage cameraStage = new Stage();
            cameraStage.setTitle("Reconhecimento Facial");
            cameraStage.setScene(new Scene(root));
            
            // Define o que acontece quando a janela da câmera for fechada
            cameraStage.setOnHidden(e -> {
                // Pega a imagem capturada do CameraController
                BufferedImage imagemCapturada = cameraController.getImagemCapturada();
                if (imagemCapturada != null) {
                    Platform.runLater(() -> {
                        identificarUsuario(imagemCapturada);
                    });
                } else {
                    Platform.runLater(() -> {
                        lblStatusReconhecimento.setText("Captura cancelada. Tente novamente.");
                        btnCapturar.setDisable(false);
                        progressReconhecimento.setVisible(false);
                    });
                }
            });
            
            cameraStage.showAndWait(); // Mostra a janela da câmera e espera ela ser fechada
            
        } catch (IOException e) {
            mostrarErro("Erro", "Não foi possível abrir a câmera: " + e.getMessage());
            btnCapturar.setDisable(false);
            progressReconhecimento.setVisible(false);
            e.printStackTrace();
        }
    }

    /**
     * Identifica o usuário pela imagem capturada
     */
    private void identificarUsuario(BufferedImage imagem) {
        try {
            lblStatusReconhecimento.setText("Processando reconhecimento facial...");
            
            // Extrai descritores faciais da imagem
            String descritores = reconhecimentoFacial.extrairDescritoresFaciais(imagem);
            
            if (descritores == null || descritores.isEmpty() || descritores.equals("[]")) {
                lblStatusReconhecimento.setText("Não foi possível extrair descritores faciais. Tente novamente.");
                btnCapturar.setDisable(false);
                progressReconhecimento.setVisible(false);
                return;
            }
            
            // Busca usuário por similaridade facial (threshold de 0.7 = 70% de similaridade)
            usuario = dadosFaciaisDAO.buscarPorSimilaridadeFacial(descritores, 0.7);
            
            if (usuario != null) {
                // Usuário identificado com sucesso
                lblStatusReconhecimento.setText("Usuário identificado com sucesso!");
                exibirDadosUsuario();
            } else {
                // Usuário não encontrado
                mostrarErro("Usuário não encontrado", 
                    "Não foi possível identificar um usuário com base na imagem capturada.\n" +
                    "Verifique se você está cadastrado no sistema e tente novamente.");
                btnCapturar.setDisable(false);
                progressReconhecimento.setVisible(false);
            }
            
        } catch (Exception e) {
            mostrarErro("Erro na identificação", "Erro ao processar reconhecimento facial: " + e.getMessage());
            btnCapturar.setDisable(false);
            progressReconhecimento.setVisible(false);
            e.printStackTrace();
        }
    }

    /**
     * Exibe os dados do usuário identificado
     */
    private void exibirDadosUsuario() {
        if (usuario == null) {
            return;
        }
        
        // Preenche os campos
        preencherCampos();
        
        // Carrega a foto
        carregarFoto();
        
        // Carrega os registros
        carregarRegistros();
        
        // Alterna para a tela de dados
        paneReconhecimento.setVisible(false);
        paneDados.setVisible(true);
    }

    /**
     * Preenche os campos com os dados do usuário
     */
    private void preencherCampos() {
        if (usuario == null) {
            return;
        }
        
        // Formata o CPF
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
    }

    /**
     * Preenche o ComboBox com os códigos das penas do usuário
     */
    private void preencherComboBoxCodigoPenas() {
        todasPenas = PenaDAO.buscarPenasPorUsuario(usuario.getIdUsuario());
        
        if (todasPenas == null || todasPenas.isEmpty()) {
            System.out.println("Nenhuma pena encontrada para o usuário");
            cmbCodigoPena.setItems(FXCollections.observableArrayList());
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
            String dataInicio = formatarData(pena.getDataInicio());
            String textoExibicao = codigo + " (" + dataInicio + ")";
            items.add(new PenaItem(pena.getIdPena(), textoExibicao, pena));
            numeroPena++;
        }
        
        cmbCodigoPena.setItems(FXCollections.observableArrayList(items));
        
        // Configura o StringConverter
        cmbCodigoPena.setConverter(new StringConverter<PenaItem>() {
            @Override
            public String toString(PenaItem item) {
                return item != null ? item.getCodigo() : "";
            }
            
            @Override
            public PenaItem fromString(String string) {
                return null;
            }
        });
        
        // Adiciona listener para quando a seleção mudar
        cmbCodigoPena.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                carregarRegistrosPorPena(newVal.getIdPena());
            }
        });
        
        // Seleciona a primeira pena (mais recente) por padrão
        if (!items.isEmpty()) {
            cmbCodigoPena.getSelectionModel().select(0);
        }
    }

    /**
     * Carrega os registros de trabalho
     */
    private void carregarRegistros() {
        PenaItem itemSelecionado = cmbCodigoPena.getSelectionModel().getSelectedItem();
        if (itemSelecionado != null) {
            carregarRegistrosPorPena(itemSelecionado.getIdPena());
        } else if (todasPenas != null && !todasPenas.isEmpty()) {
            carregarRegistrosPorPena(todasPenas.get(0).getIdPena());
        } else {
            tblRegistros.setItems(FXCollections.observableArrayList());
        }
    }

    /**
     * Carrega os registros de trabalho para uma pena específica
     */
    private void carregarRegistrosPorPena(int idPena) {
        if (tblRegistros == null || usuario == null) {
            return;
        }
        
        // Busca a pena selecionada
        Pena pena = PenaDAO.buscarPorId(idPena);
        if (pena == null) {
            tblRegistros.setItems(FXCollections.observableArrayList());
            return;
        }
        
        // Busca registros da pena selecionada
        var lista = RegistroDeTrabalhoDAO.buscarPorUsuarioEPena(usuario.getIdUsuario(), idPena);
        
        if (lista.isEmpty()) {
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
        
        // Cria os DTOs com o cálculo correto de horas restantes
        var tabela = new java.util.ArrayList<RegistroDTO>();
        double acumuladoParcial = 0;

        for (RegistroDeTrabalho r : lista) {
            if (r != null) {
                acumuladoParcial += r.getHorasCumpridas();
                double falta = Math.max(totPena - acumuladoParcial, 0);
                
                // Formata a data do registro
                String dataTrabalho = "";
                if (r.getDataTrabalho() != null) {
                    java.util.Date dataUtil = new java.util.Date(r.getDataTrabalho().getTime());
                    dataTrabalho = formatarData(dataUtil);
                }
                
                RegistroDTO dto = new RegistroDTO(
                        dataTrabalho,
                        String.format("%.2f", r.getHorasCumpridas()),
                        String.format("%.2f", falta),
                        instituicao);
                
                tabela.add(dto);
            }
        }
        
        // Configura os itens na tabela
        javafx.collections.ObservableList<RegistroDTO> items = FXCollections.observableArrayList(tabela);
        tblRegistros.setItems(items);
        tblRegistros.refresh();
    }

    /**
     * Carrega a foto do apenado do banco de dados (BLOB)
     */
    private void carregarFoto() {
        if (usuario == null || imgFoto == null) {
            return;
        }

        try {
            int idUsuario = usuario.getIdUsuario();
            
            DadosFaciais dadosFaciais = dadosFaciaisDAO.buscarPorUsuario(idUsuario);
            if (dadosFaciais != null && dadosFaciais.getImagemRosto() != null) {
                try {
                    // Converte o blob para BufferedImage
                    BufferedImage imagemDoBanco = reconhecimentoFacial.blobParaImagem(dadosFaciais.getImagemRosto());
                    if (imagemDoBanco != null) {
                        // Converte BufferedImage para Image JavaFX
                        Image imagePreview = converterBufferedImageParaImage(imagemDoBanco);
                        if (imagePreview != null && !imagePreview.isError()) {
                            imgFoto.setImage(imagePreview);
                            imgFoto.setFitWidth(140.0);
                            imgFoto.setFitHeight(140.0);
                            imgFoto.setPreserveRatio(true);
                            imgFoto.setSmooth(true);
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Erro ao carregar foto do banco de dados: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao carregar foto: " + e.getMessage());
        }
    }

    /**
     * Converte BufferedImage para Image JavaFX
     */
    private Image converterBufferedImageParaImage(BufferedImage bufferedImage) {
        if (bufferedImage == null) {
            return null;
        }
        try {
            java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
            boolean written = javax.imageio.ImageIO.write(bufferedImage, "png", outputStream);
            if (!written) {
                outputStream.reset();
                written = javax.imageio.ImageIO.write(bufferedImage, "jpg", outputStream);
            }
            if (!written) {
                return null;
            }
            byte[] bytes = outputStream.toByteArray();
            return new Image(new java.io.ByteArrayInputStream(bytes));
        } catch (Exception e) {
            System.err.println("Erro na conversão BufferedImage para Image: " + e.getMessage());
            return null;
        }
    }

    /**
     * Configura as colunas da tabela de registros
     */
    private void configurarColunasTabela() {
        if (colData == null || colCumprida == null || colFalta == null || colInst == null) {
            return;
        }
        
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
    }

    /**
     * Formata CPF para exibição (xxx.xxx.xxx-xx)
     */
    private String formatarCPF(String cpf) {
        if (cpf == null || cpf.trim().isEmpty()) {
            return "";
        }
        // Remove caracteres não numéricos
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");
        if (cpfLimpo.length() != 11) {
            return cpf; // Retorna o CPF original se não tiver 11 dígitos
        }
        return cpfLimpo.substring(0, 3) + "." + cpfLimpo.substring(3, 6) + "." + 
               cpfLimpo.substring(6, 9) + "-" + cpfLimpo.substring(9, 11);
    }

    /**
     * Formata data para exibição (dd/MM/yyyy)
     */
    private String formatarData(java.util.Date data) {
        if (data == null) {
            return "";
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(data);
    }

    @FXML
    private void voltar() {
        // Volta para a tela de reconhecimento
        paneDados.setVisible(false);
        paneReconhecimento.setVisible(true);
        usuario = null;
        btnCapturar.setDisable(false);
        progressReconhecimento.setVisible(false);
        lblStatusReconhecimento.setText("Aguardando captura...");
    }

    @FXML
    private void imprimirDados() {
        // Implementação simplificada - pode ser expandida
        mostrarInfo("Impressão", "Funcionalidade de impressão será implementada em breve.");
    }

    /**
     * Mostra mensagem de erro
     */
    private void mostrarErro(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    /**
     * Mostra mensagem de informação
     */
    private void mostrarInfo(String titulo, String mensagem) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensagem);
        alert.showAndWait();
    }

    /**
     * Record para representar um registro de trabalho na tabela
     */
    private record RegistroDTO(String data, String cumprida, String falta, String inst) {
    }
}

