package br.gov.sp.cpma.desktop.ui;

import br.gov.sp.cpma.desktop.client.InstituicaoDTO;
import br.gov.sp.cpma.desktop.util.FormValidator;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class FormControlsBindingTest {

    @BeforeAll
    static void initJavaFx() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            Platform.startup(latch::countDown);
        } catch (IllegalStateException e) {
            latch.countDown();
        }
        assertTrue(latch.await(5, TimeUnit.SECONDS));
    }

    @Test
    @DisplayName("Deve alternar estado de selecao de checkboxes de dias da semana e acumular selecionados")
    void deveAlternarSelecaoCheckboxesDiasSemana() {
        CheckBox chkSegunda = new CheckBox("Segunda-feira");
        CheckBox chkTerca = new CheckBox("Terca-feira");
        CheckBox chkQuarta = new CheckBox("Quarta-feira");
        CheckBox chkQuinta = new CheckBox("Quinta-feira");
        CheckBox chkSexta = new CheckBox("Sexta-feira");
        CheckBox chkSabado = new CheckBox("Sabado");

        List<CheckBox> dias = List.of(chkSegunda, chkTerca, chkQuarta, chkQuinta, chkSexta, chkSabado);

        for (CheckBox chk : dias) {
            assertFalse(chk.isSelected());
        }

        chkSegunda.setSelected(true);
        chkQuarta.setSelected(true);
        chkSexta.setSelected(true);

        List<String> diasAtivos = new ArrayList<>();
        for (CheckBox chk : dias) {
            if (chk.isSelected()) {
                diasAtivos.add(chk.getText());
            }
        }

        assertEquals(3, diasAtivos.size());
        assertTrue(diasAtivos.contains("Segunda-feira"));
        assertTrue(diasAtivos.contains("Quarta-feira"));
        assertTrue(diasAtivos.contains("Sexta-feira"));
        assertFalse(diasAtivos.contains("Sabado"));

        chkSegunda.setSelected(false);
        assertFalse(chkSegunda.isSelected());
    }

    @Test
    @DisplayName("Deve formatar e selecionar instituicao corretamente no ComboBox com StringConverter")
    void deveSelecionarInstituicaoComboBoxComConverter() {
        ComboBox<InstituicaoDTO> cbInstituicao = new ComboBox<>();

        cbInstituicao.setConverter(new StringConverter<>() {
            @Override
            public String toString(InstituicaoDTO inst) {
                return inst != null ? inst.getNome() : "";
            }

            @Override
            public InstituicaoDTO fromString(String string) {
                return null;
            }
        });

        InstituicaoDTO inst1 = new InstituicaoDTO();
        inst1.setIdInstituicao(1L);
        inst1.setNome("Hospital das Clinicas");

        InstituicaoDTO inst2 = new InstituicaoDTO();
        inst2.setIdInstituicao(2L);
        inst2.setNome("Parque Zoologico");

        cbInstituicao.setItems(FXCollections.observableArrayList(inst1, inst2));
        assertNull(cbInstituicao.getValue());

        cbInstituicao.getSelectionModel().select(inst1);
        assertEquals(inst1, cbInstituicao.getValue());
        assertEquals("Hospital das Clinicas", cbInstituicao.getConverter().toString(cbInstituicao.getValue()));

        cbInstituicao.getSelectionModel().select(inst2);
        assertEquals(inst2, cbInstituicao.getValue());
        assertEquals("Parque Zoologico", cbInstituicao.getConverter().toString(cbInstituicao.getValue()));

        cbInstituicao.getSelectionModel().clearSelection();
        assertNull(cbInstituicao.getValue());
        assertEquals("", cbInstituicao.getConverter().toString(null));
    }

    @Test
    @DisplayName("Deve limpar classe de erro e label reativamente ao modificar texto de TextField")
    void deveLimparErroReativamenteAoModificarTexto() {
        TextField txtNome = new TextField();
        Label lblErro = new Label();

        FormValidator.vincularLimpezaAoInteragir(txtNome, lblErro);

        FormValidator.marcarErro(txtNome, lblErro, "Campo obrigatorio");
        assertTrue(txtNome.getStyleClass().contains("field-error"));
        assertEquals("Campo obrigatorio", lblErro.getText());

        txtNome.setText("A");
        assertFalse(txtNome.getStyleClass().contains("field-error"));
        assertEquals("", lblErro.getText());
    }

    @Test
    @DisplayName("Deve limpar classe de erro reativamente ao alterar selecao de ComboBox")
    void deveLimparErroAoAlterarSelecaoComboBox() {
        ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList("Tipo A", "Tipo B"));
        Label lblErro = new Label();

        FormValidator.vincularLimpezaAoInteragir(cbTipo, lblErro);

        FormValidator.marcarErro(cbTipo, lblErro, "Selecione um tipo");
        assertTrue(cbTipo.getStyleClass().contains("field-error"));
        assertEquals("Selecione um tipo", lblErro.getText());

        cbTipo.setValue("Tipo A");
        assertFalse(cbTipo.getStyleClass().contains("field-error"));
        assertEquals("", lblErro.getText());
    }

    @Test
    @DisplayName("Deve limpar classe de erro reativamente ao selecionar data no DatePicker")
    void deveLimparErroAoSelecionarDataNoDatePicker() {
        DatePicker dpData = new DatePicker();
        Label lblErro = new Label();

        FormValidator.vincularLimpezaAoInteragir(dpData, lblErro);

        FormValidator.marcarErro(dpData, lblErro, "Data obrigatoria");
        assertTrue(dpData.getStyleClass().contains("field-error"));
        assertEquals("Data obrigatoria", lblErro.getText());

        dpData.setValue(LocalDate.now());
        assertFalse(dpData.getStyleClass().contains("field-error"));
        assertEquals("", lblErro.getText());
    }

    @Test
    @DisplayName("Deve validar campo obrigatorio e numero inteiro positivo em fluxo de formulario")
    void deveValidarCamposObrigatoriosENumericos() {
        TextField txtHoras = new TextField();
        Label lblErro = new Label();

        assertFalse(FormValidator.validarNumeroInteiroPositivo(txtHoras, lblErro, "Horas invalidas"));
        assertTrue(txtHoras.getStyleClass().contains("field-error"));

        txtHoras.setText("-5");
        assertFalse(FormValidator.validarNumeroInteiroPositivo(txtHoras, lblErro, "Horas invalidas"));

        txtHoras.setText("abc");
        assertFalse(FormValidator.validarNumeroInteiroPositivo(txtHoras, lblErro, "Horas invalidas"));

        txtHoras.setText("40");
        assertTrue(FormValidator.validarNumeroInteiroPositivo(txtHoras, lblErro, "Horas invalidas"));
        assertFalse(txtHoras.getStyleClass().contains("field-error"));
        assertEquals("", lblErro.getText());
    }
}
