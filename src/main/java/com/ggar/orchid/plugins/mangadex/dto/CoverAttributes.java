package com.ggar.orchid.plugins.mangadex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CoverAttributes { 
    @JsonProperty("description") private String description;
    @JsonProperty("volume") private String volume;
    @JsonProperty("fileName") private String fileName;
    @JsonProperty("locale") private String locale;
    @JsonProperty("version") private Integer version;
    @JsonProperty("createdAt") private String createdAt;
    @JsonProperty("updatedAt") private String updatedAt;
    public String getDescription() { return description; } public void setDescription(String description) { this.description = description; }
    public String getVolume() { return volume; } public void setVolume(String volume) { this.volume = volume; }
    public String getFileName() { return fileName; } public void setFileName(String fileName) { this.fileName = fileName; }
    public String getLocale() { return locale; } public void setLocale(String locale) { this.locale = locale; }
    public Integer getVersion() { return version; } public void setVersion(Integer version) { this.version = version; }
    public String getCreatedAt() { return createdAt; } public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getUpdatedAt() { return updatedAt; } public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }
}
