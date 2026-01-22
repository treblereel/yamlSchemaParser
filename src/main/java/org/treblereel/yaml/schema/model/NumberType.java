package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record NumberType(JsonNode node) implements HasType {

  public Optional<Double> minimum() {
    return Optional.ofNullable(node.get("minimum"))
            .filter(JsonNode::isNumber)
            .map(JsonNode::doubleValue);
  }

  public Optional<Double> maximum() {
    return Optional.ofNullable(node.get("maximum"))
            .filter(JsonNode::isNumber)
            .map(JsonNode::doubleValue);
  }

  public Optional<Double> exclusiveMinimum() {
    return Optional.ofNullable(node.get("exclusiveMinimum"))
            .filter(JsonNode::isNumber)
            .map(JsonNode::doubleValue);
  }

  public Optional<Double> exclusiveMaximum() {
    return Optional.ofNullable(node.get("exclusiveMaximum"))
            .filter(JsonNode::isNumber)
            .map(JsonNode::doubleValue);
  }

  public Optional<Double> multipleOf() {
    return Optional.ofNullable(node.get("multipleOf"))
            .filter(JsonNode::isNumber)
            .map(JsonNode::doubleValue);
  }

  public Optional<List<Double>> enumValues() {
    return Optional.ofNullable(node.get("enum"))
            .filter(JsonNode::isArray)
            .map(array -> {
              List<Double> values = new ArrayList<>();
              array.forEach(v -> values.add(v.doubleValue()));
              return values;
            });
  }

  public Optional<Double> constValue() {
    return Optional.ofNullable(node.get("const"))
            .filter(JsonNode::isNumber)
            .map(JsonNode::doubleValue);
  }

  public Optional<Double> defaultValue() {
    return Optional.ofNullable(node.get("default"))
            .filter(JsonNode::isNumber)
            .map(JsonNode::doubleValue);
  }

  public Optional<String> title() {
    return Optional.ofNullable(node.get("title")).map(JsonNode::asText);
  }

  public Optional<String> description() {
    return Optional.ofNullable(node.get("description")).map(JsonNode::asText);
  }
}
