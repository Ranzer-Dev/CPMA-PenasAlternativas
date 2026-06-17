package controller;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import dao.CodigoAcessoApenadoDAO;
import dao.DadosFaciaisDAO;
import dao.InstituicaoDAO;
import dao.LogAcessoApenadoDAO;
import dao.PenaDAO;
import dao.RegistroDeTrabalhoDAO;
import dao.UsuarioDAO;
import model.CodigoAcessoApenado;
import model.DadosFaciais;
import model.LogAcessoApenado;
import util.CodigoAcessoUtil;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import model.Pena;
import model.RegistroDeTrabalho;
import model.Usuario;
import util.CodigoPenaUtil;
import util.ReconhecimentoFacial;

public class ConsultaApenadoController {

    private enum ModoFacial {
        NENHUM,
        /** Segunda etapa obrigatória após o código ser aceito. */
        OBRIGATORIO_APOS_CODIGO,
        /** Alternativa quando o código não pôde ser validado. */
        FALLBACK
    }

    @FXML
    private VBox paneCodigo;
    @FXML
    private VBox paneReconhecimento;
    @FXML
    private ScrollPane paneDados;
    @FXML
    private Button btnCapturar;
    @FXML
    private Button btnConfirmarCodigo;
    @FXML
    private Button btnUsarFacial;
    @FXML
    private Button btnVoltarParaCodigo;
    @FXML
    private TextField txtEntradaCodigo;
    @FXML
    private Label lblStatusCodigo;
    @FXML
    private Label lblStatusReconhecimento;
    @FXML
    private Text txtInstrucaoFacial;
    @FXML
    private ProgressIndicator progressReconhecimento;
    
    // Campos de dados
    @FXML
    private TextField txtNome, txtCpf, txtDataNasc;
    @FXML
    private TextField txtCodigoPena;
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
    private Pena penaAtual;
    private CodigoAcessoApenado codigoAcessoUtilizado;
    private DadosFaciaisDAO dadosFaciaisDAO;
    private ReconhecimentoFacial reconhecimentoFacial;

    /** Só true após falha na validação do código; libera o fallback facial. */
    private boolean fallbackFacialHabilitado;
    private ModoFacial modoFacialAtual = ModoFacial.NENHUM;

    @FXML
    private void initialize() {
        dadosFaciaisDAO = new DadosFaciaisDAO();
        reconhecimentoFacial = new ReconhecimentoFacial();
        reconhecimentoFacial.inicializar();

        // Acesso por código é o caminho primário; reconhecimento facial é fallback
        if (paneCodigo != null) {
            paneCodigo.setVisible(true);
        }
        if (paneReconhecimento != null) {
            paneReconhecimento.setVisible(false);
        }
        paneDados.setVisible(false);

        btnVoltar.setOnAction(e -> voltar());
        btnImprimir.setOnAction(e -> imprimirDados());

        resetarFluxoCodigoInicial();
        configurarEntradaCodigo();
        configurarColunasTabela();
        configurarTelaCheiaTotem();
    }

    /**
     * Reinicia o fluxo: código é sempre o primeiro passo; facial fica oculto
     * até uma tentativa de código falhar na validação.
     */
    private void resetarFluxoCodigoInicial() {
        fallbackFacialHabilitado = false;
        modoFacialAtual = ModoFacial.NENHUM;
        usuario = null;
        codigoAcessoUtilizado = null;
        if (btnUsarFacial != null) {
            btnUsarFacial.setVisible(false);
            btnUsarFacial.setManaged(false);
        }
    }

    /**
     * Exibe o botão de fallback facial somente depois que o código foi
     * rejeitado (inválido, expirado ou já utilizado).
     */
    private void habilitarFallbackFacial() {
        fallbackFacialHabilitado = true;
        if (btnUsarFacial != null) {
            btnUsarFacial.setVisible(true);
            btnUsarFacial.setManaged(true);
        }
    }

