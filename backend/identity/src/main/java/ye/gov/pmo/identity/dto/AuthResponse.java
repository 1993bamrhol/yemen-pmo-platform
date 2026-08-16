package ye.gov.pmo.identity.dto;

import java.util.Set;

public class AuthResponse {

    private String token;
    private String tokenType;
    private String username;
    private Set<String> roles;

    public AuthResponse() {
    }

    public AuthResponse(String token, String tokenType, String username, Set<String> roles) {
        this.token = token;
        this.tokenType = tokenType;
        this.username = username;
        this.roles = roles;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRoles(Set<String> roles) {
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
