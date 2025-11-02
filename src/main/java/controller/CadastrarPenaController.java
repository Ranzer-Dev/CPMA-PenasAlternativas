package controller;

import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


import dao.InstituicaoDAO;
import dao.PenaDAO;
import dao.UsuarioDAO;
import utils.FormatacaoUtils;
import util.CodigoPenaUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import model.Instituicao;
import model.Pena;
import model.Usuario;

public class CadastrarPenaController {

    @FXML private ComboBox<Usuario> usuario;
    @FXML private ComboBox<Instituicao> instituicao;
    @FXML private ComboBox<Pena> comboPenas;
    @FXML private VBox vboxComboPenas;
    @FXML private TextField tipoPena, tempoPena, horasSemanais, horasTotais;
    @FXML private TextField codigoAtual, proximoCodigo;
    @FXML private DatePicker dataInicio, dataTermino;
    @FXML private TextArea descricao, atividadesAcordadas;
    @FXML private Button btnCadastrarPena;

    @FXML private CheckBox checkSegunda, checkTerca, checkQuarta, checkQuinta, checkSexta, checkSabado;
    @FXML private TextField inicioSeg, saidaSeg, inicioSeg2, saidaSeg2;
    @FXML private TextField inicioTer, saidaTer, inicioTer2, saidaTer2;
    @FXML private TextField inicioQua, saidaQua, inicioQua2, saidaQua2;
    @FXML private TextField inicioQui, saidaQui, inicioQui2, saidaQui2;
    @FXML private TextField inicioSex, saidaSex, inicioSex2, saidaSex2;
    @FXML private TextField inicioSab, saidaSab, inicioSab2, saidaSab2;

    private boolean modoEdicao = false;
    private Pena penaAtual;

    @FXML
    private void initialize() {
        carregarUsuarios();
        carregarInstituicoes();
        configurarFormatacaoData();
        configurarFormatacaoHoras();

        btnCadastrarPena.setOnAction(e -> {
            if (modoEdicao) salvarAlteracoes();
            else cadastrar();
        });

        horasTotais.textProperty().addListener((obs, oldVal, newVal) -> atualizarCamposCalculados());
        horasSemanais.textProperty().addListener((obs, oldVal, newVal) -> atualizarCamposCalculados());
        dataInicio.valueProperty().addListener((obs, oldVal, newVal) -> atualizarCamposCalculados());

        // Listener para atualizar código quando usuário for selecionado
        usuario.setOnAction(e -> atualizarCodigoPenas());

        tempoPena.setEditable(false);
        codigoAtual.setEditable(false);
        proximoCodigo.setEditable(false);
    }

    public void ativarModoEdicao() {
        modoEdicao = true;
        vboxComboPenas.setVisible(true);
        btnCadastrarPena.setText("Salvar alterações");

        usuario.setOnAction(e -> {
            carregarPenasDoUsuario();
            atualizarCodigoPenas(); // Atualiza código quando usuário é selecionado
        });

        comboPenas.setOnAction(e -> {
            Pena p = comboPenas.getValue();
            if (p != null) preencherCampos(p);
        });
    }

    private void carregarUsuarios() {
        List<Usuario> usuarios = UsuarioDAO.buscarTodosUsuarios();
        usuario.setItems(FXCollections.observableArrayList(usuarios));
        usuario.setConverter(new StringConverter<>() {
            @Override
            public String toString(Usuario u) {
                return u == null ? "" : u.getNome();
            }
            @Override
            public Usuario fromString(String s) {
                return null; // Não usado
            }
        });
    }

    private void carregarInstituicoes() {
        List<Instituicao> instituicoes = InstituicaoDAO.buscarTodasInstituicoes();
        instituicao.setItems(FXCollections.observableArrayList(instituicoes));
        instituicao.setConverter(new StringConverter<>() {
            @Override
            public String toString(Instituicao i) {
                return i == null ? "" : i.getNome();
            }
            @Override
            public Instituicao fromString(String s) {
                return null;
            }
        });
    }

