package br.gov.sp.cpma.api.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardApiError {

    private int status;
    private String code;
    private String message;
    private LocalDateTime timestamp;
    private List<FieldErrorDetail> errors;

    public StandardApiError() {
        this.timestamp = LocalDateTime.now();
    }

    public StandardApiError(int status, String code, String message) {
        this();
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public StandardApiError(int status, String code, String message, List<FieldErrorDetail> errors) {
        this(status, code, message);
        this.errors = errors;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<FieldErrorDetail> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldErrorDetail> errors) {
        this.errors = errors;
    }
}
