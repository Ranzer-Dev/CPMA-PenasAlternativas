package br.gov.sp.cpma.desktop.client;

public class ApiResponse<T> {

    private final int statusCode;
    private final T data;
    private final StandardApiError error;

    public ApiResponse(int statusCode, T data) {
        this.statusCode = statusCode;
        this.data = data;
        this.error = null;
    }

    public ApiResponse(int statusCode, StandardApiError error) {
        this.statusCode = statusCode;
        this.data = null;
        this.error = error;
    }

    public boolean isSuccess() {
        return statusCode >= 200 && statusCode < 300;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public T getData() {
        return data;
    }

    public StandardApiError getError() {
        return error;
    }
}
