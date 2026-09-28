package br.gov.sp.cpma;

import br.gov.sp.cpma.domain.entity.*;
import br.gov.sp.cpma.domain.repository.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("postgres")
@Transactional
public class PostgresIntegrationTest {

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private InstituicaoRepository instituicaoRepository;

    @Autowired
    private TipoInstituicaoRepository tipoInstituicaoRepository;

    @Autowired
    private PenaRepository penaRepository;

    @Autowired
    private RegistroDeTrabalhoRepository registroRepository;

    @Autowired
    private CodigoAcessoApenadoRepository codigoAcessoRepository;

    @Autowired
    private LogAcessoApenadoRepository logAcessoRepository;

    @Test
    @DisplayName("Deve persistir e consultar administrador no PostgreSQL oficial")
    void devePersistirEConsultarAdministradorNoPostgres() {
        Administrador admin = new Administrador();
        admin.setNome("Dr. Marcelo Juiz");
        admin.setCpf("99988877766");
        admin.setSenha("hash_senha_segura");
        admin.setNivelPermissao(2);
        admin.setPerguntaSecreta("Cidade natal?");
        admin.setRespostaSecreta("Sorocaba");

        Administrador salvo = administradorRepository.save(admin);
        assertNotNull(salvo.getIdAdmin());

        Optional<Administrador> buscado = administradorRepository.findByCpf("99988877766");
        assertTrue(buscado.isPresent());
        assertEquals("Dr. Marcelo Juiz", buscado.get().getNome());
    }

    @Test
    @DisplayName("Deve persistir instituicao e vincular com pena no PostgreSQL")
    void devePersistirInstituicaoEPenaNoPostgres() {
        Administrador admin = new Administrador();
        admin.setNome("Admin Juizado");
        admin.setCpf("12398745600");
        admin.setSenha("hash");
        admin.setNivelPermissao(1);
        Administrador adminSalvo = administradorRepository.save(admin);

        TipoInstituicao tipo = new TipoInstituicao();
        tipo.setTipo("Filantropica");
        TipoInstituicao tipoSalvo = tipoInstituicaoRepository.save(tipo);

        Instituicao inst = new Instituicao();
        inst.setNome("Associacao Beneficente Votorantim");
        inst.setCidade("Votorantim");
        inst.setUf("SP");
        inst.setTipo(tipoSalvo);
        Instituicao instSalva = instituicaoRepository.save(inst);
        assertNotNull(instSalva.getIdInstituicao());

        Usuario user = new Usuario();
        user.setNome("Lucas Santana");
        user.setCpf("11122233344");
        user.setCodigo("APN-PG-001");
        user.setAdministrador(adminSalvo);
        Usuario userSalvo = usuarioRepository.save(user);
        assertNotNull(userSalvo.getIdUsuario());

        Pena pena = new Pena();
        pena.setUsuario(userSalvo);
        pena.setInstituicaoPrincipal(instSalva);
        pena.setTipoPena("PSC");
        pena.setDataInicio(LocalDate.of(2026, 1, 10));
        pena.setDataTermino(LocalDate.of(2026, 6, 10));
        pena.setHorasTotais(120);
        pena.setHorasSemanais(8);
        pena.setTempoPena(3.75);

        Pena penaSalva = penaRepository.save(pena);
        assertNotNull(penaSalva.getIdPena());

        RegistroDeTrabalho reg = new RegistroDeTrabalho();
        reg.setPena(penaSalva);
        reg.setInstituicao(instSalva);
        reg.setDataTrabalho(LocalDate.of(2026, 1, 15));
        reg.setHorasCumpridas(4.0);
        reg.setAtividades("Manutencao e pintura");

        RegistroDeTrabalho regSalvo = registroRepository.save(reg);
        assertNotNull(regSalvo.getIdRegistro());

        Double horasTotal = registroRepository.somarHorasCumpridasPorPena(penaSalva.getIdPena());
        assertNotNull(horasTotal);
        assertEquals(4.0, horasTotal, 0.001);
    }

    @Test
    @DisplayName("Deve persistir ciclo de codigo de acesso do totem e log de auditoria no PostgreSQL")
    void devePersistirCodigoAcessoELogNoPostgres() {
        Administrador admin = new Administrador();
        admin.setNome("Auditor Totem");
        admin.setCpf("44433322211");
        admin.setSenha("hash");
        admin.setNivelPermissao(1);
        Administrador adminSalvo = administradorRepository.save(admin);

        Usuario user = new Usuario();
        user.setNome("Mariana Souza");
        user.setCpf("55566677788");
        user.setCodigo("APN-PG-002");
        user.setAdministrador(adminSalvo);
        Usuario userSalvo = usuarioRepository.save(user);

        CodigoAcessoApenado codigo = new CodigoAcessoApenado();
        codigo.setUsuario(userSalvo);
        codigo.setAdministrador(adminSalvo);
        codigo.setCodigo("882194");
        codigo.setDataExpiracao(LocalDateTime.now().plusHours(1));
        codigo.setStatus("ATIVO");

        CodigoAcessoApenado codigoSalvo = codigoAcessoRepository.save(codigo);
        assertNotNull(codigoSalvo.getIdCodigoAcesso());

        Optional<CodigoAcessoApenado> ativo = codigoAcessoRepository
                .findFirstByUsuarioIdUsuarioAndStatusOrderByDataGeracaoDesc(userSalvo.getIdUsuario(), "ATIVO");
        assertTrue(ativo.isPresent());
        assertEquals("882194", ativo.get().getCodigo());

        LogAcessoApenado log = new LogAcessoApenado();
        log.setUsuario(userSalvo);
        log.setCodigoAcesso(codigoSalvo);
        log.setMetodo("CODIGO");
        log.setSucesso(1);
        log.setMensagem("Acesso autorizado via totem");

        LogAcessoApenado logSalvo = logAcessoRepository.save(log);
        assertNotNull(logSalvo.getIdLog());
        assertEquals(1, logSalvo.getSucesso());
    }
}
