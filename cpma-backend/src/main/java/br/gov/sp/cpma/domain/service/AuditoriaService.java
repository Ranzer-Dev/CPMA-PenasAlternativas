package br.gov.sp.cpma.domain.service;

import br.gov.sp.cpma.api.dto.EstatisticasAuditoriaDTO;
import br.gov.sp.cpma.api.dto.LogAuditoriaDTO;
import br.gov.sp.cpma.domain.entity.LogAcessoApenado;
import br.gov.sp.cpma.domain.entity.Usuario;
import br.gov.sp.cpma.domain.repository.LogAcessoApenadoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class AuditoriaService {

    private final LogAcessoApenadoRepository logAcessoRepository;

    public AuditoriaService(LogAcessoApenadoRepository logAcessoRepository) {
        this.logAcessoRepository = logAcessoRepository;
    }

    @Transactional(readOnly = true)
    public List<LogAuditoriaDTO> listarLogs(Integer limite, String termo, Boolean apenasFalhas) {
        List<LogAcessoApenado> logs;
        if (Boolean.TRUE.equals(apenasFalhas)) {
            logs = logAcessoRepository.findBySucessoOrderByDataHoraDesc(0);
        } else {
            logs = logAcessoRepository.findAllByOrderByDataHoraDesc();
        }

        String termoLower = termo != null ? termo.trim().toLowerCase(Locale.ROOT) : "";

        return logs.stream()
                .filter(log -> {
                    if (termoLower.isEmpty()) {
                        return true;
                    }
                    String nome = log.getUsuario() != null && log.getUsuario().getNome() != null ? log.getUsuario().getNome().toLowerCase(Locale.ROOT) : "";
                    String cpf = log.getUsuario() != null && log.getUsuario().getCpf() != null ? log.getUsuario().getCpf().toLowerCase(Locale.ROOT) : "";
                    String codigoTentado = log.getCodigoTentado() != null ? log.getCodigoTentado().toLowerCase(Locale.ROOT) : "";
                    String mensagem = log.getMensagem() != null ? log.getMensagem().toLowerCase(Locale.ROOT) : "";
                    String metodo = log.getMetodo() != null ? log.getMetodo().toLowerCase(Locale.ROOT) : "";

                    return nome.contains(termoLower) || cpf.contains(termoLower) || codigoTentado.contains(termoLower) || mensagem.contains(termoLower) || metodo.contains(termoLower);
                })
                .limit(limite != null && limite > 0 ? limite : 150)
                .map(this::converterParaDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public EstatisticasAuditoriaDTO obterEstatisticas() {
        List<LogAcessoApenado> todos = logAcessoRepository.findAll();
        long total = todos.size();
        long sucessos = todos.stream().filter(l -> Integer.valueOf(1).equals(l.getSucesso())).count();
        long falhas = todos.stream().filter(l -> Integer.valueOf(0).equals(l.getSucesso())).count();

        long falhasBiometricas = todos.stream()
                .filter(l -> Integer.valueOf(0).equals(l.getSucesso()))
                .filter(l -> {
                    String metodo = l.getMetodo() != null ? l.getMetodo() : "";
                    String msg = l.getMensagem() != null ? l.getMensagem().toLowerCase(Locale.ROOT) : "";
                    return metodo.contains("BIOMETRIA") || metodo.contains("FACIAL") || msg.contains("biometric") || msg.contains("rosto");
                })
                .count();

        long falhasCodigo = Math.max(0, falhas - falhasBiometricas);

        return new EstatisticasAuditoriaDTO(total, sucessos, falhas, falhasBiometricas, falhasCodigo);
    }

    private LogAuditoriaDTO converterParaDto(LogAcessoApenado log) {
        LogAuditoriaDTO dto = new LogAuditoriaDTO();
        dto.setIdLog(log.getIdLog());
        dto.setDataHora(log.getDataHora());
        dto.setSucesso(Integer.valueOf(1).equals(log.getSucesso()));
        dto.setMetodo(log.getMetodo());
        dto.setCodigoTentado(log.getCodigoTentado());
        dto.setMensagem(log.getMensagem());

        Usuario u = log.getUsuario();
        if (u != null) {
            dto.setUsuarioId(u.getIdUsuario());
            dto.setUsuarioNome(u.getNome());
            dto.setUsuarioCpf(u.getCpf());
            dto.setUsuarioMatricula(u.getCodigo());
        }

        if (Boolean.TRUE.equals(dto.isSucesso())) {
            dto.setNivelRisco("NORMAL");
        } else {
            String msg = log.getMensagem() != null ? log.getMensagem().toLowerCase(Locale.ROOT) : "";
            String metodo = log.getMetodo() != null ? log.getMetodo() : "";
            if (msg.contains("rosto") || msg.contains("biometric") || metodo.contains("BIOMETRIA")) {
                dto.setNivelRisco("CRITICO");
            } else {
                dto.setNivelRisco("ALERTA");
            }
        }

        return dto;
    }
}
