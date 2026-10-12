package br.gov.sp.cpma.api.controller;

import br.gov.sp.cpma.api.dto.EstatisticasAuditoriaDTO;
import br.gov.sp.cpma.api.dto.LogAuditoriaDTO;
import br.gov.sp.cpma.domain.service.AuditoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/auditoria")
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    public AuditoriaController(AuditoriaService auditoriaService) {
        this.auditoriaService = auditoriaService;
    }

    @GetMapping("/logs")
    public ResponseEntity<List<LogAuditoriaDTO>> listarLogs(
            @RequestParam(required = false) Integer limite,
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) Boolean apenasFalhas) {
        List<LogAuditoriaDTO> logs = auditoriaService.listarLogs(limite, termo, apenasFalhas);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/estatisticas")
    public ResponseEntity<EstatisticasAuditoriaDTO> obterEstatisticas() {
        EstatisticasAuditoriaDTO stats = auditoriaService.obterEstatisticas();
        return ResponseEntity.ok(stats);
    }
}
