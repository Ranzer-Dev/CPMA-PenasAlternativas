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
