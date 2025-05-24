package com.ggar.orchid.plugins.mangadex;

import com.fasterxml.jackson.core.type.TypeReference;
import com.ggar.orchid.plugins.mangadex.dto.*;
import com.ggar.orchid.plugins.mangadex.http.MangaDexHttpTransport;
import jakarta.annotation.Nullable;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * MangaDexClient provides a high-level interface to interact with the MangaDex API.
 * It handles authentication, token management, rate limiting configuration, and API calls
 * to fetch manga and chapter information.
 * This class is stateful and designed to be instantiated once with the necessary configuration.
 */
public class MangaDexClient {
    private static final Logger logger = Logger.getLogger(MangaDexClient.class.getName());
    private static final String MANGADEX_API_BASE_URL = "https://api.mangadex.org";
    private static final String COVER_ART_BASE_URL = "https://uploads.mangadex.org";

    private final String clientId;
    private final String clientSecret;
    private final String grantType;
    @Nullable private final String username;
    @Nullable private final String password;


    private MangaDexAuthTokens currentAuthTokens;
    private final Object tokenLock = new Object();

    private static final Pattern MANGADEX_UUID_PATTERN = Pattern.compile(
            "mangadex\\.org/(?:title|manga)/([0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12})"
    );

    /**
     * Placeholder for unknown string values in domain DTOs.
     */
    public static final String UNKNOWN_VALUE = "N/A";
    /**
     * Placeholder for string values indicating an error state in domain DTOs.
     */
    public static final String ERROR_VALUE = "Error";
    private static final long TOKEN_REFRESH_MARGIN_SECONDS = 300;

    /**
     * Constructs a new MangaDexClient.
     *
     * @param clientId Your MangaDex API client ID.
     * @param clientSecret Your MangaDex API client secret.
     * @param grantType The OAuth2 grant type (e.g., "password", "client_credentials").
     * @param username Your MangaDex username (required for "password" grant type, can be null otherwise).
     * @param password Your MangaDex password (required for "password" grant type, can be null otherwise).
     * @param globalHttpIntervalMs The global request interval in milliseconds for rate limiting all HTTP calls.
     */
    public MangaDexClient(String clientId, String clientSecret, String grantType,
                          @Nullable String username, @Nullable String password,
                          long globalHttpIntervalMs) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.grantType = grantType;
        this.username = username;
        this.password = password;

