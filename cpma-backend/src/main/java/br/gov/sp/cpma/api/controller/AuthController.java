package br.gov.sp.cpma.api.controller;

import br.gov.sp.cpma.api.dto.LoginRequest;
import br.gov.sp.cpma.api.dto.LoginResponse;
import br.gov.sp.cpma.api.dto.RedefinirSenhaRequest;
import br.gov.sp.cpma.domain.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResponse res = authService.login(req);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/redefinir-senha")
    public ResponseEntity<Map<String, String>> redefinirSenha(@Valid @RequestBody RedefinirSenhaRequest req) {
        authService.redefinirSenha(req);
        return ResponseEntity.ok(Map.of("message", "Senha redefinida com sucesso."));
    }
}
