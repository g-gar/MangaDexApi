package com.ggar.orchid.plugins.mangadex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TagAttributes {
    @JsonProperty("name") private Map<String, String> name; @JsonProperty("group") private String group;
    public String getEnglishName() { return (name != null) ? name.get("en") : null; }
    public String getGroup() { return group; } public void setGroup(String group) { this.group = group; }
}
