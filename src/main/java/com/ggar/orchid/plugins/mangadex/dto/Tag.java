package com.ggar.orchid.plugins.mangadex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Tag {
    @JsonProperty("id") private String id;
    @JsonProperty("type") private String type; 
    @JsonProperty("attributes") private TagAttributes attributes;
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getType() { return type; } public void setType(String type) { this.type = type; }
    public TagAttributes getAttributes() { return attributes; } public void setAttributes(TagAttributes attributes) { this.attributes = attributes; }
}
