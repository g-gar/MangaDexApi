package com.ggar.orchid.plugins.mangadex.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ggar.orchid.plugins.mangadex.dto.MangaDexTokenApiResponse;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * MangaDexHttpTransport provides static methods for making HTTP requests to the MangaDex API.
 * It includes a global rate limiter to ensure compliance with API usage policies.
 * The rate limit is applied to all requests made through this transport.
 * It supports GET, POST (form-urlencoded for auth, JSON for general API), and byte array downloads.
 */
public class MangaDexHttpTransport {

    private static final Logger logger = Logger.getLogger(MangaDexHttpTransport.class.getName());
    private static final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    /**
     * Shared ObjectMapper instance for JSON processing. It's thread-safe.
     */
    public static final ObjectMapper objectMapper = new ObjectMapper();

    
    private static final Semaphore GLOBAL_REQUEST_PERMIT = new Semaphore(1);
    private static volatile long lastRequestStartTimeMs = 0; 
    private static long configuredGlobalIntervalMs = 500L; 

    /**
     * Configures the global minimum interval between ANY HTTP requests made to MangaDex via this transport.
     * This should be called once during application setup.
     *
     * @param intervalMs The interval in milliseconds. Must be positive.
     */
    public static synchronized void configureGlobalRequestInterval(long intervalMs) {
        if (intervalMs > 0) {
            configuredGlobalIntervalMs = intervalMs;
            logger.info("Global HTTP request interval for MangaDex set to: " + intervalMs + " ms");
        } else {
            logger.warning("Attempted to configure a non-positive global request interval: " + intervalMs +
                    " ms. Current value remains: " + configuredGlobalIntervalMs + " ms.");
        }
    }

    /**
     * Applies the configured global rate limit delay.
     * This method must be called while holding the GLOBAL_REQUEST_PERMIT.
     *
     * @throws InterruptedException if the thread is interrupted while sleeping.
     */
    private static void applyGlobalRateLimitDelay() throws InterruptedException {
        if (lastRequestStartTimeMs != 0) { 
            long currentTime = System.currentTimeMillis();
            long timeSinceLastStart = currentTime - lastRequestStartTimeMs;
            if (timeSinceLastStart < configuredGlobalIntervalMs) {
                long sleepDuration = configuredGlobalIntervalMs - timeSinceLastStart;
                if (sleepDuration > 0) { 
                    logger.fine("Global rate limiting. Sleeping for " + sleepDuration + " ms.");
                    Thread.sleep(sleepDuration);
                }
            }
        }
        lastRequestStartTimeMs = System.currentTimeMillis(); 
    }

