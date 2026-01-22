package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record AllOfType(SchemaDefinition schema, JsonNode node) implements HasType {

  public List<HasType> getAllOf() {
    List<HasType> allOfTypes = new ArrayList<>();
    for (JsonNode allOfNode : node.get("allOf")) {
      allOfTypes.add(NodeFactory.resolveType(schema, allOfNode));
    }
    return allOfTypes;
  }

  public Optional<Boolean> unevaluatedProperties() {
    return Optional.ofNullable(node.get("unevaluatedProperties")).map(JsonNode::asBoolean);
  }

  public Optional<String> title() {
    return Optional.ofNullable(node.get("title")).map(JsonNode::asText);
  }

  public Optional<String> description() {
    return Optional.ofNullable(node.get("description")).map(JsonNode::asText);
  }
}
