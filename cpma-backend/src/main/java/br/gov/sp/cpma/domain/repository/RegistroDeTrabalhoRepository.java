package br.gov.sp.cpma.domain.repository;

import br.gov.sp.cpma.domain.entity.RegistroDeTrabalho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistroDeTrabalhoRepository extends JpaRepository<RegistroDeTrabalho, Long> {

    List<RegistroDeTrabalho> findByPena_IdPenaOrderByDataTrabalhoDesc(Long penaId);

    @Query("SELECT COALESCE(SUM(r.horasCumpridas), 0.0) FROM RegistroDeTrabalho r WHERE r.pena.idPena = :penaId")
    Double somarHorasCumpridasPorPena(@Param("penaId") Long penaId);
}
