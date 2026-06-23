package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
     * Used by default metadata methods.
     *
     * @return JsonNode or null if not applicable
     */
    default JsonNode getRawNode() {
        return null;
    }

    /**
     * Returns the schema definition this type belongs to.
     * Used by default methods that need to resolve nested schemas.
     *
     * @return SchemaDefinition or null if not applicable
     */
    default SchemaDefinition getSchema() {
        return null;
    }

    default Optional<String> title() {
        JsonNode node = getRawNode();
        if (node == null) return Optional.empty();
        return Optional.ofNullable(node.get("title")).map(JsonNode::asText);
    }

    default Optional<String> description() {
        JsonNode node = getRawNode();
        if (node == null) return Optional.empty();
        return Optional.ofNullable(node.get("description")).map(JsonNode::asText);
    }

    default Optional<Boolean> deprecated() {
        JsonNode node = getRawNode();
        if (node == null) return Optional.empty();
        return Optional.ofNullable(node.get("deprecated"))
                .filter(JsonNode::isBoolean)
                .map(JsonNode::booleanValue);
    }

    default Optional<Boolean> readOnly() {
        JsonNode node = getRawNode();
        if (node == null) return Optional.empty();
        return Optional.ofNullable(node.get("readOnly"))
                .filter(JsonNode::isBoolean)
                .map(JsonNode::booleanValue);
    }

    default Optional<Boolean> writeOnly() {
        JsonNode node = getRawNode();
        if (node == null) return Optional.empty();
        return Optional.ofNullable(node.get("writeOnly"))
                .filter(JsonNode::isBoolean)
                .map(JsonNode::booleanValue);
    }

    default Optional<List<JsonNode>> examples() {
        JsonNode node = getRawNode();
        if (node == null) return Optional.empty();
        return Optional.ofNullable(node.get("examples"))
                .filter(JsonNode::isArray)
                .map(examplesNode -> {
                    List<JsonNode> result = new ArrayList<>();
                    examplesNode.forEach(result::add);
                    return result;
                });
    }

    default Optional<HasType> not() {
        JsonNode node = getRawNode();
        SchemaDefinition schema = getSchema();
        if (node == null || schema == null) return Optional.empty();
        return Optional.ofNullable(node.get("not"))
                .map(n -> NodeFactory.resolveType(schema, n));
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
