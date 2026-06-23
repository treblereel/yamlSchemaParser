package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a union type schema (type: [string, null]).
 * <p>
 * Union types allow a value to be one of multiple types specified in an array.
 * This is commonly used for nullable types (e.g., [string, null]) or multi-type
 * fields (e.g., [string, number]).
 * </p>
 *
 * @param schema the parent schema definition
 * @param node the JSON node representing this union type
 * @param types the list of type names in the union
 * @author Dmitrii Tikhomirov
 */
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
            throw new SchemaParseException("Union type must have array type field");
        }

        final List<String> types = new ArrayList<>();
        typeNode.forEach(t -> types.add(t.asText()));

        if (types.isEmpty()) {
            throw new SchemaParseException("Union type must contain at least one type");
        }

        return List.copyOf(types);
    }

    public boolean isNullable() {
        return types.contains("null");
    }

    public List<HasType> getResolvedTypes() {
        return types.stream()
            .map(this::createTypeNode)
            .map(typeNode -> NodeFactory.resolveType(schema, typeNode))
            .toList();
    }

    private JsonNode createTypeNode(String typeName) {
        ObjectNode syntheticNode = JsonNodeFactory.instance.objectNode();
        syntheticNode.put("type", typeName);
        return syntheticNode;
    }

    @Override
    public JsonNode getRawNode() {
        return node;
    }

    @Override
    public SchemaDefinition getSchema() {
        return schema;
    }
}