    /**
     * Restringe o campo do código a 8 dígitos numéricos e permite enviar com Enter.
     */
    private void configurarEntradaCodigo() {
        if (txtEntradaCodigo == null) {
            return;
        }
        txtEntradaCodigo.textProperty().addListener((obs, antigo, novo) -> {
            if (novo == null) return;
            String limpo = novo.replaceAll("[^0-9]", "");
            if (limpo.length() > CodigoAcessoUtil.TAMANHO_CODIGO) {
                limpo = limpo.substring(0, CodigoAcessoUtil.TAMANHO_CODIGO);
            }
            if (!limpo.equals(novo)) {
                txtEntradaCodigo.setText(limpo);
            }
        });
        txtEntradaCodigo.setOnAction(e -> confirmarCodigo());
    }

    private void configurarTelaCheiaTotem() {
        Platform.runLater(() -> {
            if (paneReconhecimento == null || paneReconhecimento.getScene() == null) {
                return;
            }
            Stage stage = (Stage) paneReconhecimento.getScene().getWindow();
            if (stage != null) {
                stage.setMaximized(true);
                stage.setFullScreen(true);
                stage.setFullScreenExitHint("");
            }
        });
    }

    /**
     * Valida o código digitado pelo apenado. Se válido, encaminha para a etapa
     * obrigatória de reconhecimento facial (o código só é consumido após o rosto
     * confirmar a identidade). Caso inválido, registra a tentativa e libera fallback.
     */
    @FXML
    private void confirmarCodigo() {
        if (txtEntradaCodigo == null) {
            return;
        }
        String entrada = CodigoAcessoUtil.normalizar(txtEntradaCodigo.getText());
        if (entrada.length() != CodigoAcessoUtil.TAMANHO_CODIGO) {
            atualizarStatusCodigo("Digite os 8 dígitos do código.", true);
            return;
        }

        atualizarStatusCodigo("Validando código...", false);
        if (btnConfirmarCodigo != null) {
            btnConfirmarCodigo.setDisable(true);
        }

        CodigoAcessoApenado codigo = CodigoAcessoApenadoDAO.buscarAtivoPorCodigo(entrada);

        if (codigo == null) {
            LogAcessoApenadoDAO.registrar(null, null, LogAcessoApenado.METODO_CODIGO, false, entrada,
                    "Código inexistente, expirado ou já utilizado.");
            atualizarStatusCodigo(
                    "Código inválido ou expirado. Procure o delegado para um novo código.", true);
            habilitarFallbackFacial();
            txtEntradaCodigo.clear();
            txtEntradaCodigo.requestFocus();
            if (btnConfirmarCodigo != null) {
                btnConfirmarCodigo.setDisable(false);
            }
            return;
        }

        if (codigo.estaExpirado()) {
            CodigoAcessoApenadoDAO.marcarExpirados();
            LogAcessoApenadoDAO.registrar(codigo.getFkUsuarioIdUsuario(), codigo.getIdCodigoAcesso(),
                    LogAcessoApenado.METODO_CODIGO, false, entrada, "Código expirado.");
            atualizarStatusCodigo(
                    "Este código expirou. Solicite um novo ao delegado.", true);
            habilitarFallbackFacial();
            txtEntradaCodigo.clear();
            if (btnConfirmarCodigo != null) {
                btnConfirmarCodigo.setDisable(false);
            }
            return;
        }

        Usuario usuarioCodigo = UsuarioDAO.buscarPorId(codigo.getFkUsuarioIdUsuario());
        if (usuarioCodigo == null) {
            LogAcessoApenadoDAO.registrar(codigo.getFkUsuarioIdUsuario(), codigo.getIdCodigoAcesso(),
                    LogAcessoApenado.METODO_CODIGO, false, entrada, "Usuário do código não localizado.");
            atualizarStatusCodigo(
                    "Não foi possível localizar seus dados. Procure o delegado.", true);
            habilitarFallbackFacial();
            if (btnConfirmarCodigo != null) {
                btnConfirmarCodigo.setDisable(false);
            }
            return;
        }

        DadosFaciais dadosFaciais = dadosFaciaisDAO.buscarPorUsuario(usuarioCodigo.getIdUsuario());
        if (dadosFaciais == null
                || dadosFaciais.getDescritoresFaciais() == null
                || dadosFaciais.getDescritoresFaciais().isEmpty()
                || "[]".equals(dadosFaciais.getDescritoresFaciais())) {
            LogAcessoApenadoDAO.registrar(usuarioCodigo.getIdUsuario(), codigo.getIdCodigoAcesso(),
                    LogAcessoApenado.METODO_CODIGO, false, entrada,
                    "Código válido, mas apenado sem foto facial cadastrada.");
            atualizarStatusCodigo(
                    "Código aceito, porém não há foto facial cadastrada. Procure o delegado.", true);
            habilitarFallbackFacial();
            if (btnConfirmarCodigo != null) {
                btnConfirmarCodigo.setDisable(false);
            }
            return;
        }

        LogAcessoApenadoDAO.registrar(usuarioCodigo.getIdUsuario(), codigo.getIdCodigoAcesso(),
                LogAcessoApenado.METODO_CODIGO, true, entrada,
                "Código validado; encaminhado para reconhecimento facial obrigatório.");

        usuario = usuarioCodigo;
        codigoAcessoUtilizado = codigo;
        modoFacialAtual = ModoFacial.OBRIGATORIO_APOS_CODIGO;
        atualizarStatusCodigo("", false);
        txtEntradaCodigo.clear();
        if (btnConfirmarCodigo != null) {
            btnConfirmarCodigo.setDisable(false);
        }
        irParaFacialObrigatorio();
    }

