package controller;

import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import dao.InstituicaoDAO;
import dao.PenaDAO;
import dao.RegistroDeTrabalhoDAO;
import model.Pena;
import model.RegistroDeTrabalho;
import model.Usuario;
import util.CodigoPenaUtil;

public class DetalheApenadoController {

    @FXML
    private TextField txtNome, txtCpf, txtDataNasc, txtCodigo,
            txtEndereco, txtBairro, txtCidade, txtUf,
            txtNac, txtDataCad, txtFone;
    @FXML
    private TableView<RegistroDTO> tblRegistros;
    @FXML
    private TableColumn<RegistroDTO, String> colData, colInst;
    @FXML
    private TableColumn<RegistroDTO, String> colCumprida, colFalta;
    @FXML
    private Button btnVoltar, btnEditar, btnImprimir;

    private Usuario usuario;

    public void setUsuario(Usuario u) {
        this.usuario = u;
        preencherCampos();
        carregarRegistros();
    }

    private void preencherCampos() {
        txtNome.setText(usuario.getNome());
        txtCpf.setText(usuario.getCpf());
        txtDataNasc.setText(String.valueOf(usuario.getDataNascimento()));
        
        // Preenche o código de penas
        preencherCodigoPenas();
        
        txtEndereco.setText(usuario.getEndereco());
        txtBairro.setText(usuario.getBairro());
        txtCidade.setText(usuario.getCidade());
        txtUf.setText(usuario.getUf());
        txtNac.setText(usuario.getNacionalidade());
        txtDataCad.setText(String.valueOf(usuario.getCriadoEm()));
        txtFone.setText(usuario.getTelefone());
        // foto? -> use ImageView.setImage()
    }
    
    /**
     * Preenche o campo de código de penas.
     * Mostra o código salvo no banco ou calcula baseado no número de penas.
     */
    private void preencherCodigoPenas() {
        int numeroPenas = PenaDAO.contarPenasPorUsuario(usuario.getIdUsuario());
        
        if (numeroPenas == 0) {
            // Se não tem penas, mostra o código salvo no banco ou "Nenhuma pena"
            String codigoSalvo = (usuario.getCodigo() != null && !usuario.getCodigo().trim().isEmpty()) 
                    ? usuario.getCodigo() 
                    : "Nenhuma pena cadastrada";
            txtCodigo.setText(codigoSalvo);
        } else {
            // Mostra o código calculado baseado no número de penas
            String codigoAtual = CodigoPenaUtil.calcularCodigoAtual(numeroPenas);
            
            // Se houver código salvo diferente, mostra ambos
            String codigoSalvo = (usuario.getCodigo() != null && !usuario.getCodigo().trim().isEmpty()) 
                    ? usuario.getCodigo() 
                    : null;
            
            if (codigoSalvo != null && !codigoSalvo.equals(codigoAtual)) {
                // Se o código salvo é diferente do calculado, mostra o salvo
                txtCodigo.setText(codigoSalvo);
            } else {
                txtCodigo.setText(codigoAtual);
            }
        }
    }

    private void carregarRegistros() {
        Pena pena = PenaDAO.buscarPenaAtivaPorUsuario(usuario.getIdUsuario());
        if (pena == null) {
            return;
        }
        double totPena = pena.getHorasTotais();

        var lista = RegistroDeTrabalhoDAO
                .buscarPorUsuarioEPena(usuario.getIdUsuario(), pena.getIdPena());

        double acumulado = 0;
        var tabela = new java.util.ArrayList<RegistroDTO>();

        for (RegistroDeTrabalho r : lista){
            acumulado += r.getHorasCumpridas();
            double falta = Math.max(totPena - acumulado, 0);
            // Não há fk da instituição no registro; mostrar nome da instituição da pena
            String inst  = InstituicaoDAO.buscarNomePorId(pena.getFkInstituicaoIdInstituicao());

            tabela.add(new RegistroDTO(
                    String.valueOf(r.getDataTrabalho()),
                    String.format("%.2f", r.getHorasCumpridas()),
                    String.format("%.2f", falta),
                    inst));
        }

        colData    .setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().data));
        colCumprida.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().cumprida));
        colFalta   .setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().falta));
        colInst    .setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().inst));

        tblRegistros.setItems(FXCollections.observableArrayList(tabela));
    }
    private record RegistroDTO(String data, String cumprida, String falta, String inst) {

    }

    @FXML
    private void initialize() {
        btnVoltar.setOnAction(e -> ((Stage) btnVoltar.getScene().getWindow()).close());
        btnEditar.setOnAction(e -> {/* abrir tela de edição (futuramente) */
        });
        btnImprimir.setOnAction(e -> imprimir());
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
