package com.ggar.orchid.plugins.mangadex.dto;

import java.util.List;
import java.util.Optional;

public interface MangaDetails {
    String getSourceId();
    String getSourceUrl();
    String getTitle();
    List<String> getAlternativeTitles();
    List<String> getAuthors();
    List<String> getArtists();
    Optional<String> getDescription();
    Optional<String> getStatus();
    Optional<Integer> getYear();
    List<String> getGenres();
    Optional<String> getCoverImageUrl();
    List<ChapterDetails> getChapters();
    void setChapters(List<ChapterDetails> chapters);
}
