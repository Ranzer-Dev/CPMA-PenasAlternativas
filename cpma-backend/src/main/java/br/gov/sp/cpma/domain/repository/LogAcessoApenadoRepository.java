package br.gov.sp.cpma.domain.repository;

import br.gov.sp.cpma.domain.entity.LogAcessoApenado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogAcessoApenadoRepository extends JpaRepository<LogAcessoApenado, Long> {

    List<LogAcessoApenado> findTop50ByOrderByDataHoraDesc();

    List<LogAcessoApenado> findByUsuarioIdUsuarioOrderByDataHoraDesc(Long idUsuario);
}
