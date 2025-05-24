package com.ggar.orchid.plugins.mangadex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents the attributes of a related entity, typically when not fully expanded.
 * For expanded relationships (using 'includes[]'), the 'attributes' field in the
 * {@link Relationship} object will contain a more specific structure (e.g., {@link AuthorAttributes}, {@link CoverAttributes}).
 * This class can hold common simple attributes if any, or can be considered a base/marker.
 * Given the move to JsonNode in Relationship for flexibility, this class might primarily serve
 * to hold very basic, consistently present attributes if MangaDex API had such a pattern for
 * non-expanded relationships. The OpenAPI spec often just shows "type: object" for non-expanded attributes.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RelationshipAttributes {

    @JsonProperty("name")
    private String name; // Primarily for author/artist if not fully expanded

    @JsonProperty("fileName")
    private String fileName; // Primarily for cover_art if not fully expanded

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

}