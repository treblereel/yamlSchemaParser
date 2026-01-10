package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

public record ArrayType(SchemaDefinition schemaDefinition, JsonNode node) implements HasType {

  public HasType getItems() {
    JsonNode itemsNode = node.get("items");
    return NodeFactory.resolveType(schemaDefinition, itemsNode);
  }

  public Integer getMinItems() {
    if (node.get("minItems") != null) {
      return node.get("minItems").asInt();
    }
    return null;
  }

  public Integer getMaxItems() {
    if (node.get("maxItems") != null) {
      return node.get("maxItems").asInt();
    }
    return null;
  }

  public Boolean isUniqueItems() {
    if (node.get("uniqueItems") != null) {
      return node.get("uniqueItems").asBoolean();
    }
    return null;
  }

}
