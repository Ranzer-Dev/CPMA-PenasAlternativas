package br.gov.sp.cpma.domain.repository;

import br.gov.sp.cpma.domain.entity.DisponibilidadeInstituicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DisponibilidadeInstituicaoRepository extends JpaRepository<DisponibilidadeInstituicao, Long> {

    List<DisponibilidadeInstituicao> findByInstituicao_IdInstituicao(Long instituicaoId);

    void deleteByInstituicao_IdInstituicao(Long instituicaoId);
}
