package com.ggar.orchid.plugins.mangadex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChapterAttributes { 
    @JsonProperty("title") private String title;
    @JsonProperty("volume") private String volume;
    @JsonProperty("chapter") private String chapter;
    @JsonProperty("pages") private Integer pages; 
    @JsonProperty("translatedLanguage") private String translatedLanguage;
    @JsonProperty("uploader") private String uploader; 
    @JsonProperty("externalUrl") private String externalUrl;
    @JsonProperty("version") private Integer version; 
    @JsonProperty("createdAt") private String createdAt;
    @JsonProperty("updatedAt") private String updatedAt;
    @JsonProperty("publishAt") private String publishAt;
    @JsonProperty("readableAt") private String readableAt;

    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getVolume() { return volume; } public void setVolume(String volume) { this.volume = volume; }
    public String getChapter() { return chapter; } public void setChapter(String chapter) { this.chapter = chapter; }
    public Integer getPages() { return pages; } public void setPages(Integer pages) { this.pages = pages; }
    public String getTranslatedLanguage() { return translatedLanguage; } public void setTranslatedLanguage(String translatedLanguage) { this.translatedLanguage = translatedLanguage; }
    public String getUploader() { return uploader; } public void setUploader(String uploader) { this.uploader = uploader; }
    public String getExternalUrl() { return externalUrl; } public void setExternalUrl(String externalUrl) { this.externalUrl = externalUrl; }
    public Integer getVersion() { return version; } public void setVersion(Integer version) { this.version = version; }
    public String getCreatedAt() { return createdAt; } public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; } public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public String getPublishAt() { return publishAt; } public void setPublishAt(String publishAt) { this.publishAt = publishAt; }
    public String getReadableAt() { return readableAt; } public void setReadableAt(String readableAt) { this.readableAt = readableAt; }
}
