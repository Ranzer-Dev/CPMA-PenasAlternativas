package br.gov.sp.cpma;

import br.gov.sp.cpma.api.exception.DomainException;
import br.gov.sp.cpma.api.exception.GlobalExceptionHandler;
import br.gov.sp.cpma.api.exception.StandardApiError;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    @DisplayName("Deve capturar DomainException e retornar status e codigo correspondentes")
    void deveTratarDomainException() {
        DomainException ex = new DomainException("RESOURCE_NOT_FOUND", "Recurso nao localizado", HttpStatus.NOT_FOUND);

        ResponseEntity<StandardApiError> response = handler.handleDomainException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(404, response.getBody().getStatus());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getCode());
        assertEquals("Recurso nao localizado", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Deve capturar Exception generica e retornar status 500 com mensagem")
    void deveTratarExceptionGenericaComMensagem() {
        Exception ex = new RuntimeException("Falha inesperada no subsistema de disco");

        ResponseEntity<StandardApiError> response = handler.handleGenericException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getCode());
        assertEquals("Falha inesperada no subsistema de disco", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Deve capturar Exception generica com mensagem nula e aplicar mensagem padrao")
    void deveTratarExceptionGenericaComMensagemNula() {
        Exception ex = new NullPointerException();

        ResponseEntity<StandardApiError> response = handler.handleGenericException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(500, response.getBody().getStatus());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getCode());
        assertEquals("Ocorreu um erro interno inesperado no servidor.", response.getBody().getMessage());
    }

    @Test
    @DisplayName("Deve capturar MethodArgumentNotValidException e retornar status 422 com lista de campos")
    void deveTratarMethodArgumentNotValidException() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "objetoTeste");
        bindingResult.addError(new FieldError("objetoTeste", "cpf", "CPF invalido"));
        bindingResult.addError(new FieldError("objetoTeste", "nome", "Nome obrigatorio"));

        MethodParameter parameter = new MethodParameter(this.getClass().getDeclaredMethod("metodoParaParametro", String.class), 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(parameter, bindingResult);

        ResponseEntity<StandardApiError> response = handler.handleValidationException(ex);

        assertNotNull(response);
        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(422, response.getBody().getStatus());
        assertEquals("VALIDATION_FAILED", response.getBody().getCode());
        assertEquals(2, response.getBody().getErrors().size());
        assertEquals("cpf", response.getBody().getErrors().get(0).getField());
        assertEquals("CPF invalido", response.getBody().getErrors().get(0).getMessage());
    }

    public void metodoParaParametro(String valor) {}
}
