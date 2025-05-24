package com.ggar.orchid.plugins.mangadex;

import com.ggar.orchid.plugins.mangadex.dto.MangaDexAuthTokens;
import com.ggar.orchid.plugins.mangadex.dto.MangaDexTokenApiResponse;
import com.ggar.orchid.plugins.mangadex.http.MangaDexHttpTransport;
import jakarta.annotation.Nullable;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class MangaDexAuth {
    private static final Logger logger = Logger.getLogger(MangaDexAuth.class.getName());
    private static final String TOKEN_URL = "https://auth.mangadex.org/realms/mangadex/protocol/openid-connect/token";
    private static final String GRANT_TYPE_CLIENT_CREDENTIALS = "client_credentials";
    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final String GRANT_TYPE_REFRESH_TOKEN = "refresh_token";

    public static Optional<MangaDexAuthTokens> login(String grantType, String clientId, String clientSecret, @Nullable String username, @Nullable String password) {
        if (!hasText(grantType) || !hasText(clientId) || !hasText(clientSecret)) { logger.severe("Parámetros login faltantes."); return Optional.empty(); }
        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put("client_id", clientId); bodyParams.put("client_secret", clientSecret);
        if (GRANT_TYPE_PASSWORD.equalsIgnoreCase(grantType)) {
            if (!hasText(username) || !hasText(password)) { logger.severe("Credenciales password grant faltantes."); return Optional.empty(); }
            bodyParams.put("grant_type", GRANT_TYPE_PASSWORD); bodyParams.put("username", username); bodyParams.put("password", password);
        } else if (GRANT_TYPE_CLIENT_CREDENTIALS.equalsIgnoreCase(grantType)) {
            bodyParams.put("grant_type", GRANT_TYPE_CLIENT_CREDENTIALS);
        } else { logger.severe("Grant type no soportado para login: " + grantType); return Optional.empty(); }
        if (bodyParams.containsKey("grant_type")) logger.info("Intentando login con grant '" + bodyParams.get("grant_type") + "'.");
        Optional<MangaDexTokenApiResponse> apiResponseOpt = MangaDexHttpTransport.postAuthForm(TOKEN_URL, bodyParams);
        return apiResponseOpt.map(r -> new MangaDexAuthTokens(r.getAccessToken(), r.getRefreshToken(), Instant.now().plusSeconds(r.getExpiresIn())));
    }

    public static Optional<MangaDexAuthTokens> refreshToken(String clientId, String clientSecret, String refreshTokenValue) {
        if (!hasText(clientId) || !hasText(clientSecret)) { logger.severe("Parámetros refresh faltantes."); return Optional.empty(); }
        if (!hasText(refreshTokenValue)) { logger.severe("Refresh token vacío."); return Optional.empty(); }
        Map<String, String> bodyParams = new HashMap<>();
        bodyParams.put("grant_type", GRANT_TYPE_REFRESH_TOKEN); bodyParams.put("refresh_token", refreshTokenValue);
        bodyParams.put("client_id", clientId); bodyParams.put("client_secret", clientSecret);
        logger.info("Intentando refrescar token.");
        Optional<MangaDexTokenApiResponse> apiResponseOpt = MangaDexHttpTransport.postAuthForm(TOKEN_URL, bodyParams);
        return apiResponseOpt.map(r -> new MangaDexAuthTokens(r.getAccessToken(), hasText(r.getRefreshToken()) ? r.getRefreshToken() : refreshTokenValue, Instant.now().plusSeconds(r.getExpiresIn())));
    }
    private static boolean hasText(String str) { return str != null && !str.trim().isEmpty(); }
}