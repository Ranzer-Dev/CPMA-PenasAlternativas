package br.gov.sp.cpma.domain.service;

import br.gov.sp.cpma.api.dto.CadastroUsuarioRequest;
import br.gov.sp.cpma.api.dto.UsuarioResponse;
import br.gov.sp.cpma.api.exception.DomainException;
import br.gov.sp.cpma.domain.entity.Administrador;
import br.gov.sp.cpma.domain.entity.Usuario;
import br.gov.sp.cpma.domain.repository.AdministradorRepository;
import br.gov.sp.cpma.domain.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final AdministradorRepository administradorRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, AdministradorRepository administradorRepository) {
        this.usuarioRepository = usuarioRepository;
        this.administradorRepository = administradorRepository;
    }

    @Transactional
    public UsuarioResponse cadastrar(CadastroUsuarioRequest request) {
        if (usuarioRepository.existsByCpf(request.getCpf())) {
            throw new DomainException("CPF_ALREADY_EXISTS", "Ja existe um apenado cadastrado com este CPF.", HttpStatus.CONFLICT);
        }

        if (usuarioRepository.existsByCodigo(request.getCodigo())) {
            throw new DomainException("CODIGO_ALREADY_EXISTS", "Ja existe um apenado cadastrado com este codigo.", HttpStatus.CONFLICT);
        }

        Administrador admin = administradorRepository.findById(request.getAdminId())
                .orElseThrow(() -> new DomainException("ADMIN_NOT_FOUND", "Administrador responsavel nao foi encontrado.", HttpStatus.NOT_FOUND));

        Usuario usuario = new Usuario();
        usuario.setNome(request.getNome());
        usuario.setCpf(request.getCpf());
        usuario.setCodigo(request.getCodigo());
        usuario.setDataNascimento(request.getDataNascimento());
        usuario.setEndereco(request.getEndereco());
        usuario.setBairro(request.getBairro());
        usuario.setCidade(request.getCidade());
        usuario.setCep(request.getCep());
        usuario.setUf(request.getUf());
        usuario.setNacionalidade(request.getNacionalidade());
        usuario.setFoto(request.getFoto());
        usuario.setObservacao(request.getObservacao());
        usuario.setTelefone(request.getTelefone());
        usuario.setAdministrador(admin);

        Usuario salvo = usuarioRepository.save(usuario);
        return new UsuarioResponse(salvo);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listarTodos() {
        return usuarioRepository.findAll().stream()
                .map(UsuarioResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Apenado nao encontrado com o ID fornecido: " + id, HttpStatus.NOT_FOUND));
        return new UsuarioResponse(usuario);
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorCpf(String cpf) {
        Usuario usuario = usuarioRepository.findByCpf(cpf)
                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "Apenado nao encontrado com o CPF fornecido: " + cpf, HttpStatus.NOT_FOUND));
        return new UsuarioResponse(usuario);
    }
}
