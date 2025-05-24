package com.ggar.orchid.plugins.mangadex.dto;

import java.time.Instant;

public class MangaDexAuthTokens {
    private final String accessToken;
    private final String refreshToken; // Puede ser null si no se proporciona o no es aplicable
    private final Instant expiryTime;   // Momento exacto en que expira el accessToken

    /**
     * Constructor para MangaDexAuthTokens.
     *
     * @param accessToken El token de acceso. No puede ser nulo ni vacío.
     * @param refreshToken El token de refresco. Puede ser nulo.
     * @param expiryTime El momento exacto de expiración del token de acceso. No puede ser nulo.
     * @throws IllegalArgumentException si accessToken o expiryTime son nulos, o si accessToken está vacío.
     */
    public MangaDexAuthTokens(String accessToken, String refreshToken, Instant expiryTime) {
        if (accessToken == null || accessToken.trim().isEmpty()) {
            throw new IllegalArgumentException("Access token cannot be null or empty.");
        }
        if (expiryTime == null) {
            throw new IllegalArgumentException("Expiry time cannot be null.");
        }
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.expiryTime = expiryTime;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Instant getExpiryTime() {
        return expiryTime;
    }

    /**
     * Comprueba si el token de acceso ha expirado o está a punto de expirar
     * dentro de un margen de seguridad especificado.
     *
     * @param safetyMarginSeconds Un margen de seguridad en segundos. Si el token expira
     * dentro de este margen, se considera "expirado" para
     * propósitos de refresco proactivo.
     * @return true si el token de acceso se considera expirado (o a punto de expirar
     * según el margen), false en caso contrario.
     */
    public boolean isAccessTokenExpired(long safetyMarginSeconds) {
        if (this.expiryTime == null) {
            return true;
        }
        return Instant.now().isAfter(this.expiryTime.minusSeconds(safetyMarginSeconds));
    }

    /**
     * Comprueba si el token de acceso ha expirado estrictamente (sin margen de seguridad).
     * @return true si el token de acceso ha expirado, false en caso contrario.
     */
    public boolean isAccessTokenStrictlyExpired() {
        return isAccessTokenExpired(0);
    }


    @Override
    public String toString() {
        return "MangaDexAuthTokens{" +
                "accessToken='********'" + // Evitar loguear el token completo por seguridad
                ", refreshToken='" + (refreshToken != null ? "********" : "null") + "'" +
                ", expiryTime=" + expiryTime +
                '}';
    }
}
