package com.ggar.orchid.plugins.mangadex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthorAttributes { 
    @JsonProperty("name") private String name;
    @JsonProperty("imageUrl") private String imageUrl;
    @JsonProperty("biography") private Map<String, String> biography; 
    @JsonProperty("twitter") private String twitter;
    @JsonProperty("pixiv") private String pixiv;
    @JsonProperty("youtube") private String youtube;
    
    @JsonProperty("website") private String website;
    @JsonProperty("version") private Integer version;
    @JsonProperty("createdAt") private String createdAt;
    @JsonProperty("updatedAt") private String updatedAt;
    public String getName() { return name; } public void setName(String name) { this.name = name; }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Map<String, String> getBiography() {
        return biography;
    }

    public void setBiography(Map<String, String> biography) {
        this.biography = biography;
    }

    public String getTwitter() {
        return twitter;
    }

    public void setTwitter(String twitter) {
        this.twitter = twitter;
    }

    public String getPixiv() {
        return pixiv;
    }

    public void setPixiv(String pixiv) {
        this.pixiv = pixiv;
    }

    public String getYoutube() {
        return youtube;
    }

    public void setYoutube(String youtube) {
        this.youtube = youtube;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }
}
