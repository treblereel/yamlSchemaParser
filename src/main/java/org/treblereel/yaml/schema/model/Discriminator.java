package org.treblereel.yaml.schema.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * OpenAPI discriminator metadata for polymorphic schemas.
 *
 * <p>Used with oneOf/anyOf/allOf to specify which property determines
 * the concrete schema type and how property values map to schema references.</p>
 *
 * <p>Example:</p>
 * <pre>
 * discriminator:
 *   propertyName: petType
 *   mapping:
 *     cat: '#/components/schemas/Cat'
 *     dog: '#/components/schemas/Dog'
 * </pre>
 *
 * @param propertyName The property name that contains the discriminator value
 * @param mapping Map from discriminator value to full $ref path (e.g., "#/components/schemas/Cat")
 *
 * @author Dmitrii Tikhomirov
 */
public record Discriminator(
    String propertyName,
    Map<String, String> mapping
) {
    /**
     * Compact constructor with validation.
     *
     * @throws IllegalArgumentException if propertyName is null or blank
     */
    public Discriminator {
        if (propertyName == null || propertyName.isBlank()) {
            throw new IllegalArgumentException("Discriminator must have propertyName field");
        }

        // Defensive copy and ensure non-null
        mapping = mapping == null ? Map.of() : new LinkedHashMap<>(mapping);
    }
}
