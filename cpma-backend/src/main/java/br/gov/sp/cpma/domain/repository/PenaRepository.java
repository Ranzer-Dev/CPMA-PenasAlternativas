package br.gov.sp.cpma.domain.repository;

import br.gov.sp.cpma.domain.entity.Pena;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PenaRepository extends JpaRepository<Pena, Long> {

    List<Pena> findByUsuario_IdUsuarioOrderByDataInicioDesc(Long usuarioId);

    int countByUsuario_IdUsuario(Long usuarioId);
}
