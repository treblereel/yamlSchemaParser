package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public record AdditionalProperties(JsonNode node, SchemaDefinition schema) {

  public boolean isAllowed() {
    return !node.isBoolean() || node.asBoolean();
  }

  public Optional<HasType> getType() {
    if (node.isBoolean()) {
      return Optional.empty();
    }
    return Optional.of(NodeFactory.resolveType(schema, node));
  }
}
