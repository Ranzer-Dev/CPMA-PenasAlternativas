package br.gov.sp.cpma.desktop.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenTotemDTO {

    private String terminalId;
    private String token;
    private String tokenType;

    public TokenTotemDTO() {}

    public TokenTotemDTO(String terminalId, String token, String tokenType) {
        this.terminalId = terminalId;
        this.token = token;
        this.tokenType = tokenType;
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
