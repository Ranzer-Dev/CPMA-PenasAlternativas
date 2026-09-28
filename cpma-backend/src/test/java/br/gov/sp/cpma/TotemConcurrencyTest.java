package br.gov.sp.cpma;

import br.gov.sp.cpma.api.dto.ValidarAcessoResponse;
import br.gov.sp.cpma.api.dto.ValidarCodigoAcessoRequest;
import br.gov.sp.cpma.api.exception.DomainException;
import br.gov.sp.cpma.domain.entity.Administrador;
import br.gov.sp.cpma.domain.entity.CodigoAcessoApenado;
import br.gov.sp.cpma.domain.entity.Usuario;
import br.gov.sp.cpma.domain.repository.AdministradorRepository;
import br.gov.sp.cpma.domain.repository.CodigoAcessoApenadoRepository;
import br.gov.sp.cpma.domain.repository.UsuarioRepository;
import br.gov.sp.cpma.domain.service.TotemService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("postgres")
public class TotemConcurrencyTest {

    @Autowired
    private TotemService totemService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private CodigoAcessoApenadoRepository codigoAcessoRepository;

    @Test
    @DisplayName("Deve permitir que apenas 1 thread valide com sucesso o codigo concorrente de uso unico no PostgreSQL")
    void deveGarantirUsoUnicoDeCodigoSobConcorrencia() throws InterruptedException {
        String sufixo = String.valueOf(System.currentTimeMillis() % 100000000);
        String cpfAdmin = String.format("%011d", Math.abs(sufixo.hashCode() % 10000000000L));
        String cpfUser = String.format("%011d", Math.abs((sufixo + "1").hashCode() % 10000000000L));

        Administrador admin = new Administrador();
        admin.setNome("Admin Concorrencia " + sufixo);
        admin.setCpf(cpfAdmin);
        admin.setSenha("hash");
        admin.setNivelPermissao(1);
        Administrador adminSalvo = administradorRepository.save(admin);

        Usuario user = new Usuario();
        user.setNome("Apenado Concorrente " + sufixo);
        user.setCpf(cpfUser);
        user.setCodigo("APN-" + sufixo);
        user.setAdministrador(adminSalvo);
        Usuario userSalvo = usuarioRepository.save(user);

        String codigoUnico = String.format("%06d", Math.abs(UUID.randomUUID().hashCode() % 1000000));
        CodigoAcessoApenado codigo = new CodigoAcessoApenado();
        codigo.setUsuario(userSalvo);
        codigo.setAdministrador(adminSalvo);
        codigo.setCodigo(codigoUnico);
        codigo.setDataExpiracao(LocalDateTime.now().plusHours(1));
        codigo.setStatus("ATIVO");
        CodigoAcessoApenado codigoSalvo = codigoAcessoRepository.save(codigo);

        int totalThreads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(totalThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(totalThreads);

        AtomicInteger sucessos = new AtomicInteger(0);
        AtomicInteger falhas = new AtomicInteger(0);
        List<String> codigosErro = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < totalThreads; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    ValidarCodigoAcessoRequest req = new ValidarCodigoAcessoRequest();
                    req.setCodigo(codigoUnico);

                    ValidarAcessoResponse resp = totemService.validarCodigoAcesso(req);
                    if (resp.isSucesso()) {
                        sucessos.incrementAndGet();
                    }
                } catch (DomainException e) {
                    falhas.incrementAndGet();
                    codigosErro.add(e.getCode());
                } catch (Exception e) {
                    falhas.incrementAndGet();
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(finishLatch.await(10, TimeUnit.SECONDS));
        executor.shutdown();

        assertEquals(1, sucessos.get());
        assertEquals(totalThreads - 1, falhas.get());
        assertTrue(codigosErro.contains("INVALID_OR_EXPIRED_CODE"));

        CodigoAcessoApenado atualizado = codigoAcessoRepository.findById(codigoSalvo.getIdCodigoAcesso()).orElseThrow();
        assertEquals("USADO", atualizado.getStatus());
        assertNotNull(atualizado.getDataUso());
    }
}
