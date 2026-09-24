package br.gov.sp.cpma.domain.repository;

import br.gov.sp.cpma.domain.entity.TipoInstituicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TipoInstituicaoRepository extends JpaRepository<TipoInstituicao, Long> {

    Optional<TipoInstituicao> findByTipo(String tipo);
}
