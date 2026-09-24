package br.gov.sp.cpma.domain.repository;

import br.gov.sp.cpma.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByCpf(String cpf);

    Optional<Usuario> findByCodigo(String codigo);

    boolean existsByCpf(String cpf);

    boolean existsByCodigo(String codigo);
}
