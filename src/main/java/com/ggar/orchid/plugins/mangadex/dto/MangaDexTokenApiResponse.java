package com.ggar.orchid.plugins.mangadex.dto; 

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * DTO para deserializar la respuesta JSON del endpoint de tokens de MangaDex.
 * Utiliza anotaciones de Jackson para mapear los campos snake_case del JSON
 * a campos camelCase en Java.
 */
@JsonIgnoreProperties(ignoreUnknown = true) 
public class MangaDexTokenApiResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken; 

    @JsonProperty("expires_in")
    private long expiresIn; 

    public MangaDexTokenApiResponse() {
    }

    public MangaDexTokenApiResponse(String accessToken, String refreshToken, long expiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(long expiresIn) {
        this.expiresIn = expiresIn;
    }

    @Override
    public String toString() {
        return "MangaDexTokenApiResponse{" +
                "accessToken='********'" + 
                ", refreshToken='" + (refreshToken != null ? "********" : "null") + "'" +
                ", expiresIn=" + expiresIn +
                '}';
    }
}
