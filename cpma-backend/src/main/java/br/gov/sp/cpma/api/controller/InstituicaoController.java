package br.gov.sp.cpma.api.controller;

import br.gov.sp.cpma.api.dto.*;
import br.gov.sp.cpma.domain.service.InstituicaoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instituicoes")
public class InstituicaoController {

    private final InstituicaoService instituicaoService;

    public InstituicaoController(InstituicaoService instituicaoService) {
        this.instituicaoService = instituicaoService;
    }

    @PostMapping
    public ResponseEntity<InstituicaoResponse> cadastrar(@Valid @RequestBody CadastroInstituicaoRequest req) {
        InstituicaoResponse res = instituicaoService.cadastrar(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InstituicaoResponse> atualizar(@PathVariable Long id, @Valid @RequestBody CadastroInstituicaoRequest req) {
        InstituicaoResponse res = instituicaoService.atualizar(id, req);
        return ResponseEntity.ok(res);
    }

    @GetMapping
    public ResponseEntity<List<InstituicaoResponse>> listar() {
        return ResponseEntity.ok(instituicaoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstituicaoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(instituicaoService.buscarPorId(id));
    }

    @PostMapping("/tipos")
    public ResponseEntity<TipoInstituicaoResponse> cadastrarTipo(@Valid @RequestBody CadastroTipoInstituicaoRequest req) {
        TipoInstituicaoResponse res = instituicaoService.cadastrarTipo(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res);
    }

    @GetMapping("/tipos")
    public ResponseEntity<List<TipoInstituicaoResponse>> listarTipos() {
        return ResponseEntity.ok(instituicaoService.listarTipos());
    }
}
