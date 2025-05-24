package com.ggar.orchid.plugins.mangadex.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Represents a generic relationship between entities in the MangaDex API.
 * The actual structure of 'attributes' depends on the 'type' of relationship
 * and whether the related entity's attributes are included via 'includes[]' parameter.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Relationship {
    private static final Logger logger = Logger.getLogger(Relationship.class.getName());

    @JsonProperty("id")
    private String id;

    @JsonProperty("type")
    private String type; 

    @JsonProperty("related") 
    private String related; 

    @JsonProperty("attributes")
    private JsonNode attributesRaw; 
    
    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getRelated() {
        return related;
    }

    /**
     * Gets the raw JsonNode of the attributes. Can be null.
     * @return The JsonNode for attributes.
     */
    public JsonNode getAttributesRaw() {
        return attributesRaw;
    }

    /**
     * Attempts to convert the raw attributes JsonNode to a specific DTO class.
     * This is useful when 'includes[]' is used in the API request, and the attributes
     * field contains the full data of the related entity.
     *
     * @param attributeClass The class of the target DTO (e.g., AuthorAttributes.class, CoverAttributes.class).
     * @param objectMapper   An ObjectMapper instance to perform the conversion.
     * @param <A>            The type of the target DTO.
     * @return An Optional containing the deserialized attributes object, or empty if conversion fails or attributes are null.
     */
    public <A> Optional<A> getTypedAttributes(Class<A> attributeClass, ObjectMapper objectMapper) {
        if (attributesRaw == null || attributesRaw.isNull() || attributesRaw.isMissingNode()) {
            return Optional.empty();
        }
        try {
            return Optional.of(objectMapper.treeToValue(attributesRaw, attributeClass));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to convert JsonNode attributes to " + attributeClass.getSimpleName() +
                    " for relationship type '" + type + "' and id '" + id + "'. Error: " + e.getMessage(), e);
            return Optional.empty();
        }
    }
    
    public void setId(String id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setRelated(String related) {
        this.related = related;
    }

    public void setAttributesRaw(JsonNode attributesRaw) {
        this.attributesRaw = attributesRaw;
    }
}
