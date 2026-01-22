package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record IntegerType(JsonNode node) implements HasType {

  public Optional<Integer> minimum() {
    return Optional.ofNullable(node.get("minimum"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  public Optional<Integer> maximum() {
    return Optional.ofNullable(node.get("maximum"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  public Optional<Integer> exclusiveMinimum() {
    return Optional.ofNullable(node.get("exclusiveMinimum"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  public Optional<Integer> exclusiveMaximum() {
    return Optional.ofNullable(node.get("exclusiveMaximum"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  public Optional<List<Integer>> enumValues() {
    return Optional.ofNullable(node.get("enum"))
            .filter(JsonNode::isArray)
            .map(array -> {
              List<Integer> values = new ArrayList<>();
              array.forEach(v -> values.add(v.intValue()));
              return values;
            });
  }

  public Optional<Integer> constValue() {
    return Optional.ofNullable(node.get("const"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  public Optional<Integer> defaultValue() {
    return Optional.ofNullable(node.get("default"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  public Optional<String> title() {
    return Optional.ofNullable(node.get("title")).map(JsonNode::asText);
  }

  public Optional<String> description() {
    return Optional.ofNullable(node.get("description")).map(JsonNode::asText);
  }
}