    private void carregarPenasDoUsuario() {
        Usuario u = usuario.getValue();
        if (u == null) return;

        List<Pena> penas = PenaDAO.buscarPenasPorUsuario(u.getIdUsuario());
        comboPenas.setItems(FXCollections.observableArrayList(penas));
        comboPenas.setConverter(new StringConverter<>() {
            @Override public String toString(Pena p) { return p == null ? "" : p.getDescricao(); }
            @Override public Pena fromString(String s) { return null; }
        });
        comboPenas.getSelectionModel().clearSelection();
    }

    private void preencherCampos(Pena p) {
        penaAtual = p;
        usuario.getSelectionModel().select(buscarUsuarioPorId(p.getFkUsuarioIdUsuario()));
        instituicao.getSelectionModel().select(buscarInstituicaoPorId(p.getFkInstituicaoIdInstituicao()));
        tipoPena.setText(p.getTipoPena());
        tempoPena.setText(String.valueOf(p.getTempoPena()));
        horasSemanais.setText(String.valueOf(p.getHorasSemanais()));
        horasTotais.setText(String.valueOf(p.getHorasTotais()));
        descricao.setText(p.getDescricao());
        atividadesAcordadas.setText(p.getAtividadesAcordadas());
        dataInicio.setValue(p.getDataInicio().toLocalDate());
        dataTermino.setValue(p.getDataTermino() != null ? p.getDataTermino().toLocalDate() : null);
        preencherHorarios(p.getDiasSemanaEHorariosDisponivel());
        
        // Atualiza o código de penas quando uma pena é selecionada para edição
        atualizarCodigoPenas();
    }

    private Usuario buscarUsuarioPorId(int id) {
        return usuario.getItems().stream().filter(u -> u.getIdUsuario() == id).findFirst().orElse(null);
    }

    private Instituicao buscarInstituicaoPorId(int id) {
        return instituicao.getItems().stream().filter(i -> i.getIdInstituicao() == id).findFirst().orElse(null);
    }

    private void cadastrar() {
        Pena novaPena = construirPena();
        if (novaPena == null) return;

        Usuario u = usuario.getValue();
        if (u == null) {
            alert("Selecione um usuário.");
            return;
        }

        // Conta penas atuais antes de inserir
        int numeroPenasAtuais = PenaDAO.contarPenasPorUsuario(u.getIdUsuario());
        
        // Calcula o próximo código
        String novoCodigo = CodigoPenaUtil.calcularProximoCodigo(numeroPenasAtuais);

        boolean sucesso = PenaDAO.inserirPena(novaPena) > 0;
        if (sucesso) {
            // Atualiza o código do usuário no banco
            UsuarioDAO.atualizarCodigo(u.getIdUsuario(), novoCodigo);
            alert("Pena cadastrada com sucesso! Código atualizado para: " + novoCodigo);
            limparCampos();
            fecharJanela();
        } else {
            alert("Falha ao cadastrar pena.");
        }
    }

    private void salvarAlteracoes() {
        Pena p = construirPena();
        if (p == null) return;

        p.setIdPena(penaAtual.getIdPena());
        boolean ok = PenaDAO.atualizar(p);

        alert(ok ? "Pena atualizada com sucesso!" : "Erro ao atualizar pena.");

        if (ok) {
            carregarPenasDoUsuario();
            penaAtual = PenaDAO.buscarPorId(p.getIdPena());
            comboPenas.getSelectionModel().select(penaAtual);
            preencherCampos(penaAtual);
        }
    }

    private Pena construirPena() {
        Usuario u = usuario.getValue();
        Instituicao i = instituicao.getValue();

        if (u == null || i == null) {
            alert("Selecione usuário e instituição.");
            return null;
        }

        try {
            String tipo = tipoPena.getText().trim();
            String tempoStr = tempoPena.getText().trim();
            String horasSemStr = horasSemanais.getText().trim();
            String horasTotStr = horasTotais.getText().trim();
            String desc = descricao.getText().trim();
            String atividades = atividadesAcordadas.getText().trim();

            if (tipo.isEmpty() || tempoStr.isEmpty() || horasSemStr.isEmpty() || horasTotStr.isEmpty()) {
                alert("Preencha todos os campos obrigatórios.");
                return null;
            }

            double tempo = Double.parseDouble(tempoStr.replace(",", "."));
            int horasSem = Integer.parseInt(horasSemStr);
            double horasTot = Double.parseDouble(horasTotStr);
            LocalDate ini = dataInicio.getValue();
            if (ini == null) {
                alert("Data de início é obrigatória.");
                return null;
            }
            LocalDate fim = dataTermino.getValue();

            Pena pena = new Pena();
            pena.setFkUsuarioIdUsuario(u.getIdUsuario());
            pena.setFkInstituicaoIdInstituicao(i.getIdInstituicao());
            pena.setTipoPena(tipo);
            pena.setTempoPena(tempo);
            pena.setHorasSemanais(horasSem);
            pena.setHorasTotais(horasTot);
            pena.setDescricao(desc);
            pena.setAtividadesAcordadas(atividades);
            pena.setDataInicio(Date.valueOf(ini));
            pena.setDataTermino(fim != null ? Date.valueOf(fim) : null);
            pena.setDiasSemanaEHorariosDisponivel(montarHorarios());
            return pena;

        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            alert("Informe valores numéricos válidos.");
            return null;
        }
    }

