package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Marker interface for all schema type representations.
 * <p>
 * This interface is implemented by all type classes representing JSON Schema types:
 * </p>
 * <ul>
 *   <li>{@link ObjectType} - object schemas with properties</li>
 *   <li>{@link ArrayType} - array schemas with items</li>
 *   <li>{@link StringType} - string schemas with constraints</li>
 *   <li>{@link IntegerType} - integer schemas with range constraints</li>
 *   <li>{@link NumberType} - number schemas with range constraints</li>
 *   <li>{@link BooleanType} - boolean schemas</li>
 *   <li>{@link NullType} - null type schemas</li>
 *   <li>{@link RefType} - $ref references to other schema parts</li>
 *   <li>{@link AllOfType} - allOf combinator schemas</li>
 *   <li>{@link UnionType} - union type schemas (type: [string, null])</li>
 * </ul>
 *
 * <p><strong>Extension Metadata (v2.1):</strong></p>
 * <p>
 * All types support OpenAPI extension metadata (x- prefixed properties).
 * Default implementations delegate to {@link #getRawNode()}.
 * </p>
 *
 * @author Dmitrii Tikhomirov
 */
public interface HasType {

    /**
     * Returns the raw JsonNode for this type.
     * Used by default extension metadata methods.
     *
     * @return JsonNode or null if not applicable (e.g., NullType)
     */
    default JsonNode getRawNode() {
        return null;
    }

    /**
     * Returns all OpenAPI extension metadata (x- prefixed properties).
     *
     * @return Map of extension key → JsonNode value, empty if none
     */
    default Map<String, JsonNode> getExtensions() {
        Map<String, JsonNode> extensions = new LinkedHashMap<>();
        JsonNode node = getRawNode();

        if (node != null) {
            node.fields().forEachRemaining(entry -> {
                if (entry.getKey().startsWith("x-")) {
                    extensions.put(entry.getKey(), entry.getValue());
                }
            });
        }

        return extensions;
    }

    /**
     * Returns a specific OpenAPI extension metadata value.
     *
     * @param key extension key (must start with "x-")
     * @return Optional JsonNode value, empty if key doesn't start with "x-" or not found
     */
    default Optional<JsonNode> getExtension(String key) {
        if (key == null || !key.startsWith("x-")) {
            return Optional.empty();
        }

        JsonNode node = getRawNode();
        return node == null ? Optional.empty() : Optional.ofNullable(node.get(key));
    }
}
