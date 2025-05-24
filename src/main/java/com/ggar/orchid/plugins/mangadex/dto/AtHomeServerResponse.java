package com.ggar.orchid.plugins.mangadex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AtHomeServerResponse {
    @JsonProperty("result")
    private String result;
    @JsonProperty("baseUrl")
    private String baseUrl;
    @JsonProperty("chapter")
    private AtHomeChapterData chapter;

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getBaseUrl() { return baseUrl; }
    public void setBaseUrl(String baseUrl) { this.baseUrl = baseUrl; }
    public AtHomeChapterData getChapter() { return chapter; }
    public void setChapter(AtHomeChapterData chapter) { this.chapter = chapter; }
}
