package br.gov.sp.cpma.api.dto;

public class GerarTokenTotemResponse {

    private String terminalId;
    private String token;
    private String tokenType;

    public GerarTokenTotemResponse() {}

    public GerarTokenTotemResponse(String terminalId, String token) {
        this.terminalId = terminalId;
        this.token = token;
        this.tokenType = "Bearer";
    }

    public String getTerminalId() {
        return terminalId;
    }

    public void setTerminalId(String terminalId) {
        this.terminalId = terminalId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }
}
