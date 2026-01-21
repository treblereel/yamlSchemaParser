package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public record AdditionalProperties(JsonNode node, SchemaDefinition schema) {

  public boolean isAllowed() {
    if (node.isBoolean()) {
      return node.asBoolean();
    }
    return true; // type schema means allowed
  }

  public Optional<HasType> getType() {
    if (node.isBoolean()) {
      return Optional.empty();
    }
    return Optional.of(NodeFactory.resolveType(schema, node));
  }
}