        MangaDexHttpTransport.configureGlobalRequestInterval(globalHttpIntervalMs);
        logger.info("MangaDexClient initialized. Global HTTP request interval set to: " + globalHttpIntervalMs + "ms");
    }

    /**
     * Retrieves a valid access token.
     * If the current token is expired or about to expire, it attempts to refresh it.
     * If refreshment fails or no refresh token is available, it attempts a new login.
     * This method is synchronized to prevent race conditions during token operations.
     *
     * @return An Optional containing the access token string, or an empty Optional if unable to obtain a valid token.
     */
    private Optional<String> getValidAccessToken() {
        synchronized (tokenLock) {
            if (currentAuthTokens != null && !currentAuthTokens.isAccessTokenExpired(TOKEN_REFRESH_MARGIN_SECONDS)) {
                logger.fine("Using existing valid access token.");
                return Optional.of(currentAuthTokens.getAccessToken());
            }

            if (currentAuthTokens != null && currentAuthTokens.getRefreshToken() != null) {
                logger.info("Access token expired or nearing expiry. Attempting to refresh...");
                Optional<MangaDexAuthTokens> refreshedTokens = MangaDexAuth.refreshToken(
                        this.clientId, this.clientSecret, currentAuthTokens.getRefreshToken()
                );
                if (refreshedTokens.isPresent()) {
                    this.currentAuthTokens = refreshedTokens.get();
                    logger.info("Access token refreshed successfully.");
                    return Optional.of(this.currentAuthTokens.getAccessToken());
                } else {
                    logger.warning("Failed to refresh access token. Attempting a new login.");
                    this.currentAuthTokens = null;
                }
            }

            logger.info("Attempting new login to obtain access token...");
            Optional<MangaDexAuthTokens> newTokens = MangaDexAuth.login(
                    this.grantType, this.clientId, this.clientSecret, this.username, this.password
            );
            if (newTokens.isPresent()) {
                this.currentAuthTokens = newTokens.get();
                logger.info("Login successful. New access token obtained.");
                return Optional.of(this.currentAuthTokens.getAccessToken());
            } else {
                logger.severe("Failed to obtain access token after login and/or refresh attempts.");
                this.currentAuthTokens = null;
                return Optional.empty();
            }
        }
    }

    @FunctionalInterface
    private interface AuthenticatedRequest<T> {
        Optional<T> execute(Map<String, String> headers);
        default boolean requiresAuth() { return true; }
    }

    /**
     * Executes an HTTP request that may require authentication.
     * It ensures a valid access token is available, includes it in the headers,
     * and performs a basic retry mechanism if the initial request fails (suspected token invalidation).
     *
     * @param requestFunction A lambda expression representing the actual HTTP call to be made.
     * It receives authentication headers and should return an Optional of the response type.
     * @param isRetry         A flag to indicate if this is a retry attempt, to prevent infinite retry loops.
     * @param <T>             The expected type of the deserialized response.
     * @return An Optional containing the deserialized response, or an empty Optional on failure.
     */
    private <T> Optional<T> executeAuthenticatedRequest(AuthenticatedRequest<T> requestFunction, boolean isRetry) {
        Optional<String> accessTokenOpt = getValidAccessToken();

        if (accessTokenOpt.isEmpty() && requestFunction.requiresAuth()) {
            logger.severe("Could not obtain access token for an authenticated request.");
            return Optional.empty();
        }

        Map<String, String> headers = new HashMap<>();
        accessTokenOpt.ifPresent(token -> headers.put("Authorization", "Bearer " + token));

        Optional<T> response = requestFunction.execute(headers);

        if (response.isEmpty() && !isRetry && requestFunction.requiresAuth() && accessTokenOpt.isPresent()) {
            logger.warning("Authenticated request failed. Suspecting invalidated token. Forcing token refresh and retrying once...");
            synchronized (tokenLock) {
                this.currentAuthTokens = null;
            }
            return executeAuthenticatedRequest(requestFunction, true);
        }
        return response;
    }


    /**
     * Extracts a MangaDex UUID from a given identifier, which can be a direct UUID or a MangaDex URL.
     *
     * @param identifier The manga identifier (UUID or URL).
     * @return An Optional containing the extracted UUID string, or empty if extraction fails.
     */
    private Optional<String> extractMangaId(String identifier) {
        if (!hasText(identifier)) {
            logger.warning("Manga identifier is null or empty.");
            return Optional.empty();
        }
        if (identifier.matches("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}")) {
            return Optional.of(identifier);
        }
        Matcher matcher = MANGADEX_UUID_PATTERN.matcher(identifier);
        if (matcher.find()) {
            return Optional.of(matcher.group(1));
        }
        logger.warning("Failed to extract MangaDex UUID from identifier: " + identifier);
        return Optional.empty();
    }

    /**
     * Fetches detailed information for a specific manga.
     *
     * @param identifier The MangaDex manga UUID or a full MangaDex URL to the manga.
     * @return An Optional containing {@link MangaDetails}, or an empty Optional if not found or an error occurs.
     */
    public Optional<MangaDetails> fetchMangaDetails(String identifier) {
        Optional<String> mangaIdOpt = extractMangaId(identifier);
        if (mangaIdOpt.isEmpty()) {
            return Optional.empty();
        }
        String mangaId = mangaIdOpt.get();

        AuthenticatedRequest<MangaDexResponse<MangaData>> requestLogic = headers -> {
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("includes[]", "author");
            queryParams.put("includes[]", "artist");
            queryParams.put("includes[]", "cover_art");
            String queryString = queryParams.entrySet().stream()
                    .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));
            String url = String.format("%s/manga/%s?%s", MANGADEX_API_BASE_URL, mangaId, queryString);

            return MangaDexHttpTransport.get(url, headers, new TypeReference<MangaDexResponse<MangaData>>() {});
        };

        Optional<MangaDexResponse<MangaData>> responseOpt = executeAuthenticatedRequest(requestLogic, false);

        if (responseOpt.isEmpty() || !"ok".equalsIgnoreCase(responseOpt.get().getResult())) {
            logger.warning("Invalid response or error fetching manga details for ID " + mangaId +
                    ". Result: " + responseOpt.map(MangaDexResponse::getResult).orElse("N/A"));
            return Optional.empty();
        }
        MangaDexResponse<MangaData> mangaDexResponse = responseOpt.get();
        if (mangaDexResponse.getData() == null) {
            logger.warning("No data found in manga details response for ID " + mangaId);
            return Optional.empty();
        }
        return Optional.of(mapToDomainMangaDetails(mangaDexResponse.getData(), identifier));
    }

    /**
     * Fetches a list of the most recent chapters for a given manga.
     *
     * @param identifier The MangaDex manga UUID or a full MangaDex URL to the manga.
     * @param translatedLanguages Optional list of language codes (e.g., "en", "es") to filter chapters by.
     * If null or empty, chapters in all languages are fetched.
     * @param count Optional limit for the number of most recent chapters to fetch.
     * If null, all available chapters (matching language filters) are fetched.
     * @return A list of {@link ChapterDetails}.
     */
    public List<ChapterDetails> fetchChapterListByRecency(
            String identifier,
            @Nullable List<String> translatedLanguages,
            @Nullable Integer count
    ) {
        Optional<String> mangaIdOpt = extractMangaId(identifier);
        if (mangaIdOpt.isEmpty()) {
            return Collections.emptyList();
        }
        String mangaId = mangaIdOpt.get();

        List<ChapterDetails> collectedChapters = new ArrayList<>();
        int apiRequestLimit = 100;
        int offset = 0;
        boolean moreToFetch = true;

        logger.info("Fetching chapter list by recency for manga ID: " + mangaId +
                (translatedLanguages != null ? ", Languages: " + translatedLanguages : "") +
                (count != null ? ", Count: " + count : ", Count: All"));

        while (moreToFetch) {
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("limit", String.valueOf(apiRequestLimit));
            queryParams.put("offset", String.valueOf(offset));
            queryParams.put("order[publishAt]", "desc");

            if (translatedLanguages != null && !translatedLanguages.isEmpty()) {
                translatedLanguages.forEach(lang -> queryParams.put("translatedLanguage[]", lang));
            }

            String queryString = queryParams.entrySet().stream()
                    .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));
            String url = String.format("%s/manga/%s/feed?%s", MANGADEX_API_BASE_URL, mangaId, queryString);

            AuthenticatedRequest<MangaDexResponse<List<ChapterData>>> requestLogic = headers ->
                    MangaDexHttpTransport.get(url, headers, new TypeReference<MangaDexResponse<List<ChapterData>>>() {});

            Optional<MangaDexResponse<List<ChapterData>>> responseOpt = executeAuthenticatedRequest(requestLogic, false);

            if (responseOpt.isPresent() && "ok".equalsIgnoreCase(responseOpt.get().getResult())) {
                MangaDexResponse<List<ChapterData>> feedResponse = responseOpt.get();
                List<ChapterData> batch = feedResponse.getData();

                if (batch != null && !batch.isEmpty()) {
                    for (ChapterData chapterData : batch) {
                        if (chapterData == null) continue;
                        collectedChapters.add(mapToDomainChapterDetails(chapterData));
                        if (count != null && collectedChapters.size() >= count) {
                            moreToFetch = false;
                            break;
                        }
                    }
                    offset += batch.size();
                    if ((feedResponse.getTotal() != null && offset >= feedResponse.getTotal()) || batch.size() < apiRequestLimit) {
                        moreToFetch = false;
                    }
                } else {
                    moreToFetch = false;
                }
            } else {
                logger.warning("Failed to fetch chapters (by recency) for manga ID " + mangaId + " at offset " + offset +
                        ". Result: " + responseOpt.map(MangaDexResponse::getResult).orElse("Empty Response"));
                moreToFetch = false;
            }
            if (!moreToFetch) break;
        }

        if (count != null && collectedChapters.size() > count) {
            return collectedChapters.subList(0, count);
        }
        logger.info("Fetched " + collectedChapters.size() + " chapters for manga ID " + mangaId + " (by recency).");
        return collectedChapters;
    }

    /**
     * Fetches a list of chapters for a given manga, optionally filtered by a start and/or end chapter UUID.
     * Chapters are returned in their natural reading order (volume ascending, chapter number ascending).
     * The UUID range filtering is performed client-side after fetching all relevant chapters.
     *
     * @param identifier The MangaDex manga UUID or a full MangaDex URL to the manga.
     * @param translatedLanguages Optional list of language codes to filter chapters by.
     * @param startChapterUuid Optional UUID of the chapter from which to start the list (inclusive).
     * @param endChapterUuid Optional UUID of the chapter at which to end the list (inclusive).
     * @return A list of {@link ChapterDetails}.
     */
    public List<ChapterDetails> fetchChapterListByUuidRange(
            String identifier,
            @Nullable List<String> translatedLanguages,
            @Nullable String startChapterUuid,
            @Nullable String endChapterUuid
    ) {
        Optional<String> mangaIdOpt = extractMangaId(identifier);
        if (mangaIdOpt.isEmpty()) {
            return Collections.emptyList();
        }
        String mangaId = mangaIdOpt.get();

        List<ChapterDetails> allChaptersInOrder = new ArrayList<>();
        int apiRequestLimit = 100;
        int offset = 0;
        boolean moreToFetch = true;

        logger.info("Fetching all chapters for UUID range filtering for manga ID: " + mangaId +
                (translatedLanguages != null ? ", Languages: " + translatedLanguages : ""));

        while (moreToFetch) {
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("limit", String.valueOf(apiRequestLimit));
            queryParams.put("offset", String.valueOf(offset));
            queryParams.put("order[volume]", "asc");
            queryParams.put("order[chapter]", "asc");

            if (translatedLanguages != null && !translatedLanguages.isEmpty()) {
                translatedLanguages.forEach(lang -> queryParams.put("translatedLanguage[]", lang));
            }

            String queryString = queryParams.entrySet().stream()
                    .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                    .collect(Collectors.joining("&"));
            String url = String.format("%s/manga/%s/feed?%s", MANGADEX_API_BASE_URL, mangaId, queryString);

            AuthenticatedRequest<MangaDexResponse<List<ChapterData>>> requestLogic = headers ->
                    MangaDexHttpTransport.get(url, headers, new TypeReference<MangaDexResponse<List<ChapterData>>>() {});

            Optional<MangaDexResponse<List<ChapterData>>> responseOpt = executeAuthenticatedRequest(requestLogic, false);

            if (responseOpt.isPresent() && "ok".equalsIgnoreCase(responseOpt.get().getResult())) {
                MangaDexResponse<List<ChapterData>> feedResponse = responseOpt.get();
                List<ChapterData> batch = feedResponse.getData();

                if (batch != null && !batch.isEmpty()) {
                    batch.stream()
                            .filter(Objects::nonNull)
                            .map(this::mapToDomainChapterDetails)
                            .forEach(allChaptersInOrder::add);
                    offset += batch.size();
                    if ((feedResponse.getTotal() != null && offset >= feedResponse.getTotal()) || batch.size() < apiRequestLimit) {
                        moreToFetch = false;
                    }
                } else {
                    moreToFetch = false;
                }
            } else {
                logger.warning("Failed to fetch chapters (for UUID range) for manga ID " + mangaId + " at offset " + offset +
                        ". Result: " + responseOpt.map(MangaDexResponse::getResult).orElse("Empty Response"));
                moreToFetch = false;
            }
        }

        if (startChapterUuid == null && endChapterUuid == null) {
            logger.info("No UUID range specified, returning all " + allChaptersInOrder.size() + " fetched chapters for manga ID " + mangaId);
            return allChaptersInOrder;
        }

        List<ChapterDetails> rangedChapters = new ArrayList<>();
        boolean inRange = (startChapterUuid == null);

        for (ChapterDetails chapter : allChaptersInOrder) {
            if (!inRange && startChapterUuid != null && startChapterUuid.equals(chapter.getSourceId())) {
                inRange = true;
            }

            if (inRange) {
                rangedChapters.add(chapter);
            }

            if (inRange && endChapterUuid != null && endChapterUuid.equals(chapter.getSourceId())) {
                break;
            }
        }

        if (startChapterUuid != null && rangedChapters.stream().noneMatch(ch -> ch.getSourceId().equals(startChapterUuid))) {
            if (!(startChapterUuid == null && endChapterUuid != null && !rangedChapters.isEmpty())) {
                logger.warning("Start chapter UUID '" + startChapterUuid + "' not found in the fetched chapter list. Returning empty list for range.");
                return Collections.emptyList();
            }
        }


        logger.info("Filtered " + rangedChapters.size() + " chapters by UUID range for manga ID " + mangaId);
        return rangedChapters;
    }

    /**
     * Fetches the full image URLs for a given chapter's pages.
     *
     * @param chapterId The UUID of the chapter.
     * @param useDataSaver If true, requests data-saver image variants. Otherwise, requests normal quality images.
     * @param forcePort443 If true, requests MangaDex@Home servers operating on port 443.
     * @return An Optional containing a list of image URLs, or empty if an error occurs.
     */
    public Optional<List<String>> fetchChapterPageImageUrls(String chapterId, boolean useDataSaver, boolean forcePort443) {
        if (!hasText(chapterId)) {
            logger.warning("Chapter ID is null or empty for fetching page URLs.");
            return Optional.empty();
        }

        String atHomeServerUrl = String.format("%s/at-home/server/%s?forcePort443=%b",
                MANGADEX_API_BASE_URL, chapterId, forcePort443);

        Optional<String> rawJsonResponse = MangaDexHttpTransport.getRawJson(atHomeServerUrl, Collections.emptyMap());

        if (rawJsonResponse.isEmpty()) {
            logger.warning("No response received from MangaDex@Home server for chapter: " + chapterId);
            return Optional.empty();
        }

        Optional<AtHomeServerResponse> atHomeResponseOpt;
        try {
            atHomeResponseOpt = Optional.of(MangaDexHttpTransport.objectMapper.readValue(rawJsonResponse.get(), AtHomeServerResponse.class));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error deserializing MangaDex@Home server response for chapter: " + chapterId, e);
            return Optional.empty();
        }

        if (atHomeResponseOpt.isEmpty() || !"ok".equalsIgnoreCase(atHomeResponseOpt.get().getResult())) {
            logger.warning("Non-'ok' or empty response from MangaDex@Home server for chapter: " + chapterId +
                    ". Result: " + atHomeResponseOpt.map(AtHomeServerResponse::getResult).orElse("N/A"));
            return Optional.empty();
        }

        AtHomeServerResponse atHomeData = atHomeResponseOpt.get();
        if (!hasText(atHomeData.getBaseUrl()) || atHomeData.getChapter() == null || !hasText(atHomeData.getChapter().getHash())) {
            logger.warning("Incomplete response from MangaDex@Home server (missing baseUrl or chapter hash) for chapter: " + chapterId);
            return Optional.empty();
        }

        List<String> pageFilenames = useDataSaver ? atHomeData.getChapter().getDataSaver() : atHomeData.getChapter().getData();
        if (pageFilenames == null || pageFilenames.isEmpty()) {
            logger.warning("No page filenames found in MangaDex@Home response for chapter: " + chapterId +
                    (useDataSaver ? " (dataSaver mode)" : " (normal mode)"));
            return Optional.empty();
        }

        String qualityModePath = useDataSaver ? "data-saver" : "data";
        List<String> pageUrls = pageFilenames.stream()
                .map(filename -> String.format("%s/%s/%s/%s",
                        atHomeData.getBaseUrl(),
                        qualityModePath,
                        atHomeData.getChapter().getHash(),
                        filename))
                .collect(Collectors.toList());

        logger.info("Successfully fetched " + pageUrls.size() + " page URLs for chapter " + chapterId);
        return Optional.of(pageUrls);
    }

    /**
     * Downloads the raw image bytes for a given manga chapter page URL.
     * These URLs are typically obtained from {@link #fetchChapterPageImageUrls(String, boolean, boolean)}.
     * <p>
     * Note: Image URLs from MangaDex@Home servers generally do not require Authorization headers.
     * This method uses the global rate limiter configured for {@link MangaDexHttpTransport}.
     *
     * @param imageUrl The full URL of the image to download.
     * @return An Optional containing the byte array of the image, or an empty Optional if the download fails.
     */
    public Optional<byte[]> downloadChapterPageImage(String imageUrl) {
        if (!hasText(imageUrl)) {
            logger.warning("Image URL is null or empty.");
            return Optional.empty();
        }

        logger.info("Attempting to download image from: " + imageUrl);
        Optional<byte[]> imageBytes = MangaDexHttpTransport.getRawBytes(imageUrl, null);

        if (imageBytes.isPresent()) {
            logger.info("Successfully downloaded image bytes from: " + imageUrl + ", size: " + imageBytes.get().length + " bytes.");
        } else {
            logger.warning("Failed to download image bytes from: " + imageUrl);
        }
        return imageBytes;
    }

    private MangaDetails mapToDomainMangaDetails(MangaData mangaData, String originalIdentifier) {
        MangaAttributes attributes = mangaData.getAttributes();
        if (attributes == null) {
            logger.severe("Manga attributes are null for manga ID: " + (mangaData != null ? mangaData.getId() : UNKNOWN_VALUE));
            return new MangaDetailsAdapter(mangaData != null ? mangaData.getId() : null, originalIdentifier);
        }

        List<String> authors = new ArrayList<>();
        List<String> artists = new ArrayList<>();
        AtomicReference<Optional<String>> coverFilenameOpt = new AtomicReference<>(Optional.empty());

        if (mangaData.getRelationships() != null) {
            for (Relationship rel : mangaData.getRelationships()) {
                if (rel == null) continue;
                String type = rel.getType();

                if ("author".equalsIgnoreCase(type)) {
                    rel.getTypedAttributes(AuthorAttributes.class, MangaDexHttpTransport.objectMapper)
                            .ifPresent(authorAttrs -> {
                                if (hasText(authorAttrs.getName())) authors.add(authorAttrs.getName());
                            });
                } else if ("artist".equalsIgnoreCase(type)) {
                    rel.getTypedAttributes(AuthorAttributes.class, MangaDexHttpTransport.objectMapper)
                            .ifPresent(artistAttrs -> {
                                if (hasText(artistAttrs.getName())) artists.add(artistAttrs.getName());
                            });
                } else if ("cover_art".equalsIgnoreCase(type) && coverFilenameOpt.get().isEmpty()) {
                    rel.getTypedAttributes(CoverAttributes.class, MangaDexHttpTransport.objectMapper)
                            .ifPresent(coverAttrs -> {
                                if (hasText(coverAttrs.getFileName())) coverFilenameOpt.set(Optional.of(coverAttrs.getFileName()));
                            });
                }
            }
        }

        List<String> genres = (attributes.getTags() != null) ?
                attributes.getTags().stream()
                        .filter(tag -> tag != null && tag.getAttributes() != null && "genre".equalsIgnoreCase(tag.getAttributes().getGroup()))
                        .map(tag -> tag.getAttributes().getEnglishName())
                        .filter(MangaDexClient::hasText)
                        .distinct()
                        .collect(Collectors.toList()) :
                Collections.emptyList();

        List<String> collectedAltTitles = new ArrayList<>();
        if (attributes.getAltTitles() != null) {
            attributes.getAltTitles().stream()
                    .filter(Objects::nonNull)
                    .flatMap(map -> map.values().stream())
                    .filter(MangaDexClient::hasText)
                    .forEach(title -> {
                        if (collectedAltTitles.stream().noneMatch(t -> t.equalsIgnoreCase(title))) {
                            collectedAltTitles.add(title);
                        }
                    });
        }
        if (attributes.getTitle() != null) {
            attributes.getTitle().values().stream()
                    .filter(MangaDexClient::hasText)
                    .forEach(title -> {
                        if (collectedAltTitles.stream().noneMatch(t -> t.equalsIgnoreCase(title))) {
                            collectedAltTitles.add(title);
                        }
                    });
        }
        String mainEnglishTitle = attributes.getEnglishTitle();
        if (hasText(mainEnglishTitle)) {
            collectedAltTitles.removeIf(altTitle -> altTitle.equalsIgnoreCase(mainEnglishTitle));
        }

        final String mangaIdForCover = mangaData.getId();
        Optional<String> finalCoverImageUrl = coverFilenameOpt.get().flatMap(
                filename -> (hasText(filename) && hasText(mangaIdForCover)) ?
                        Optional.of(String.format("%s/covers/%s/%s", COVER_ART_BASE_URL, mangaIdForCover, filename)) :
                        Optional.empty()
        );

        return new MangaDetailsAdapter(mangaData, originalIdentifier, attributes, authors, artists, genres, collectedAltTitles, finalCoverImageUrl);
    }

    private ChapterDetails mapToDomainChapterDetails(ChapterData chapterData) {
        ChapterAttributes attributes = chapterData.getAttributes();
        if (attributes == null) {
            logger.warning("Chapter attributes are null for chapter ID: " + (chapterData != null ? chapterData.getId() : UNKNOWN_VALUE));
            return new ChapterDetailsAdapter(chapterData != null ? chapterData.getId() : null);
        }
        return new ChapterDetailsAdapter(chapterData, attributes);
    }

    /**
     * Checks if a String has actual text content (not null, not empty, not just whitespace).
     * @param str The String to check.
     * @return true if the String has text, false otherwise.
     */
    public static boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}