    /** Etapa 2: reconhecimento facial obrigatório após código aceito. */
    private void irParaFacialObrigatorio() {
        configurarInstrucaoFacial(
                "Código validado. Posicione seu rosto na câmera para confirmar sua identidade.");
        if (btnVoltarParaCodigo != null) {
            btnVoltarParaCodigo.setText("Voltar ao código");
        }
        mostrarTelaFacial();
    }

    /**
     * Alterna para a tela de reconhecimento facial (fallback).
     * Só é permitido após falha na validação do código — o código é sempre o primeiro passo.
     */
    @FXML
    private void irParaFacial() {
        if (!fallbackFacialHabilitado) {
            atualizarStatusCodigo(
                    "Primeiro informe e valide o código de 8 dígitos entregue pelo delegado.", true);
            if (txtEntradaCodigo != null) {
                txtEntradaCodigo.requestFocus();
            }
            return;
        }

        LogAcessoApenadoDAO.registrar(null, null, LogAcessoApenado.METODO_FACIAL, false, null,
                "Apenado optou pelo reconhecimento facial após falha na validação do código.");

        modoFacialAtual = ModoFacial.FALLBACK;
        usuario = null;
        codigoAcessoUtilizado = null;
        configurarInstrucaoFacial(
                "Alternativa: o código não foi validado. Posicione seu rosto na câmera "
                        + "para tentar o reconhecimento facial.");
        if (btnVoltarParaCodigo != null) {
            btnVoltarParaCodigo.setText("Voltar e tentar o código novamente");
        }
        mostrarTelaFacial();
    }

    private void mostrarTelaFacial() {
        if (paneCodigo != null) {
            paneCodigo.setVisible(false);
        }
        if (paneReconhecimento != null) {
            paneReconhecimento.setVisible(true);
        }
        paneDados.setVisible(false);
        if (lblStatusReconhecimento != null) {
            lblStatusReconhecimento.setText("Aguardando captura...");
        }
        if (btnCapturar != null) {
            btnCapturar.setDisable(false);
        }
        if (progressReconhecimento != null) {
            progressReconhecimento.setVisible(false);
        }
    }

    private void configurarInstrucaoFacial(String texto) {
        if (txtInstrucaoFacial != null) {
            txtInstrucaoFacial.setText(texto);
        }
    }

