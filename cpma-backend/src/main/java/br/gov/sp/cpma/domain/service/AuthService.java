package br.gov.sp.cpma.domain.service;

import br.gov.sp.cpma.api.dto.LoginRequest;
import br.gov.sp.cpma.api.dto.LoginResponse;
import br.gov.sp.cpma.api.dto.RedefinirSenhaRequest;
import br.gov.sp.cpma.api.exception.DomainException;
import br.gov.sp.cpma.domain.entity.Administrador;
import br.gov.sp.cpma.domain.repository.AdministradorRepository;
import br.gov.sp.cpma.domain.util.PasswordHasher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AdministradorRepository adminRepository;
    private final TokenService tokenService;

    public AuthService(AdministradorRepository adminRepository, TokenService tokenService) {
        this.adminRepository = adminRepository;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        String cleanCpf = req.getCpf().replaceAll("\\D", "");

        Administrador admin = adminRepository.findByCpf(cleanCpf)
                .or(() -> adminRepository.findByCpf(req.getCpf()))
                .orElseThrow(() -> new DomainException("INVALID_CREDENTIALS", "CPF ou senha incorretos.", HttpStatus.UNAUTHORIZED));

        if (!PasswordHasher.matches(req.getSenha(), admin.getSenha())) {
            throw new DomainException("INVALID_CREDENTIALS", "CPF ou senha incorretos.", HttpStatus.UNAUTHORIZED);
        }

        String token = tokenService.gerarTokenAdmin(
                admin.getIdAdmin(),
                admin.getCpf(),
                admin.getNome(),
                admin.getNivelPermissao() != null ? admin.getNivelPermissao() : 1
        );

        return new LoginResponse(
                admin.getIdAdmin(),
                admin.getNome(),
                admin.getCpf(),
                admin.getNivelPermissao(),
                token
        );
    }

    @Transactional
    public void redefinirSenha(RedefinirSenhaRequest req) {
        String cleanCpf = req.getCpf().replaceAll("\\D", "");

        Administrador admin = adminRepository.findByCpf(cleanCpf)
                .or(() -> adminRepository.findByCpf(req.getCpf()))
                .orElseThrow(() -> new DomainException("ADMIN_NOT_FOUND", "Administrador nao encontrado para o CPF informado.", HttpStatus.NOT_FOUND));

        if (admin.getRespostaSecreta() == null || !admin.getRespostaSecreta().trim().equalsIgnoreCase(req.getRespostaSecreta().trim())) {
            throw new DomainException("INVALID_SECRET_ANSWER", "Resposta secreta incorreta.", HttpStatus.UNAUTHORIZED);
        }

        admin.setSenha(PasswordHasher.hash(req.getNovaSenha()));
        adminRepository.save(admin);
    }
}
