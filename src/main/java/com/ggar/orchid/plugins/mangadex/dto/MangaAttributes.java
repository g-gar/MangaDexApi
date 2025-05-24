package com.ggar.orchid.plugins.mangadex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MangaAttributes {
    @JsonProperty("title") private Map<String, String> title;
    @JsonProperty("altTitles") private List<Map<String, String>> altTitles;
    @JsonProperty("description") private Map<String, String> description;
    @JsonProperty("links") private Map<String, String> links;
    @JsonProperty("originalLanguage") private String originalLanguage;
    @JsonProperty("lastVolume") private String lastVolume;
    @JsonProperty("lastChapter") private String lastChapter;
    @JsonProperty("publicationDemographic") private String publicationDemographic;
    @JsonProperty("status") private String status;
    @JsonProperty("year") private Integer year;
    @JsonProperty("contentRating") private String contentRating;
    @JsonProperty("tags") private List<Tag> tags;
    @JsonProperty("state") private String state;
    @JsonProperty("chapterNumbersResetOnNewVolume") private boolean chapterNumbersResetOnNewVolume;
    @JsonProperty("createdAt") private String createdAt;
    @JsonProperty("updatedAt") private String updatedAt;
    @JsonProperty("version") private int version;
    @JsonProperty("availableTranslatedLanguages") private List<String> availableTranslatedLanguages;
    @JsonProperty("latestUploadedChapter") private String latestUploadedChapter;

    public String getEnglishTitle() { return (title != null) ? title.get("en") : null; }
    public String getEnglishDescription() { return (description != null) ? description.get("en") : null; }

    public Map<String, String> getTitle() { return title; } public void setTitle(Map<String, String> title) { this.title = title; }
    public List<Map<String, String>> getAltTitles() { return altTitles; } public void setAltTitles(List<Map<String, String>> altTitles) { this.altTitles = altTitles; }
    public Map<String, String> getDescription() { return description; } public void setDescription(Map<String, String> description) { this.description = description; }
    public Map<String, String> getLinks() { return links; } public void setLinks(Map<String, String> links) { this.links = links; }
    public String getOriginalLanguage() { return originalLanguage; } public void setOriginalLanguage(String originalLanguage) { this.originalLanguage = originalLanguage; }
    public String getLastVolume() { return lastVolume; } public void setLastVolume(String lastVolume) { this.lastVolume = lastVolume; }
    public String getLastChapter() { return lastChapter; } public void setLastChapter(String lastChapter) { this.lastChapter = lastChapter; }
    public String getPublicationDemographic() { return publicationDemographic; } public void setPublicationDemographic(String publicationDemographic) { this.publicationDemographic = publicationDemographic; }
    public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
    public Integer getYear() { return year; } public void setYear(Integer year) { this.year = year; }
    public String getContentRating() { return contentRating; } public void setContentRating(String contentRating) { this.contentRating = contentRating; }
    public List<Tag> getTags() { return tags; } public void setTags(List<Tag> tags) { this.tags = tags; }
    public String getState() { return state; } public void setState(String state) { this.state = state; }
    public boolean isChapterNumbersResetOnNewVolume() { return chapterNumbersResetOnNewVolume; } public void setChapterNumbersResetOnNewVolume(boolean chapterNumbersResetOnNewVolume) { this.chapterNumbersResetOnNewVolume = chapterNumbersResetOnNewVolume; }
    public String getCreatedAt() { return createdAt; } public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; } public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
    public int getVersion() { return version; } public void setVersion(int version) { this.version = version; }
    public List<String> getAvailableTranslatedLanguages() { return availableTranslatedLanguages; } public void setAvailableTranslatedLanguages(List<String> availableTranslatedLanguages) { this.availableTranslatedLanguages = availableTranslatedLanguages; }
    public String getLatestUploadedChapter() { return latestUploadedChapter; } public void setLatestUploadedChapter(String latestUploadedChapter) { this.latestUploadedChapter = latestUploadedChapter; }
}
