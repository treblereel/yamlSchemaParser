package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

class NodeFactory {

  static HasType resolveType(SchemaDefinition schema, JsonNode value) {
    if (value == null || value.isNull()) {
      return new NullType(value);
    }
    if(value.isArray()) {
        return new ArrayType(schema, value);
    }
    JsonNode node = value.get("type");
    if(node != null && node.isArray()) {
      return new ArrayType(schema, node);
    }

    if (value.get("type") != null) {
      String type = value.get("type").asText();
      return switch (type) {
        case "string" -> new StringType(value);
        case "integer" -> new IntegerType(value);
        case "number" -> new NumberType(value);
        case "boolean" -> new BooleanType(value);
        case "object" -> new ObjectType(schema, value);
        case "array" -> new ArrayType(schema, value);
        case "null" -> new NullType(value);
        default -> throw new IllegalArgumentException("Type not supported: " + value.get("type") + " ? " + type);
      };
    } else if (value.has("allOf")) {
      return new AllOfType(schema, value);
    } else if (value.has("$ref")) {
      return new RefType(schema, value.get("$ref").asText());
    } else if (value.getNodeType().equals(JsonNodeType.OBJECT)) {
      return new ObjectType(schema, value);
    } else {
      throw new IllegalArgumentException("Type not supported: " + value.getNodeType());
    }
  }
}
