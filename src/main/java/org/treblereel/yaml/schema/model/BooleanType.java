package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public record BooleanType(JsonNode node) implements HasType {

  public Optional<Boolean> constValue() {
    return Optional.ofNullable(node.get("const"))
            .filter(JsonNode::isBoolean)
            .map(JsonNode::booleanValue);
  }

  public Optional<Boolean> defaultValue() {
    return Optional.ofNullable(node.get("default"))
            .filter(JsonNode::isBoolean)
            .map(JsonNode::booleanValue);
  }

  public Optional<String> title() {
    return Optional.ofNullable(node.get("title")).map(JsonNode::asText);
  }

  public Optional<String> description() {
    return Optional.ofNullable(node.get("description")).map(JsonNode::asText);
  }
}
