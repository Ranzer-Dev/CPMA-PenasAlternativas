package br.gov.sp.cpma.domain.repository;

import br.gov.sp.cpma.domain.entity.CodigoAcessoApenado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodigoAcessoApenadoRepository extends JpaRepository<CodigoAcessoApenado, Long> {

    Optional<CodigoAcessoApenado> findByCodigoAndStatus(String codigo, String status);

    Optional<CodigoAcessoApenado> findFirstByUsuarioIdUsuarioAndStatusOrderByDataGeracaoDesc(Long idUsuario, String status);
}
