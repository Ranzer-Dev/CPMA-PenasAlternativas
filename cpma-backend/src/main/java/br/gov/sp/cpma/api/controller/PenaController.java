package br.gov.sp.cpma.api.controller;

import br.gov.sp.cpma.api.dto.CadastroPenaRequest;
import br.gov.sp.cpma.api.dto.EstimativaPenaRequest;
import br.gov.sp.cpma.api.dto.EstimativaPenaResponse;
import br.gov.sp.cpma.api.dto.PenaResponse;
import br.gov.sp.cpma.domain.service.PenaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/penas")
public class PenaController {

    private final PenaService penaService;

    public PenaController(PenaService penaService) {
        this.penaService = penaService;
    }

    @PostMapping
    public ResponseEntity<PenaResponse> cadastrar(@Valid @RequestBody CadastroPenaRequest req) {
        PenaResponse res = penaService.cadastrar(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PenaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(penaService.buscarPorId(id));
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<List<PenaResponse>> listarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(penaService.listarPorUsuario(usuarioId));
    }

    @PostMapping("/estimativa")
    public ResponseEntity<EstimativaPenaResponse> calcularEstimativa(@Valid @RequestBody EstimativaPenaRequest req) {
        return ResponseEntity.ok(penaService.calcularEstimativa(req));
    }
}