    private String montarHorarios() {
        StringBuilder sb = new StringBuilder();
        appendDia(sb, "segunda", checkSegunda, inicioSeg, saidaSeg, inicioSeg2, saidaSeg2);
        appendDia(sb, "terça", checkTerca, inicioTer, saidaTer, inicioTer2, saidaTer2);
        appendDia(sb, "quarta", checkQuarta, inicioQua, saidaQua, inicioQua2, saidaQua2);
        appendDia(sb, "quinta", checkQuinta, inicioQui, saidaQui, inicioQui2, saidaQui2);
        appendDia(sb, "sexta", checkSexta, inicioSex, saidaSex, inicioSex2, saidaSex2);
        appendDia(sb, "sábado", checkSabado, inicioSab, saidaSab, inicioSab2, saidaSab2);

        if (sb.length() > 2) sb.setLength(sb.length() - 2);

        return sb.toString();
    }

    private void appendDia(StringBuilder sb, String dia, CheckBox check, TextField i1, TextField s1, TextField i2, TextField s2) {
        if (!check.isSelected()) return;
        sb.append(dia);
        if (!i1.getText().isBlank()) sb.append(" ").append(i1.getText().trim());
        if (!s1.getText().isBlank()) sb.append(" ").append(s1.getText().trim());
        if (!i2.getText().isBlank()) sb.append(" ").append(i2.getText().trim());
        if (!s2.getText().isBlank()) sb.append(" ").append(s2.getText().trim());
        sb.append(", ");
    }

    private void preencherHorarios(String texto) {
        if (texto == null || texto.isBlank()) return;

        String[] dias = texto.split(",\\s*");

        for (String diaHorario : dias) {
            String[] partes = diaHorario.trim().split("\\s+");
            if (partes.length < 1) continue;

            String dia = partes[0].toLowerCase();

            switch (dia) {
                case "segunda" -> setHorarioDia(checkSegunda, inicioSeg, saidaSeg, inicioSeg2, saidaSeg2, partes);
                case "terça" -> setHorarioDia(checkTerca, inicioTer, saidaTer, inicioTer2, saidaTer2, partes);
                case "quarta" -> setHorarioDia(checkQuarta, inicioQua, saidaQua, inicioQua2, saidaQua2, partes);
                case "quinta" -> setHorarioDia(checkQuinta, inicioQui, saidaQui, inicioQui2, saidaQui2, partes);
                case "sexta" -> setHorarioDia(checkSexta, inicioSex, saidaSex, inicioSex2, saidaSex2, partes);
                case "sábado" -> setHorarioDia(checkSabado, inicioSab, saidaSab, inicioSab2, saidaSab2, partes);
            }
        }
    }

    private void setHorarioDia(CheckBox check, TextField i1, TextField s1, TextField i2, TextField s2, String[] partes) {
        check.setSelected(true);

        i1.clear(); s1.clear(); i2.clear(); s2.clear();

        if (partes.length > 1) i1.setText(partes[1]);
        if (partes.length > 2) s1.setText(partes[2]);
        if (partes.length > 3) i2.setText(partes[3]);
        if (partes.length > 4) s2.setText(partes[4]);
    }

