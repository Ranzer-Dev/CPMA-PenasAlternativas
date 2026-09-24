package br.gov.sp.cpma.domain.repository;

import br.gov.sp.cpma.domain.entity.Instituicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InstituicaoRepository extends JpaRepository<Instituicao, Long> {

    List<Instituicao> findByOrderByNomeAsc();

    Optional<Instituicao> findByNome(String nome);
}
