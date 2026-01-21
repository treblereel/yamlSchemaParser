package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import java.util.Optional;

public record ArrayType(SchemaDefinition schemaDefinition, JsonNode node) implements HasType {

  public ArrayItemType[] getItems() {
    JsonNode itemsNode = node.get("items");
    if (itemsNode.getNodeType().equals(JsonNodeType.ARRAY)) {
      ArrayItemType[] arrayItemTypes = new ArrayItemType[itemsNode.size()];
      for (int i = 0; i < itemsNode.size(); i++) {
        arrayItemTypes[i] = new ArrayItemType(schemaDefinition, itemsNode.get(i));
      }
      return arrayItemTypes;
    }
    return new ArrayItemType[]{new ArrayItemType(schemaDefinition, itemsNode)};
  }

  public Optional<Integer> minItems() {
    return Optional.ofNullable(node.get("minItems"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  public Optional<Integer> maxItems() {
    return Optional.ofNullable(node.get("maxItems"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  public Optional<Boolean> uniqueItems() {
    return Optional.ofNullable(node.get("uniqueItems"))
            .filter(JsonNode::isBoolean)
            .map(JsonNode::booleanValue);
  }

  public Optional<Boolean> additionalItems() {
    return Optional.ofNullable(node.get("additionalItems"))
            .filter(JsonNode::isBoolean)
            .map(JsonNode::booleanValue);
  }

}
