package com.ggar.orchid.plugins.mangadex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ErrorDetail {
    @JsonProperty("id") private String id; @JsonProperty("status") private int status;
    @JsonProperty("title") private String title; @JsonProperty("detail") private String detail;
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public int getStatus() { return status; } public void setStatus(int status) { this.status = status; }
    public String getTitle() { return title; } public void setTitle(String title) { this.title = title; }
    public String getDetail() { return detail; } public void setDetail(String detail) { this.detail = detail; }
}