    /**
     * Volta da tela de reconhecimento facial para a entrada de código.
     */
    @FXML
    private void voltarParaCodigo() {
        if (paneReconhecimento != null) {
            paneReconhecimento.setVisible(false);
        }
        paneDados.setVisible(false);
        if (paneCodigo != null) {
            paneCodigo.setVisible(true);
        }
        if (modoFacialAtual == ModoFacial.OBRIGATORIO_APOS_CODIGO) {
            usuario = null;
            codigoAcessoUtilizado = null;
            modoFacialAtual = ModoFacial.NENHUM;
        } else if (modoFacialAtual == ModoFacial.FALLBACK) {
            modoFacialAtual = ModoFacial.NENHUM;
        }
        atualizarStatusCodigo("", false);
        if (txtEntradaCodigo != null) {
            txtEntradaCodigo.clear();
            txtEntradaCodigo.requestFocus();
        }
    }

    private void atualizarStatusCodigo(String mensagem, boolean erro) {
        if (lblStatusCodigo == null) {
            return;
        }
        lblStatusCodigo.setText(mensagem == null ? "" : mensagem);
        lblStatusCodigo.setStyle(erro ? "-fx-text-fill: #b91c1c;" : "");
    }

    /**
     * Confirma que o rosto capturado pertence ao apenado do código validado.
     * Só então consome o código e libera a consulta dos registros.
     */
    private void processarFacialObrigatorioAposCodigo(String descritores, double threshold) {
        if (usuario == null || codigoAcessoUtilizado == null) {
            lblStatusReconhecimento.setText("Sessão expirada. Volte e informe o código novamente.");
            btnCapturar.setDisable(false);
            progressReconhecimento.setVisible(false);
            return;
        }

        int idUsuario = usuario.getIdUsuario();
        System.out.println("\n[ETAPA 3] Verificando rosto do apenado vinculado ao código (ID " + idUsuario + ")...");

        boolean rostoConfere = dadosFaciaisDAO.verificarRostoDoUsuario(idUsuario, descritores, threshold);

        if (!rostoConfere) {
            LogAcessoApenadoDAO.registrar(idUsuario, codigoAcessoUtilizado.getIdCodigoAcesso(),
                    LogAcessoApenado.METODO_FACIAL, false, codigoAcessoUtilizado.getCodigo(),
                    "Rosto não confere com o titular do código validado.");
            mostrarErro("Identidade não confirmada",
                    "O rosto capturado não corresponde ao apenado deste código.\n\n"
                            + "Verifique se está usando o código correto e tente novamente.\n"
                            + "Se o problema persistir, procure o delegado.");
            lblStatusReconhecimento.setText("Rosto não confere. Tente novamente.");
            btnCapturar.setDisable(false);
            progressReconhecimento.setVisible(false);
            return;
        }

        boolean marcou = CodigoAcessoApenadoDAO.marcarComoUsado(codigoAcessoUtilizado.getIdCodigoAcesso());
        if (!marcou) {
            LogAcessoApenadoDAO.registrar(idUsuario, codigoAcessoUtilizado.getIdCodigoAcesso(),
                    LogAcessoApenado.METODO_FACIAL, false, codigoAcessoUtilizado.getCodigo(),
                    "Rosto confirmado, mas código já havia sido utilizado.");
            mostrarErro("Código indisponível",
                    "Este código já foi utilizado. Solicite um novo código ao delegado.");
            lblStatusReconhecimento.setText("Código já utilizado.");
            btnCapturar.setDisable(false);
            progressReconhecimento.setVisible(false);
            voltarParaCodigo();
            return;
        }

        LogAcessoApenadoDAO.registrar(idUsuario, codigoAcessoUtilizado.getIdCodigoAcesso(),
                LogAcessoApenado.METODO_FACIAL, true, codigoAcessoUtilizado.getCodigo(),
                "Rosto confirmado após validação do código; acesso liberado.");

        lblStatusReconhecimento.setText("Identidade confirmada!");
        modoFacialAtual = ModoFacial.NENHUM;
        codigoAcessoUtilizado = null;
        exibirDadosUsuario();
    }

