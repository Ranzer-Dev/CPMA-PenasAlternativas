package br.gov.sp.cpma.desktop.util;

import br.gov.sp.cpma.desktop.client.PenaDTO;
import br.gov.sp.cpma.desktop.client.RegistroTrabalhoDTO;
import br.gov.sp.cpma.desktop.client.ResumoCumprimentoDTO;
import br.gov.sp.cpma.desktop.client.UsuarioDTO;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RelatorioImpressaoServiceTest {

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
    @DisplayName("Deve construir arvore de nos do documento com dados completos do apenado")
    void deveConstruirDocumentoComDadosCompletos() {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setIdUsuario(10L);
        usuario.setNome("Marcos Aurelio");
        usuario.setCodigo("APN-2026-01");
        usuario.setCpf("123.456.789-00");
        usuario.setTelefone("(11) 98765-4321");
        usuario.setEndereco("Rua das Flores, 123");
        usuario.setBairro("Centro");

        PenaDTO pena = new PenaDTO();
        pena.setIdPena(5L);
        pena.setTipoPena("Prestacao de Servicos a Comunidade (PSC)");
        pena.setInstituicaoPrincipalNome("Associacao Beneficente Esperanca");
        pena.setHorasTotais(120);
        pena.setDataInicio(LocalDate.of(2026, 1, 15));

        ResumoCumprimentoDTO resumo = new ResumoCumprimentoDTO();
        resumo.setHorasTotais(120);
        resumo.setHorasCumpridas(45.5);
        resumo.setHorasRestantes(74.5);

        List<RegistroTrabalhoDTO> registros = new ArrayList<>();
        RegistroTrabalhoDTO r1 = new RegistroTrabalhoDTO();
        r1.setIdRegistro(101L);
        r1.setDataTrabalho(LocalDate.of(2026, 2, 1));
        r1.setHorarioInicio("08:00");
        r1.setHorarioSaida("12:00");
        r1.setHorasCumpridas(4.0);
        r1.setAtividades("Manutencao e higienizacao de salas");
        registros.add(r1);

        Node docNode = RelatorioImpressaoService.criarDocumento(usuario, pena, resumo, registros);

        assertNotNull(docNode);
        assertTrue(docNode instanceof VBox);
        VBox docVBox = (VBox) docNode;
        assertFalse(docVBox.getChildren().isEmpty());
    }

    @Test
    @DisplayName("Deve construir documento com resiliencia a parametros nulos sem lancar NullPointerException")
    void deveConstruirDocumentoComParametrosNulos() {
        Node docNode = RelatorioImpressaoService.criarDocumento(null, null, null, null);

        assertNotNull(docNode);
        assertTrue(docNode instanceof VBox);
        VBox docVBox = (VBox) docNode;
        assertFalse(docVBox.getChildren().isEmpty());
    }

    @Test
    @DisplayName("Deve construir documento com lista de registros vazia exibindo estado neutro")
    void deveConstruirDocumentoComListaRegistrosVazia() {
        UsuarioDTO usuario = new UsuarioDTO();
        usuario.setNome("Joao Silva");

        PenaDTO pena = new PenaDTO();
        pena.setHorasTotais(80);

        Node docNode = RelatorioImpressaoService.criarDocumento(usuario, pena, null, List.of());

        assertNotNull(docNode);
        assertTrue(docNode instanceof VBox);
    }
}
