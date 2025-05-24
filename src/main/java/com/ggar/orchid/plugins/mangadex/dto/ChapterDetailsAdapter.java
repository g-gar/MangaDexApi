package com.ggar.orchid.plugins.mangadex.dto;

import com.ggar.orchid.plugins.mangadex.MangaDexClient;

import java.util.Optional;

public class ChapterDetailsAdapter implements ChapterDetails {
    private final String sourceId, chapterUrl, chapterNumber;
    private final Optional<String> title, language, publishDate;
    private final Optional<Integer> pages;

    public ChapterDetailsAdapter(ChapterData cd, ChapterAttributes ca) {
        this.sourceId = cd.getId(); this.chapterUrl = MangaDexClient.hasText(cd.getId()) ? String.format("https://mangadex.org/chapter/%s", cd.getId()) : "";
        this.chapterNumber = MangaDexClient.hasText(ca.getChapter()) ? ca.getChapter() : MangaDexClient.UNKNOWN_VALUE;
        this.title = Optional.ofNullable(ca.getTitle()).filter(MangaDexClient::hasText);
        this.language = Optional.ofNullable(ca.getTranslatedLanguage()).filter(MangaDexClient::hasText);
        this.pages = (ca.getPages() >= 0) ? Optional.of(ca.getPages()) : Optional.empty();
        this.publishDate = Optional.ofNullable(ca.getPublishAt()).filter(MangaDexClient::hasText);
    }
    public ChapterDetailsAdapter(String sourceId) { // Constructor para error
        this.sourceId = sourceId != null ? sourceId : MangaDexClient.ERROR_VALUE; this.chapterUrl = ""; this.chapterNumber = MangaDexClient.UNKNOWN_VALUE;
        this.title = Optional.empty(); this.language = Optional.empty(); this.pages = Optional.empty(); this.publishDate = Optional.empty();
    }
    @Override public String getSourceId() { return sourceId; } @Override public String getChapterUrl() { return chapterUrl; }
    @Override public String getChapterNumber() { return chapterNumber; } @Override public Optional<String> getTitle() { return title; }
    @Override public Optional<String> getLanguage() { return language; } @Override public Optional<Integer> getPages() { return pages; }
    @Override public Optional<String> getPublishDate() { return publishDate; }
}
