package br.gov.sp.cpma.domain.service;

import br.gov.sp.cpma.domain.entity.*;
import br.gov.sp.cpma.domain.repository.*;
import br.gov.sp.cpma.domain.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSeeder.class);

    private final AdministradorRepository adminRepository;
    private final TipoInstituicaoRepository tipoInstituicaoRepository;
    private final InstituicaoRepository instituicaoRepository;
    private final UsuarioRepository usuarioRepository;
    private final PenaRepository penaRepository;
    private final RegistroDeTrabalhoRepository registroTrabalhoRepository;
    private final CodigoAcessoApenadoRepository codigoAcessoRepository;

    public DatabaseSeeder(
            AdministradorRepository adminRepository,
            TipoInstituicaoRepository tipoInstituicaoRepository,
            InstituicaoRepository instituicaoRepository,
            UsuarioRepository usuarioRepository,
            PenaRepository penaRepository,
            RegistroDeTrabalhoRepository registroTrabalhoRepository,
            CodigoAcessoApenadoRepository codigoAcessoRepository) {
        this.adminRepository = adminRepository;
        this.tipoInstituicaoRepository = tipoInstituicaoRepository;
        this.instituicaoRepository = instituicaoRepository;
        this.usuarioRepository = usuarioRepository;
        this.penaRepository = penaRepository;
        this.registroTrabalhoRepository = registroTrabalhoRepository;
        this.codigoAcessoRepository = codigoAcessoRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (adminRepository.count() > 0) {
            log.info("Base de dados ja contem registros cadastrados. Pulando seed inicial.");
            return;
        }

        log.info("Primeira inicializacao detectada! Populando base de dados com registros essenciais...");

        Administrador admin = new Administrador();
        admin.setNome("Administrador Geral");
        admin.setCpf("12345678900");
        admin.setSenha(PasswordHasher.hash("admin123"));
        admin.setNivelPermissao(1);
        admin.setPerguntaSecreta("Qual é a cor do seu cavalo branco?");
        admin.setRespostaSecreta("Branco");
        admin.setCriadoEm(LocalDateTime.now());
        admin = adminRepository.save(admin);

        TipoInstituicao tipoEducacional = new TipoInstituicao();
        tipoEducacional.setTipo("Educacional / Ensino Superior");
        tipoEducacional.setCriadoEm(LocalDateTime.now());
        tipoEducacional = tipoInstituicaoRepository.save(tipoEducacional);

        Instituicao instituicao = new Instituicao();
        instituicao.setNome("Fatec Benedicto Plagliato");
        instituicao.setResponsavel("Mauro Tomazela");
        instituicao.setTipo(tipoEducacional);
        instituicao.setCidade("Sorocaba");
        instituicao.setUf("SP");
        instituicao.setEndereco("Avenida Engenheiro Carlos Reinaldo Mendes, 2015");
        instituicao.setBairro("Alto da Boa Vista");
        instituicao.setCep("18013-280");
        instituicao.setTelefone("1532385266");
        instituicao.setCriadoEm(LocalDateTime.now());
        instituicao = instituicaoRepository.save(instituicao);

        Usuario apenado = new Usuario();
        apenado.setNome("Apenado Teste Homologacao");
        apenado.setCpf("11122233344");
        apenado.setCodigo("1a");
        apenado.setTelefone("11999999999");
        apenado.setDataNascimento("1995-05-15");
        apenado.setCidade("Sao Paulo");
        apenado.setUf("SP");
        apenado.setEndereco("Avenida Paulista, 1000");
        apenado.setBairro("Bela Vista");
        apenado.setCep("01310-100");
        apenado.setNacionalidade("Brasileira");
        apenado.setObservacao("Cadastro padrao para ambiente de homologacao e testes");
        apenado.setAdministrador(admin);
        apenado.setCriadoEm(LocalDateTime.now());
        apenado = usuarioRepository.save(apenado);

        Pena pena = new Pena();
        pena.setUsuario(apenado);
        pena.setInstituicaoPrincipal(instituicao);
        pena.setInstituicoesVinculadas(Collections.singletonList(instituicao));
        pena.setTipoPena("Prestação de Serviços à Comunidade (PSC)");
        pena.setDescricao("Manutenção, jardinagem e apoio administrativo comunitário");
        pena.setAtividadesAcordadas("Auxílio na biblioteca e manutenção predial");
        pena.setDiasSemanaEHorariosDisponivel("Sábados das 08:00 às 17:00");
        pena.setDataInicio(LocalDate.now().minusWeeks(2));
        pena.setDataTermino(LocalDate.now().plusMonths(6));
        pena.setHorasSemanais(8);
        pena.setHorasTotais(120);
        pena.setTempoPena(6.0);
        pena.setCriadoEm(LocalDateTime.now());
        pena = penaRepository.save(pena);

        RegistroDeTrabalho registro = new RegistroDeTrabalho();
        registro.setPena(pena);
        registro.setInstituicao(instituicao);
        registro.setDataTrabalho(LocalDate.now().minusDays(7));
        registro.setHorasCumpridas(8.0);
        registro.setAtividades("Manutenção e organização de acervo");
        registro.setHorarioInicio("08:00");
        registro.setHorarioAlmoco("12:00");
        registro.setHorarioVolta("13:00");
        registro.setHorarioSaida("17:00");
        registro.setCriadoEm(LocalDateTime.now());
        registroTrabalhoRepository.save(registro);

        CodigoAcessoApenado codigoAcesso = new CodigoAcessoApenado();
        codigoAcesso.setUsuario(apenado);
        codigoAcesso.setAdministrador(admin);
        codigoAcesso.setCodigo("123456");
        codigoAcesso.setDataGeracao(LocalDateTime.now());
        codigoAcesso.setDataExpiracao(LocalDateTime.now().plusMonths(3));
        codigoAcesso.setStatus("ATIVO");
        codigoAcessoRepository.save(codigoAcesso);

        log.info("Base de dados populada com sucesso! Admin padrao: CPF 12345678900 / Senha admin123 | PIN Totem: 123456");
    }
}