    private void atualizarCamposCalculados() {
        try {
            double totalHoras = Double.parseDouble(horasTotais.getText().trim());
            int horasSemana = Integer.parseInt(horasSemanais.getText().trim());

            if (horasSemana <= 0) return;

            double mesesEstimado = totalHoras / (horasSemana * 4.0);
            tempoPena.setText(String.format("%.1f", mesesEstimado));

            LocalDate inicio = dataInicio.getValue();
            if (inicio != null) {
                double diasEstimado = (totalHoras / horasSemana) * 7.0;
                int dias = (int) Math.ceil(diasEstimado);
                LocalDate termino = inicio.plusDays(dias);
                dataTermino.setValue(termino);
            }

        } catch (NumberFormatException e) {
        }
    }


    /**
     * Atualiza os campos de código quando um usuário é selecionado.
     */
    private void atualizarCodigoPenas() {
        Usuario u = usuario.getValue();
        if (u == null) {
            codigoAtual.setText("");
            proximoCodigo.setText("");
            return;
        }

        int numeroPenas = PenaDAO.contarPenasPorUsuario(u.getIdUsuario());
        
        if (numeroPenas == 0) {
            // Se não tem penas, mostra o código salvo no banco ou "Nenhuma pena"
            String codigoSalvo = (u.getCodigo() != null && !u.getCodigo().trim().isEmpty()) 
                    ? u.getCodigo() 
                    : "Nenhuma pena";
            codigoAtual.setText(codigoSalvo);
            proximoCodigo.setText(CodigoPenaUtil.calcularProximoCodigo(0));
        } else {
            // Mostra o código calculado baseado no número de penas (deve corresponder ao salvo no banco)
            String codigoAtualStr = CodigoPenaUtil.calcularCodigoAtual(numeroPenas);
            String proximoCodigoStr = CodigoPenaUtil.calcularProximoCodigo(numeroPenas);
            
            // Se houver código salvo que seja diferente, mostra ambos (salvo e calculado)
            String codigoSalvo = (u.getCodigo() != null && !u.getCodigo().trim().isEmpty()) 
                    ? u.getCodigo() 
                    : null;
            
            if (codigoSalvo != null && !codigoSalvo.equals(codigoAtualStr)) {
                // Se o código salvo é diferente do calculado, mostra o salvo como referência
                codigoAtual.setText(codigoSalvo + " (calc: " + codigoAtualStr + ")");
            } else {
                codigoAtual.setText(codigoAtualStr);
            }
            proximoCodigo.setText(proximoCodigoStr);
        }
    }

    private void limparCampos() {
        usuario.getSelectionModel().clearSelection();
        instituicao.getSelectionModel().clearSelection();
        tipoPena.clear(); tempoPena.clear();
        horasSemanais.clear(); horasTotais.clear();
        dataInicio.setValue(null); dataTermino.setValue(null);
        descricao.clear(); atividadesAcordadas.clear();
        codigoAtual.clear();
        proximoCodigo.clear();
    }

    /**
     * Configura a formatação de data em formato brasileiro (dd/MM/yyyy)
     */
    private void configurarFormatacaoData() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        
        // Configuração para dataInicio
        dataInicio.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, formatter);
                } else {
                    return null;
                }
            }
        });

        // Configuração para dataTermino
        dataTermino.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return formatter.format(date);
                } else {
                    return "";
                }
            }

            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, formatter);
                } else {
                    return null;
                }
            }
        });
    }

    /**
     * Configura a formatação de campos de hora usando FormatacaoUtils
     */
    private void configurarFormatacaoHoras() {
        // Array com todos os campos de hora
        TextField[] camposHora = {
            inicioSeg, saidaSeg, inicioSeg2, saidaSeg2,
            inicioTer, saidaTer, inicioTer2, saidaTer2,
            inicioQua, saidaQua, inicioQua2, saidaQua2,
            inicioQui, saidaQui, inicioQui2, saidaQui2,
            inicioSex, saidaSex, inicioSex2, saidaSex2,
            inicioSab, saidaSab, inicioSab2, saidaSab2
        };

        // Aplica formatação usando a classe utilitária para cada campo de hora
        for (TextField campo : camposHora) {
            FormatacaoUtils.aplicarFormatacaoHora(campo);
        }
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK) {{
            setTitle("Aviso"); setHeaderText(null); showAndWait();
        }};
    }

    private void fecharJanela() {
        Stage stage = (Stage) btnCadastrarPena.getScene().getWindow();
        stage.close();
    }
}
