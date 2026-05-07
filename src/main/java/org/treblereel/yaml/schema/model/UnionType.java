package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public record UnionType(
    SchemaDefinition schema,
    JsonNode node,
    List<String> types
) implements HasType {

    public UnionType(SchemaDefinition schema, JsonNode node) {
        this(schema, node, extractTypes(node));
    }

    private static List<String> extractTypes(JsonNode node) {
        JsonNode typeNode = node.get("type");
        if (typeNode == null || !typeNode.isArray()) {
            throw new IllegalArgumentException("Union type must have array type field");
        }

        List<String> types = new ArrayList<>();
        typeNode.forEach(t -> types.add(t.asText()));

        if (types.isEmpty()) {
            throw new IllegalArgumentException("Union type must contain at least one type");
        }

        return List.copyOf(types);
    }

    public List<String> getTypes() {
        return types;
    }
}
