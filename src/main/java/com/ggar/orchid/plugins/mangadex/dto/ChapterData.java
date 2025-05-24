package com.ggar.orchid.plugins.mangadex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChapterData { 
    @JsonProperty("id") private String id;
    @JsonProperty("type") private String type; 
    @JsonProperty("attributes") private ChapterAttributes attributes;
    @JsonProperty("relationships") private List<Relationship> relationships;
    public String getId() { return id; } public void setId(String id) { this.id = id; }
    public String getType() { return type; } public void setType(String type) { this.type = type; }
    public ChapterAttributes getAttributes() { return attributes; } public void setAttributes(ChapterAttributes attributes) { this.attributes = attributes; }
    public List<Relationship> getRelationships() { return relationships; } public void setRelationships(List<Relationship> relationships) { this.relationships = relationships; }
}
