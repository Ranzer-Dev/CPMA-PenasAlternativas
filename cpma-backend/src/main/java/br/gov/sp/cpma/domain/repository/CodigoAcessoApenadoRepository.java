package br.gov.sp.cpma.domain.repository;

import br.gov.sp.cpma.domain.entity.CodigoAcessoApenado;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodigoAcessoApenadoRepository extends JpaRepository<CodigoAcessoApenado, Long> {

    Optional<CodigoAcessoApenado> findByCodigoAndStatus(String codigo, String status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CodigoAcessoApenado c WHERE c.codigo = :codigo AND c.status = :status")
    Optional<CodigoAcessoApenado> findByCodigoAndStatusWithLock(@Param("codigo") String codigo, @Param("status") String status);

    Optional<CodigoAcessoApenado> findFirstByUsuarioIdUsuarioAndStatusOrderByDataGeracaoDesc(Long idUsuario, String status);
}
