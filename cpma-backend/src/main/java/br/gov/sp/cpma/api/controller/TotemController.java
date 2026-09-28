package br.gov.sp.cpma.api.controller;

import br.gov.sp.cpma.api.dto.*;
import br.gov.sp.cpma.domain.service.TotemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/totem")
public class TotemController {

    private final TotemService totemService;

    public TotemController(TotemService totemService) {
        this.totemService = totemService;
    }

    @GetMapping({"", "/"})
    public void redirecionarIndex(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.sendRedirect("/api/v1/totem/index.html");
    }

    @GetMapping("/status")
    public ResponseEntity<java.util.Map<String, String>> status() {
        return ResponseEntity.ok(java.util.Map.of("status", "UP", "service", "cpma-backend"));
    }

    @PostMapping("/token")
    public ResponseEntity<GerarTokenTotemResponse> gerarTokenTotem(@Valid @RequestBody GerarTokenTotemRequest request) {
        GerarTokenTotemResponse response = totemService.gerarTokenTotem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/codigo-temporario")
    public ResponseEntity<CodigoAcessoResponse> gerarCodigoAcesso(@Valid @RequestBody GerarCodigoAcessoRequest request) {
        CodigoAcessoResponse response = totemService.gerarCodigoAcesso(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/validar-codigo")
    public ResponseEntity<ValidarAcessoResponse> validarCodigoAcesso(@Valid @RequestBody ValidarCodigoAcessoRequest request) {
        ValidarAcessoResponse response = totemService.validarCodigoAcesso(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reconhecer")
    public ResponseEntity<ReconhecimentoFacialResponse> reconhecer(
            @RequestHeader("Authorization") String authorizationHeader,
            @Valid @RequestBody ReconhecimentoFacialRequest request) {
        ReconhecimentoFacialResponse response = totemService.processarReconhecimentoFacial(request, authorizationHeader);
        return ResponseEntity.ok(response);
    }
}
