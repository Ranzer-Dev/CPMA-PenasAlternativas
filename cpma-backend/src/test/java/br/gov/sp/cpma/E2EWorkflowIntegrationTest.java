package br.gov.sp.cpma;

import br.gov.sp.cpma.api.dto.*;
import br.gov.sp.cpma.domain.entity.Administrador;
import br.gov.sp.cpma.domain.entity.Instituicao;
import br.gov.sp.cpma.domain.entity.TipoInstituicao;
import br.gov.sp.cpma.domain.entity.Usuario;
import br.gov.sp.cpma.domain.repository.AdministradorRepository;
import br.gov.sp.cpma.domain.repository.InstituicaoRepository;
import br.gov.sp.cpma.domain.repository.TipoInstituicaoRepository;
import br.gov.sp.cpma.domain.repository.UsuarioRepository;
import br.gov.sp.cpma.domain.service.PenaService;
import br.gov.sp.cpma.domain.service.RegistroTrabalhoService;
import br.gov.sp.cpma.domain.service.TokenService;
import br.gov.sp.cpma.domain.service.TotemService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class E2EWorkflowIntegrationTest {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TipoInstituicaoRepository tipoInstituicaoRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private PenaService penaService;

    @Autowired
    private RegistroTrabalhoService registroTrabalhoService;

    @Autowired
    private TotemService totemService;

    @Autowired
    private TokenService tokenService;

    @Test
    @DisplayName("Deve executar fluxo E2E completo com caracteres especiais, acentuacao, token totem e presenca")
    void deveExecutarFluxoE2ECompletoComAcentuacao() {
        Administrador admin = new Administrador();
        admin.setNome("Dr. João da Conceição");
        admin.setCpf("32165498700");
        admin.setSenha("hash");
        admin.setNivelPermissao(2);
        Administrador adminSalvo = administradorRepository.save(admin);

        TipoInstituicao tipo = new TipoInstituicao();
        tipo.setTipo("Órgão Público");
        TipoInstituicao tipoSalvo = tipoInstituicaoRepository.save(tipo);

        Instituicao inst = new Instituicao();
        inst.setNome("Secretaria de Ação Social de Votorantim");
        inst.setCidade("Votorantim");
        inst.setUf("SP");
        inst.setTipo(tipoSalvo);
        Instituicao instSalva = instituicaoRepository.save(inst);

        Usuario user = new Usuario();
        user.setNome("Sebastião Alcântara de Oliveira");
        user.setCpf("78912345600");
        user.setCodigo("APN-E2E-001");
        user.setAdministrador(adminSalvo);
        Usuario userSalvo = usuarioRepository.save(user);

        CadastroPenaRequest penaReq = new CadastroPenaRequest();
        penaReq.setUsuarioId(userSalvo.getIdUsuario());
        penaReq.setInstituicaoId(instSalva.getIdInstituicao());
        penaReq.setTipoPena("Prestação de Serviços à Comunidade");
        penaReq.setDataInicio(LocalDate.now());
        penaReq.setHorasTotais(80);
        penaReq.setHorasSemanais(8);
        PenaResponse penaResp = penaService.cadastrar(penaReq);
        assertNotNull(penaResp.getIdPena());

        CadastroRegistroTrabalhoRequest regReq = new CadastroRegistroTrabalhoRequest();
        regReq.setPenaId(penaResp.getIdPena());
        regReq.setInstituicaoId(instSalva.getIdInstituicao());
        regReq.setDataTrabalho(LocalDate.now());
        regReq.setHorasCumpridas(4.0);
        regReq.setAtividades("Revitalização da praça central");
        RegistroTrabalhoResponse regResp = registroTrabalhoService.registrar(regReq);
        assertNotNull(regResp.getIdRegistro());

        ResumoCumprimentoResponse resumo = registroTrabalhoService.obterResumo(penaResp.getIdPena());
        assertEquals(80, resumo.getHorasTotais());
        assertEquals(4.0, resumo.getHorasCumpridas(), 0.001);
        assertEquals(76.0, resumo.getHorasRestantes(), 0.001);
        assertEquals(5.0, resumo.getPercentualConcluido(), 0.001);

        String tokenTotem = tokenService.gerarTokenTotem("TOTEM_SAGUAO_01", adminSalvo.getIdAdmin());
        Claims claims = tokenService.validarTokenTotem(tokenTotem);
        assertEquals("TOTEM_SAGUAO_01", claims.getSubject());

        GerarCodigoAcessoRequest codReq = new GerarCodigoAcessoRequest();
        codReq.setUsuarioId(userSalvo.getIdUsuario());
        codReq.setAdminId(adminSalvo.getIdAdmin());
        codReq.setMinutosValidade(60);
        CodigoAcessoResponse codResp = totemService.gerarCodigoAcesso(codReq);
        assertEquals(6, codResp.getCodigo().length());
        assertEquals("ATIVO", codResp.getStatus());

        ValidarCodigoAcessoRequest valReq = new ValidarCodigoAcessoRequest();
        valReq.setCodigo(codResp.getCodigo());
        ValidarAcessoResponse valResp = totemService.validarCodigoAcesso(valReq);
        assertTrue(valResp.isSucesso());
        assertEquals(userSalvo.getNome(), valResp.getUsuarioNome());
    }
}
