package controller;

import dao.PenaDAO;
import dao.RegistroDeTrabalhoDAO;
import dao.InstituicaoDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Pena;
import model.RegistroDeTrabalho;
import model.Usuario;

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
    }

    private void preencherCampos() {
        txtNome.setText(usuario.getNome());
        txtCpf.setText(usuario.getCpf());
        txtDataNasc.setText(String.valueOf(usuario.getDataNascimento()));
        //txtCodigo   .setText(usuario.getCodigo());
        txtEndereco.setText(usuario.getEndereco());
        txtBairro.setText(usuario.getBairro());
        txtCidade.setText(usuario.getCidade());
        txtUf.setText(usuario.getUf());
        txtNac.setText(usuario.getNacionalidade());
        txtDataCad.setText(String.valueOf(usuario.getDataCadastro()));
        txtFone.setText(usuario.getTelefone());
        // foto? -> use ImageView.setImage()
    }

//    private void carregarRegistros() {
//        Pena pena = PenaDAO.buscarPenaAtivaPorUsuario(usuario.getIdUsuario());
//        if (pena == null) {
//            return;
//        }
//        double totPena = pena.getHorasTotais();
//
//        var lista = RegistroDeTrabalhoDAO
//                .buscarPorUsuarioEPena(usuario.getIdUsuario(), pena.getIdPena());
//
//        double acumulado = 0;
//        var tabela = new java.util.ArrayList<RegistroDTO>();
//
//        for (RegistroDeTrabalho r : lista){
//            acumulado += r.getHorasCumpridas();
//            double falta = Math.max(totPena - acumulado, 0);
//            String inst  = InstituicaoDAO.buscarNomePorId(r.getFkInstituicaoId());
//
//            tabela.add(new RegistroDTO(
//                    String.valueOf(r.getDataTrabalho()),
//                    String.format("%.2f", r.getHorasCumpridas()),
//                    String.format("%.2f", falta),
//                    inst));
//        }
//
//        colData    .setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().data));
//        colCumprida.setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().cumprida));
//        colFalta   .setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().falta));
//        colInst    .setCellValueFactory(d -> new javafx.beans.property.SimpleStringProperty(d.getValue().inst));
//
//        tblRegistros.setItems(FXCollections.observableArrayList(tabela));
//    }
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
