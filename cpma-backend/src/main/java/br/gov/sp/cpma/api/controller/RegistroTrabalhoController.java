package br.gov.sp.cpma.api.controller;

import br.gov.sp.cpma.api.dto.CadastroRegistroTrabalhoRequest;
import br.gov.sp.cpma.api.dto.RegistroTrabalhoResponse;
import br.gov.sp.cpma.api.dto.ResumoCumprimentoResponse;
import br.gov.sp.cpma.domain.service.RegistroTrabalhoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/registros-trabalho")
public class RegistroTrabalhoController {

    private final RegistroTrabalhoService registroService;

    public RegistroTrabalhoController(RegistroTrabalhoService registroService) {
        this.registroService = registroService;
    }

    @PostMapping
    public ResponseEntity<RegistroTrabalhoResponse> registrar(@Valid @RequestBody CadastroRegistroTrabalhoRequest req) {
        RegistroTrabalhoResponse res = registroService.registrar(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/pena/{penaId}")
    public ResponseEntity<List<RegistroTrabalhoResponse>> listarPorPena(@PathVariable Long penaId) {
        return ResponseEntity.ok(registroService.listarPorPena(penaId));
    }

    @GetMapping("/pena/{penaId}/resumo")
    public ResponseEntity<ResumoCumprimentoResponse> obterResumo(@PathVariable Long penaId) {
        return ResponseEntity.ok(registroService.obterResumo(penaId));
    }
}
