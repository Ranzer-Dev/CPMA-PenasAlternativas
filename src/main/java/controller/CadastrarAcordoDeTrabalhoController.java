package controller;

import dao.AcordoDeTrabalhoDAO;
import dao.UsuarioDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.AcordoDeTrabalho;
import model.Instituicao;
import model.Usuario;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class CadastrarAcordoDeTrabalhoController {

    @FXML private ComboBox<Usuario> comboUsuario;
    @FXML private TextArea atividadesAcordadas;
    @FXML private Button btnCadastrar;
    @FXML private ComboBox<Instituicao> comboInstituicao;

    @FXML private CheckBox checkSegunda;
    @FXML private TextField inicioSeg, saidaSeg, inicioSeg2, saidaSeg2;
    @FXML private CheckBox checkTerca;
    @FXML private TextField inicioTer, saidaTer, inicioTer2, saidaTer2;
    @FXML private CheckBox checkQuarta;
    @FXML private TextField inicioQua, saidaQua, inicioQua2, saidaQua2;
    @FXML private CheckBox checkQuinta;
    @FXML private TextField inicioQui, saidaQui, inicioQui2, saidaQui2;
    @FXML private CheckBox checkSexta;
    @FXML private TextField inicioSex, saidaSex, inicioSex2, saidaSex2;

    private static class DiaLinha {
        CheckBox dia;
        TextField ini1, sai1, ini2, sai2;
        DiaLinha(CheckBox d, TextField i1, TextField s1, TextField i2, TextField s2){
            dia=d; ini1=i1; sai1=s1; ini2=i2; sai2=s2;
        }
    }
    private final List<DiaLinha> linhas = new ArrayList<>();

    @FXML
    public void initialize() {
        carregarUsuarios();
        popularLinhas();
        btnCadastrar.setOnAction(e -> cadastrar());
        comboInstituicao.setItems(FXCollections.observableArrayList(dao.InstituicaoDAO.buscarTodasInstituicoes()));

        linhas.forEach(l -> {
            aplicarMascaraHora(l.ini1);
            aplicarMascaraHora(l.sai1);
            aplicarMascaraHora(l.ini2);
            aplicarMascaraHora(l.sai2);
        });
    }

    private void carregarUsuarios() {
        comboUsuario.setItems(FXCollections.observableArrayList(UsuarioDAO.buscarTodosUsuarios()));
    }

    private void popularLinhas() {
        linhas.add(new DiaLinha(checkSegunda, inicioSeg,  saidaSeg,  inicioSeg2,  saidaSeg2));
        linhas.add(new DiaLinha(checkTerca,   inicioTer,  saidaTer,  inicioTer2,  saidaTer2));
        linhas.add(new DiaLinha(checkQuarta,  inicioQua,  saidaQua,  inicioQua2,  saidaQua2));
        linhas.add(new DiaLinha(checkQuinta,  inicioQui,  saidaQui,  inicioQui2,  saidaQui2));
        linhas.add(new DiaLinha(checkSexta,   inicioSex,  saidaSex,  inicioSex2,  saidaSex2));
    }

    private void cadastrar() {
        Usuario u = comboUsuario.getValue();
        Instituicao i = comboInstituicao.getValue();
        if (u == null) { alert("Selecione o usuário."); return; }

        StringJoiner sj = new StringJoiner("\n");
        boolean marcou = false;

        for (DiaLinha l : linhas) {
            if (l.dia.isSelected()) {
                marcou = true;
                sj.add(String.format("%s %s, %s %s, %s",
                        l.dia.getText().toLowerCase(),
                        semPontuacao(l.ini1), semPontuacao(l.sai1),
                        semPontuacao(l.ini2), semPontuacao(l.sai2)));
            }
        }
        if (!marcou) { alert("Marque ao menos um dia."); return; }
        if (i == null) { alert("Selecione a instituição."); return; }

        try {
            boolean ok = cadastrarAcordo(
                    u.getIdUsuario(),
                    i.getIdInstituicao(),
                    sj.toString(),
                    atividadesAcordadas.getText()
            );


            alert(ok ? "Acordo cadastrado!" : "Falha ao cadastrar. Verifique se preencheu Atividades ou consulte o log.");
        } catch (Exception ex) {
            ex.printStackTrace();
            alert("Erro: " + ex.getMessage());
        }

    }

    public boolean cadastrarAcordo(int idUsuario,
                                   int idInstituicao,
                                   String diasSemanaEHorarios,
                                   String atividades) {
        if (idUsuario <= 0 || idInstituicao <= 0 ||
                diasSemanaEHorarios == null || diasSemanaEHorarios.isBlank() ||
                atividades == null || atividades.isBlank()) {
            return false;
        }

        AcordoDeTrabalho acordo = new AcordoDeTrabalho();
        acordo.setIdUsuario(idUsuario);
        acordo.setIdInstituicao(idInstituicao);
        acordo.setDiasSemanaEHorarios(diasSemanaEHorarios.trim());
        acordo.setAtividadesAcordadas(atividades.trim());

        return AcordoDeTrabalhoDAO.inserir(acordo) > 0;
    }

    /* ----- utilidades ----- */
    private String val(TextField tf){ return tf.getText().isBlank() ? "--:--" : tf.getText().trim(); }

    private String semPontuacao(TextField tf) {
        String txt = tf.getText().replaceAll("[^0-9]", "");
        if (txt.length() > 4) txt = txt.substring(0, 4);
        return txt.isEmpty() ? "----" : txt;
    }

    private void aplicarMascaraHora(TextField campo) {
        campo.textProperty().addListener((obs, oldValue, newValue) -> {
            String valor = newValue.replaceAll("[^0-9]", "");
            if (valor.length() > 4) valor = valor.substring(0, 4);

            StringBuilder f = new StringBuilder();
            for (int i = 0; i < valor.length(); i++) {
                f.append(valor.charAt(i));
                if (i == 1 && valor.length() > 2) f.append(":");
            }

            int caret = campo.getCaretPosition();
            javafx.application.Platform.runLater(() -> {
                campo.setText(f.toString());
                campo.positionCaret(Math.min(caret, campo.getText().length()));
            });
        });
    }

    private void limpar() {
        comboUsuario.getSelectionModel().clearSelection();
        linhas.forEach(l -> { l.dia.setSelected(false); l.ini1.clear(); l.sai1.clear();
            l.ini2.clear(); l.sai2.clear(); });
        atividadesAcordadas.clear();
    }

    private void alert(String m){
        new Alert(Alert.AlertType.INFORMATION,m,ButtonType.OK).showAndWait();
    }
}