    /**
     * Executes an HTTP POST request with a form-urlencoded body, typically used for authentication.
     * Applies global rate limiting.
     *
     * @param urlString The target URL.
     * @param formParameters A map of parameters to be form-urlencoded into the request body.
     * @return An Optional containing the deserialized {@link MangaDexTokenApiResponse} on success, or empty on failure.
     */
    public static Optional<MangaDexTokenApiResponse> postAuthForm(String urlString, Map<String, String> formParameters) {
        try {
            GLOBAL_REQUEST_PERMIT.acquire();
            try {
                applyGlobalRateLimitDelay();

                String formBody = formParameters.entrySet().stream()
                        .map(p -> URLEncoder.encode(p.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(p.getValue(), StandardCharsets.UTF_8))
                        .collect(Collectors.joining("&"));
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(urlString))
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .header("User-Agent", "MangaDexClient")
                        .POST(HttpRequest.BodyPublishers.ofString(formBody))
                        .build();

                logger.fine("Sending POST (auth form) to: " + urlString);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (isSuccess(response.statusCode())) {
                    return Optional.of(objectMapper.readValue(response.body(), MangaDexTokenApiResponse.class));
                } else {
                    logError(response, "postAuthForm", urlString);
                    return Optional.empty();
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); logger.log(Level.WARNING, "Auth form POST request interrupted: " + urlString, e); return Optional.empty(); }
            catch (IOException e) { logger.log(Level.SEVERE, "IOException during auth form POST to " + urlString, e); return Optional.empty(); }
            catch (Exception e) { logger.log(Level.SEVERE, "Unexpected exception during auth form POST to " + urlString, e); return Optional.empty(); }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); logger.log(Level.WARNING, "Global request permit acquisition interrupted for auth form POST.", e); return Optional.empty(); }
        finally { GLOBAL_REQUEST_PERMIT.release(); }
    }

    /**
     * Executes a generic HTTP GET request and returns the response body as a raw JSON string.
     * Applies global rate limiting.
     *
     * @param urlString The target URL.
     * @param customHeaders Optional custom headers for the request. User-Agent will be added if not present.
     * @return An Optional containing the JSON string response body on success, or empty on failure.
     */
    public static Optional<String> getRawJson(String urlString, @Nullable Map<String, String> customHeaders) {
        try {
            GLOBAL_REQUEST_PERMIT.acquire();
            try {
                applyGlobalRateLimitDelay();

                HttpRequest.Builder reqBuilder = HttpRequest.newBuilder().uri(URI.create(urlString)).GET();
                addHeaders(reqBuilder, customHeaders); 

                HttpRequest request = reqBuilder.build();
                logger.fine("Sending GET (raw JSON) to: " + urlString);
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

                if (isSuccess(response.statusCode())) {
                    return Optional.of(response.body());
                } else {
                    logError(response, "GET (raw JSON)", urlString);
                    return Optional.empty();
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); logger.log(Level.WARNING, "Raw JSON GET request interrupted: " + urlString, e); return Optional.empty(); }
            catch (IOException e) { logger.log(Level.SEVERE, "IOException during raw JSON GET to " + urlString, e); return Optional.empty(); }
            catch (Exception e) { logger.log(Level.SEVERE, "Unexpected exception during raw JSON GET to " + urlString, e); return Optional.empty(); }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); logger.log(Level.WARNING, "Global request permit acquisition interrupted for raw JSON GET.", e); return Optional.empty(); }
        finally { GLOBAL_REQUEST_PERMIT.release(); }
    }

    /**
     * Executes a generic HTTP GET request and deserializes the JSON response to the specified generic type using TypeReference.
     * Applies global rate limiting.
     *
     * @param urlString The target URL.
     * @param customHeaders Optional custom headers for the request.
     * @param typeReference A {@link TypeReference} describing the expected generic response type.
     * @param <T> The generic type of the response.
     * @return An Optional containing the deserialized response object on success, or empty on failure.
     */
    public static <T> Optional<T> get(String urlString, @Nullable Map<String, String> customHeaders, TypeReference<T> typeReference) {
        Optional<String> jsonResponse = getRawJson(urlString, customHeaders); 
        return jsonResponse.flatMap(json -> {
            try {
                return Optional.of(objectMapper.readValue(json, typeReference));
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Failed to deserialize JSON GET response from " + urlString + " to TypeReference " + typeReference.getType(), e);
                return Optional.empty();
            }
        });
    }

    /**
     * Executes a generic HTTP GET request and returns the response body as a byte array.
     * Applies global rate limiting. Useful for downloading files like images.
     *
     * @param urlString The target URL.
     * @param customHeaders Optional custom headers for the request. User-Agent will be added if not present.
     * @return An Optional containing the byte array of the response body on success, or empty on failure.
     */
    public static Optional<byte[]> getRawBytes(String urlString, @Nullable Map<String, String> customHeaders) {
        try {
            GLOBAL_REQUEST_PERMIT.acquire();
            try {
                applyGlobalRateLimitDelay();

                HttpRequest.Builder reqBuilder = HttpRequest.newBuilder().uri(URI.create(urlString)).GET();
                addHeaders(reqBuilder, customHeaders); 

                HttpRequest request = reqBuilder.build();
                logger.fine("Sending GET (raw bytes) to: " + urlString);
                HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

                if (isSuccess(response.statusCode())) {
                    return Optional.ofNullable(response.body());
                } else {
                    logger.severe("Raw bytes GET request to " + urlString + " failed. Status: " + response.statusCode());
                    return Optional.empty();
                }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); logger.log(Level.WARNING, "Raw bytes GET request interrupted: " + urlString, e); return Optional.empty(); }
            catch (IOException e) { logger.log(Level.SEVERE, "IOException during raw bytes GET to " + urlString, e); return Optional.empty(); }
            catch (Exception e) { logger.log(Level.SEVERE, "Unexpected exception during raw bytes GET to " + urlString, e); return Optional.empty(); }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); logger.log(Level.WARNING, "Global request permit acquisition interrupted for raw bytes GET.", e); return Optional.empty(); }
        finally { GLOBAL_REQUEST_PERMIT.release(); }
    }

    

    /**
     * Adds custom headers to the HttpRequest.Builder.
     * Ensures a default User-Agent is set if not already provided in customHeaders.
     * @param builder The HttpRequest.Builder to add headers to.
     * @param headers A map of custom headers. Can be null.
     */
    private static void addHeaders(HttpRequest.Builder builder, @Nullable Map<String, String> headers) {
        boolean userAgentSet = false;
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                builder.header(entry.getKey(), entry.getValue());
                if ("User-Agent".equalsIgnoreCase(entry.getKey())) {
                    userAgentSet = true;
                }
            }
        }
        if (!userAgentSet) {
            builder.header("User-Agent", "OrchidClient/1.0"); 
        }
    }

    /**
     * Checks if an HTTP status code indicates success (2xx).
     * @param statusCode The HTTP status code.
     * @return true if successful, false otherwise.
     */
    private static boolean isSuccess(int statusCode) {
        return statusCode >= 200 && statusCode < 300;
    }

    /**
     * Logs an error message for a failed HTTP request.
     * @param response The HttpResponse object.
     * @param method The HTTP method used (e.g., "GET", "POST").
     * @param url The URL of the request.
     */
    private static void logError(HttpResponse<?> response, String method, String url) {
        Object body = response.body();
        String responseBodyStr = (body instanceof String) ? (String) body : "[response body not a string or null]";
        
        String loggedBody = responseBodyStr.length() > 500 ?
                responseBodyStr.substring(0, 500) + "... (truncated)" :
                responseBodyStr;
        logger.severe("HTTP " + method + " request to " + url + " failed. Status: " + response.statusCode() + ", Body: " + loggedBody);
    }
}