    @FXML
    private void iniciarCaptura() {
        System.out.println("\n" + "=".repeat(80));
        System.out.println("🔔 [ConsultaApenadoController] iniciarCaptura() chamado");
        System.out.println("=".repeat(80));
        
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
            
            cameraStage.showAndWait();
            
            // Fallback: tenta pegar a imagem diretamente após showAndWait retornar
            BufferedImage imagemFallback = cameraController.getImagemCapturada();
            if (imagemFallback != null) {
                identificarUsuario(imagemFallback);
            }
            
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
        System.out.println("\n" + "=".repeat(80));
        System.out.println("=".repeat(80));
        
        try {
            // ETAPA 1: Validação da imagem capturada
            System.out.println("\n[ETAPA 1] Validando imagem capturada...");
            if (imagem == null) {
                System.err.println("❌ ERRO: Imagem é NULL");
                lblStatusReconhecimento.setText("Erro: Imagem não capturada.");
                btnCapturar.setDisable(false);
                progressReconhecimento.setVisible(false);
                return;
            }
            System.out.println("✅ Imagem válida: " + imagem.getWidth() + "x" + imagem.getHeight() + 
                             " (tipo: " + imagem.getType() + ")");
            
            lblStatusReconhecimento.setText("Processando reconhecimento facial...");
            
            // ETAPA 2: Extração de descritores faciais
            System.out.println("\n[ETAPA 2] Extraindo descritores faciais da imagem...");
            String descritores = reconhecimentoFacial.extrairDescritoresFaciais(imagem);
            
            if (descritores == null || descritores.isEmpty() || descritores.equals("[]")) {
                System.err.println("❌ ERRO: Não foi possível extrair descritores faciais");
                System.err.println("   Descritores retornados: " + (descritores == null ? "NULL" : 
                                 (descritores.isEmpty() ? "VAZIO" : descritores)));
                mostrarErro("Rosto não detectado", 
                    "Não foi possível detectar um rosto na imagem capturada.\n\n" +
                    "Possíveis causas:\n" +
                    "• Rosto não está visível ou está muito pequeno\n" +
                    "• Iluminação inadequada\n" +
                    "• Ângulo inadequado do rosto\n" +
                    "• Múltiplos rostos na imagem\n\n" +
                    "Sugestões:\n" +
                    "• Certifique-se de que o rosto está centralizado\n" +
                    "• Use boa iluminação\n" +
                    "• Mantenha uma distância adequada da câmera\n" +
                    "• Tente novamente");
                lblStatusReconhecimento.setText("Rosto não detectado. Tente novamente.");
                btnCapturar.setDisable(false);
                progressReconhecimento.setVisible(false);
                return;
            }
            
            System.out.println("✅ Descritores extraídos com sucesso!");
            System.out.println("   Tamanho: " + descritores.length() + " caracteres");
            System.out.println("   Primeiros 100 chars: " + descritores.substring(0, Math.min(100, descritores.length())));
            
            // Verifica se é um array JSON válido
            if (!descritores.trim().startsWith("[")) {
                System.err.println("⚠️ AVISO: Descritores não começam com '[' - pode não ser JSON válido");
            }
            
            // Conta quantos valores tem no array
            String valores = descritores.replaceAll("[\\[\\]\\s]", "");
            String[] valoresArray = valores.isEmpty() ? new String[0] : valores.split(",");
            System.out.println("   Número de dimensões: " + valoresArray.length);
            if (valoresArray.length > 0) {
                System.out.println("   Primeiro valor: " + valoresArray[0]);
                System.out.println("   Último valor: " + valoresArray[valoresArray.length - 1]);
            }
            
            final double THRESHOLD_FACENET = 0.54;

            if (modoFacialAtual == ModoFacial.OBRIGATORIO_APOS_CODIGO) {
                processarFacialObrigatorioAposCodigo(descritores, THRESHOLD_FACENET);
                return;
            }

            // Fallback: busca global por similaridade (código não foi validado)
            System.out.println("\n[ETAPA 3] Buscando usuário no banco (fallback facial)...");
            System.out.println("   Threshold: " + THRESHOLD_FACENET);
            usuario = dadosFaciaisDAO.buscarPorSimilaridadeFacial(descritores, THRESHOLD_FACENET);

            System.out.println("\n[ETAPA 4] Processando resultado da busca...");
            if (usuario != null) {
                System.out.println("✅ SUCESSO: Usuário identificado (fallback)!");
                lblStatusReconhecimento.setText("Usuário identificado com sucesso!");
                LogAcessoApenadoDAO.registrar(usuario.getIdUsuario(), null,
                        LogAcessoApenado.METODO_FACIAL, true, null,
                        "Acesso concedido pelo reconhecimento facial (fallback).");
                modoFacialAtual = ModoFacial.NENHUM;
                exibirDadosUsuario();
            } else {
                // Usuário não encontrado
                System.out.println("❌ FALHA: Nenhum usuário encontrado com similaridade suficiente");
                System.out.println("\n📋 DIAGNÓSTICO:");
                System.out.println("   Possíveis causas:");
                System.out.println("   1. Usuário não está cadastrado no sistema");
                System.out.println("   2. Usuário não tem foto cadastrada com descritores faciais");
                System.out.println("   3. Descritores faciais estão vazios ou inválidos no banco");
                System.out.println("   4. Similaridade calculada está abaixo do threshold (" + THRESHOLD_FACENET + ") ou match foi ambíguo");
                System.out.println("   5. A foto capturada não tem qualidade suficiente");
                System.out.println("   6. Iluminação ou posicionamento inadequados");
                System.out.println("\n" + "=".repeat(80));
                System.out.println("❌ RECONHECIMENTO FACIAL FALHOU");
                System.out.println("=".repeat(80) + "\n");
                
                // Verifica se há usuários cadastrados no banco
                boolean temUsuariosCadastrados = verificarSeTemUsuariosCadastrados();
                
                LogAcessoApenadoDAO.registrar(null, null,
                        LogAcessoApenado.METODO_FACIAL, false, null,
                        "Reconhecimento facial não localizou um apenado correspondente.");

                if (!temUsuariosCadastrados) {
                    mostrarErro("Nenhum cadastro encontrado", 
                        "Não há nenhum usuário cadastrado no sistema com foto facial.\n\n" +
                        "Para usar esta funcionalidade, é necessário:\n" +
                        "• Ter um cadastro no sistema\n" +
                        "• Ter uma foto facial cadastrada\n\n" +
                        "Entre em contato com o administrador para realizar seu cadastro.");
                } else {
                    mostrarErro("Usuário não encontrado", 
                        "Não foi possível identificar um usuário com base na imagem capturada.\n\n" +
                        "Você não está cadastrado no sistema ou não possui foto facial cadastrada.\n\n" +
                        "Possíveis causas:\n" +
                        "• Você não está cadastrado no sistema\n" +
                        "• Não há foto cadastrada com descritores faciais\n" +
                        "• A foto capturada não tem qualidade suficiente\n" +
                        "• Iluminação ou posicionamento inadequados\n\n" +
                        "Sugestões:\n" +
                        "• Entre em contato com o administrador para realizar seu cadastro\n" +
                        "• Se já possui cadastro, verifique se há foto facial cadastrada\n" +
                        "• Tente novamente com melhor iluminação\n" +
                        "• Posicione o rosto centralizado na câmera");
                }
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
     * Verifica se existem usuários cadastrados com dados faciais no banco
     */
    private boolean verificarSeTemUsuariosCadastrados() {
        try {
            List<DadosFaciais> todosDadosFaciais = dadosFaciaisDAO.listarTodos();
            return todosDadosFaciais != null && !todosDadosFaciais.isEmpty();
        } catch (Exception e) {
            System.err.println("Erro ao verificar usuários cadastrados: " + e.getMessage());
            return false;
        }
    }

    /**
     * Exibe os dados do usuário identificado
     */
    private void exibirDadosUsuario() {
        
        if (usuario == null) {
            System.err.println("  ❌ Usuário é NULL");
            return;
        }
        
        System.out.println("  Usuário identificado:");
        System.out.println("    ID: " + usuario.getIdUsuario());
        System.out.println("    Nome: " + usuario.getNome());
        System.out.println("    CPF: " + usuario.getCpf());
        
        // Preenche os campos
        System.out.println("  Preenchendo campos...");
        preencherCampos();
        
        // Carrega a foto (garantindo que é do usuário correto)
        System.out.println("  Carregando foto do usuário ID: " + usuario.getIdUsuario());
        carregarFoto();
        
        // Carrega os registros
        System.out.println("  Carregando registros...");
        carregarRegistros();
        
        // Alterna para a tela de dados
        if (paneCodigo != null) {
            paneCodigo.setVisible(false);
        }
        paneReconhecimento.setVisible(false);
        paneDados.setVisible(true);
        System.out.println("  ✅ Dados exibidos com sucesso!");
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
        
        exibirPenaAtual();
        
        // Campos complementares removidos da tela
    }

    /**
     * Exibe somente a pena ativa do apenado (sem permitir troca).
     */
    private void exibirPenaAtual() {
        penaAtual = PenaDAO.buscarPenaAtivaPorUsuario(usuario.getIdUsuario());

        if (penaAtual == null) {
            txtCodigoPena.setText("Nenhuma pena ativa");
            tblRegistros.setItems(FXCollections.observableArrayList());
            return;
        }

        java.util.List<Pena> penasDoUsuario = PenaDAO.buscarPenasPorUsuario(usuario.getIdUsuario());
        penasDoUsuario.sort((p1, p2) -> {
            if (p1.getDataInicio() == null && p2.getDataInicio() == null) return 0;
            if (p1.getDataInicio() == null) return -1;
            if (p2.getDataInicio() == null) return 1;
            return p1.getDataInicio().compareTo(p2.getDataInicio());
        });

        int numeroPena = 1;
        for (Pena pena : penasDoUsuario) {
            if (pena.getIdPena() == penaAtual.getIdPena()) {
                break;
            }
            numeroPena++;
        }

        String codigo = CodigoPenaUtil.calcularCodigoAtual(numeroPena);
        String dataInicio = formatarData(penaAtual.getDataInicio());
        txtCodigoPena.setText(codigo + " (" + dataInicio + ")");

        carregarRegistrosPorPena(penaAtual.getIdPena());
    }

    /**
     * Carrega os registros de trabalho da pena ativa.
     */
    private void carregarRegistros() {
        if (penaAtual != null) {
            carregarRegistrosPorPena(penaAtual.getIdPena());
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

        // Segurança: totem só exibe a pena ativa do apenado identificado.
        if (penaAtual == null || penaAtual.getIdPena() != idPena) {
            tblRegistros.setItems(FXCollections.observableArrayList());
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
        lista.removeIf(registro -> registro == null || registro.getFkPenaId() != idPena);
        
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
                
                String nomeInst = InstituicaoDAO.nomeInstituicaoDoRegistro(r, pena);
                RegistroDTO dto = new RegistroDTO(
                        dataTrabalho,
                        String.format("%.2f", r.getHorasCumpridas()),
                        String.format("%.2f", falta),
                        nomeInst);
                
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
        
        if (usuario == null) {
            System.err.println("  ❌ Usuário é NULL");
            return;
        }
        
        if (imgFoto == null) {
            System.err.println("  ❌ ImageView imgFoto é NULL");
            return;
        }

        try {
            int idUsuario = usuario.getIdUsuario();
            System.out.println("  Buscando foto para usuário ID: " + idUsuario);
            System.out.println("  Nome do usuário: " + usuario.getNome());
            
            DadosFaciais dadosFaciais = dadosFaciaisDAO.buscarPorUsuario(idUsuario);
            
            if (dadosFaciais == null) {
                System.err.println("  ❌ Nenhum registro de DadosFaciais encontrado para o usuário ID: " + idUsuario);
                return;
            }
            
            System.out.println("  ✅ DadosFaciais encontrado (ID: " + dadosFaciais.getIdDadosFaciais() + ")");
            
            if (dadosFaciais.getImagemRosto() == null) {
                System.err.println("  ❌ Campo imagem_rosto é NULL");
                return;
            }
            
            if (dadosFaciais.getImagemRosto().length == 0) {
                System.err.println("  ❌ Campo imagem_rosto está vazio (0 bytes)");
                return;
            }
            
            System.out.println("  ✅ Imagem encontrada: " + dadosFaciais.getImagemRosto().length + 
                             " bytes (" + String.format("%.1f", dadosFaciais.getImagemRosto().length / 1024.0) + " KB)");
            
            try {
                // Converte o byte[] para BufferedImage usando o método do ReconhecimentoFacial
                System.out.println("  Convertendo byte[] para BufferedImage...");
                util.ReconhecimentoFacial reconhecimentoFacial = new util.ReconhecimentoFacial();
                BufferedImage imagemDoBanco = reconhecimentoFacial.bytesParaImagem(dadosFaciais.getImagemRosto());
                
                if (imagemDoBanco == null) {
                    System.err.println("  ❌ Falha ao converter byte[] para BufferedImage");
                    return;
                }
                
                System.out.println("  ✅ BufferedImage criado: " + imagemDoBanco.getWidth() + "x" + imagemDoBanco.getHeight());
                
                // Converte BufferedImage para Image JavaFX
                System.out.println("  Convertendo BufferedImage para Image JavaFX...");
                Image imagePreview = converterBufferedImageParaImage(imagemDoBanco);
                
                if (imagePreview == null) {
                    System.err.println("  ❌ Falha ao converter BufferedImage para Image JavaFX");
                    return;
                }
                
                if (imagePreview.isError()) {
                    System.err.println("  ❌ Image JavaFX está com erro");
                    return;
                }
                
                System.out.println("  ✅ Image JavaFX criada: " + imagePreview.getWidth() + "x" + imagePreview.getHeight());
                
                // Atualiza a ImageView na thread do JavaFX
                Platform.runLater(() -> {
                    imgFoto.setImage(imagePreview);
                    imgFoto.setFitWidth(140.0);
                    imgFoto.setFitHeight(140.0);
                    imgFoto.setPreserveRatio(true);
                    imgFoto.setSmooth(true);
                    System.out.println("  ✅ Foto exibida no ImageView com sucesso!");
                });
                
            } catch (Exception e) {
                System.err.println("  ❌ Erro ao processar foto do banco de dados: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (Exception e) {
            System.err.println("  ❌ Erro ao carregar foto: " + e.getMessage());
            e.printStackTrace();
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
        paneDados.setVisible(false);
        if (paneReconhecimento != null) {
            paneReconhecimento.setVisible(false);
        }
        if (paneCodigo != null) {
            paneCodigo.setVisible(true);
        }
        usuario = null;
        penaAtual = null;
        codigoAcessoUtilizado = null;
        if (btnCapturar != null) {
            btnCapturar.setDisable(false);
        }
        if (progressReconhecimento != null) {
            progressReconhecimento.setVisible(false);
        }
        if (lblStatusReconhecimento != null) {
            lblStatusReconhecimento.setText("Aguardando captura...");
        }
        resetarFluxoCodigoInicial();
        atualizarStatusCodigo("", false);
        if (txtEntradaCodigo != null) {
            txtEntradaCodigo.clear();
            txtEntradaCodigo.requestFocus();
        }
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

