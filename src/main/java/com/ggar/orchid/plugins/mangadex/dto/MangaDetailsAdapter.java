package com.ggar.orchid.plugins.mangadex.dto;

import com.ggar.orchid.plugins.mangadex.MangaDexClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class MangaDetailsAdapter implements MangaDetails {
    private final String sourceId, sourceUrl, title;
    private final List<String> alternativeTitles, authors, artists, genres;
    private final Optional<String> description, status, coverImageUrl;
    private final Optional<Integer> year;
    private List<ChapterDetails> chapters = new ArrayList<>();

    public MangaDetailsAdapter(MangaData md, String originalId, MangaAttributes attrs, List<String> authors, List<String> artists, List<String> genres, List<String> altTitles, Optional<String> coverUrl) {
        this.sourceId = md.getId(); this.sourceUrl = MangaDexClient.hasText(md.getId()) ? String.format("https://mangadex.org/title/%s", md.getId()) : originalId;
        this.title = attrs.getEnglishTitle() != null ? attrs.getEnglishTitle() : MangaDexClient.UNKNOWN_VALUE;
        this.alternativeTitles = altTitles; this.authors = authors; this.artists = artists; this.genres = genres;
        this.description = Optional.ofNullable(attrs.getEnglishDescription()).filter(MangaDexClient::hasText);
        this.status = Optional.ofNullable(attrs.getStatus()).filter(MangaDexClient::hasText);
        this.year = Optional.ofNullable(attrs.getYear()); this.coverImageUrl = coverUrl;
    }
    public MangaDetailsAdapter(String sourceId, String originalId) { // Constructor para error
        this.sourceId = sourceId != null ? sourceId : MangaDexClient.UNKNOWN_VALUE; this.sourceUrl = originalId;
        this.title = MangaDexClient.ERROR_VALUE + ": Atributos Nulos"; this.alternativeTitles = Collections.emptyList(); this.authors = Collections.emptyList();
        this.artists = Collections.emptyList(); this.description = Optional.empty(); this.status = Optional.empty(); this.year = Optional.empty();
        this.genres = Collections.emptyList(); this.coverImageUrl = Optional.empty();
    }
    @Override public String getSourceId() { return sourceId; } @Override public String getSourceUrl() { return sourceUrl; }
    @Override public String getTitle() { return title; } @Override public List<String> getAlternativeTitles() { return alternativeTitles; }
    @Override public List<String> getAuthors() { return authors; } @Override public List<String> getArtists() { return artists; }
    @Override public Optional<String> getDescription() { return description; } @Override public Optional<String> getStatus() { return status; }
    @Override public Optional<Integer> getYear() { return year; } @Override public List<String> getGenres() { return genres; }
    @Override public Optional<String> getCoverImageUrl() { return coverImageUrl; }
    @Override public List<ChapterDetails> getChapters() { return this.chapters; }
    @Override public void setChapters(List<ChapterDetails> chapters) { this.chapters = (chapters != null) ? new ArrayList<>(chapters) : new ArrayList<>(); }
}