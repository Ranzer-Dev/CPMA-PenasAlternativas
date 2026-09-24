package br.gov.sp.cpma.domain.repository;

import br.gov.sp.cpma.domain.entity.Administrador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdministradorRepository extends JpaRepository<Administrador, Long> {

    Optional<Administrador> findByCpf(String cpf);

    boolean existsByCpf(String cpf);
}
