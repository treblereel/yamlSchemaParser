package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

class NodeFactory {

  static HasType resolveType(SchemaDefinition schema, JsonNode value) {
    if (value.get("type") != null) {
      String type = value.get("type").asText();
      return switch (type) {
        case "string" -> new StringType(value);
        case "integer" -> new IntegerType(value);
        case "number" -> new NumberType(value);
        case "boolean" -> new BooleanType(value);
        case "object" -> new ObjectType(schema, value);
        case "array" -> new ArrayType(schema, value);
        default -> throw new IllegalArgumentException("Type not supported: " + type);
      };
    } else if(value.has("allOf")) {
      return new AllOfType(schema, value);
    } else if(value.has("$ref")) {
      return new RefType(schema, value.get("$ref").asText());
    } else if(value.getNodeType().equals(JsonNodeType.OBJECT)) {
      return new ObjectType(schema, value);
    } else {
      throw new IllegalArgumentException("Type not supported: " + value.getNodeType());
    }
  }
}
