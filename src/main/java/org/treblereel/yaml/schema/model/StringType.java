package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record StringType(JsonNode node) implements HasType {

  public Optional<String> pattern() {
    return Optional.ofNullable(node.get("pattern")).map(JsonNode::asText);
  }

  public Optional<String> format() {
    return Optional.ofNullable(node.get("format")).map(JsonNode::asText);
  }

  public Optional<Integer> minLength() {
    return Optional.ofNullable(node.get("minLength"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  public Optional<Integer> maxLength() {
    return Optional.ofNullable(node.get("maxLength"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  public Optional<List<String>> enumValues() {
    return Optional.ofNullable(node.get("enum"))
            .filter(JsonNode::isArray)
            .map(array -> {
              List<String> values = new ArrayList<>();
              array.forEach(v -> values.add(v.asText()));
              return values;
            });
  }

  public Optional<String> constValue() {
    return Optional.ofNullable(node.get("const")).map(JsonNode::asText);
  }

  public Optional<String> defaultValue() {
    return Optional.ofNullable(node.get("default")).map(JsonNode::asText);
  }

  public Optional<String> title() {
    return Optional.ofNullable(node.get("title")).map(JsonNode::asText);
  }

  public Optional<String> description() {
    return Optional.ofNullable(node.get("description")).map(JsonNode::asText);
  }
}
