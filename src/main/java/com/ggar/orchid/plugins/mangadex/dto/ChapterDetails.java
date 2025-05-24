package com.ggar.orchid.plugins.mangadex.dto;

import java.util.Optional;

public interface ChapterDetails {
    String getSourceId();
    String getChapterUrl();
    String getChapterNumber();
    Optional<String> getTitle();
    Optional<String> getLanguage();
    Optional<Integer> getPages();
    Optional<String> getPublishDate();
}
