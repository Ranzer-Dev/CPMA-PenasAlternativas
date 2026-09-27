package br.gov.sp.cpma.desktop.util;

import br.gov.sp.cpma.desktop.client.FieldErrorDetail;
import br.gov.sp.cpma.desktop.client.StandardApiError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FormValidatorTest {

    @Test
    @DisplayName("Deve formatar mensagem de erro nula como erro inesperado com status code")
    void deveFormatarErroNulo() {
        String msg = FormValidator.formatarMensagemErro(null, 500);
        assertEquals("Erro inesperado de comunicacao com o servidor (Status HTTP: 500).", msg);
    }

    @Test
    @DisplayName("Deve formatar erro de validacao 422 listando os campos com erro")
    void deveFormatarErroValidacaoComCampos() {
        StandardApiError error = new StandardApiError();
        error.setStatus(422);
        error.setMessage("Campos invalidos");
        error.setErrors(List.of(
                new FieldErrorDetail("cpf", "CPF obrigatorio"),
                new FieldErrorDetail("nome", "Nome muito curto")
        ));

        String msg = FormValidator.formatarMensagemErro(error, 422);
        assertTrue(msg.contains("Campos invalidos"));
        assertTrue(msg.contains("cpf: CPF obrigatorio"));
        assertTrue(msg.contains("nome: Nome muito curto"));
    }

    @Test
    @DisplayName("Deve formatar erro 409 como conflito de dados")
    void deveFormatarErro409Conflito() {
        StandardApiError error = new StandardApiError();
        error.setStatus(409);
        error.setMessage("CPF ja cadastrado no sistema");

        String msg = FormValidator.formatarMensagemErro(error, 409);
        assertEquals("Conflito de Dados (409): CPF ja cadastrado no sistema", msg);
    }

    @Test
    @DisplayName("Deve formatar erro 503 como servico indisponivel")
    void deveFormatarErro503ServicoIndisponivel() {
        StandardApiError error = new StandardApiError();
        error.setStatus(503);
        error.setMessage("Servidor backend offline");

        String msg = FormValidator.formatarMensagemErro(error, 503);
        assertEquals("Servico Indisponivel (503): Servidor backend offline", msg);
    }

    @Test
    @DisplayName("Deve formatar erro 404 como nao encontrado")
    void deveFormatarErro404NaoEncontrado() {
        StandardApiError error = new StandardApiError();
        error.setStatus(404);
        error.setMessage("Apenado nao localizado");

        String msg = FormValidator.formatarMensagemErro(error, 404);
        assertEquals("Registro Nao Encontrado (404): Apenado nao localizado", msg);
    }
}
