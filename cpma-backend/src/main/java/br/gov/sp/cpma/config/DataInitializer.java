package br.gov.sp.cpma.config;

import br.gov.sp.cpma.domain.entity.Administrador;
import br.gov.sp.cpma.domain.entity.TipoInstituicao;
import br.gov.sp.cpma.domain.repository.AdministradorRepository;
import br.gov.sp.cpma.domain.repository.TipoInstituicaoRepository;
import br.gov.sp.cpma.domain.util.PasswordHasher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final AdministradorRepository administradorRepository;
    private final TipoInstituicaoRepository tipoInstituicaoRepository;

    public DataInitializer(AdministradorRepository administradorRepository,
                           TipoInstituicaoRepository tipoInstituicaoRepository) {
        this.administradorRepository = administradorRepository;
        this.tipoInstituicaoRepository = tipoInstituicaoRepository;
    }

    @Override
    public void run(String... args) {
        try {
            if (administradorRepository.findByCpf("12345678900").isEmpty()) {
                Administrador admin = new Administrador();
                admin.setNome("Administrador");
                admin.setCpf("12345678900");
                admin.setSenha(PasswordHasher.hash("admin123"));
                admin.setNivelPermissao(1);
                admin.setPerguntaSecreta("Qual é a cor do seu cavalo branco?");
                admin.setRespostaSecreta("Branco");
                administradorRepository.save(admin);
                log.info("Administrador padrão inicializado com sucesso (CPF: 12345678900).");
            }

            if (tipoInstituicaoRepository.count() == 0) {
                List<String> tiposPadrao = List.of(
                        "Filantrópica",
                        "Pública Municipal",
                        "ONG / Assistencial",
                        "Saúde / Hospitalar",
                        "Educacional"
                );
                for (String nomeTipo : tiposPadrao) {
                    TipoInstituicao t = new TipoInstituicao();
                    t.setTipo(nomeTipo);
                    tipoInstituicaoRepository.save(t);
                }
                log.info("Tipos de instituição padrão inicializados ({} registros).", tiposPadrao.size());
            }
        } catch (Exception e) {
            log.warn("Aviso ao inicializar dados padrão do banco: {}", e.getMessage());
        }
    }
}
