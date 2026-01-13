package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public record AllOfType(SchemaDefinition schema, JsonNode node) implements HasType {

  public List<HasType> getAllOf() {
    List<HasType> allOfTypes = new ArrayList<>();
    for (JsonNode allOfNode : node.get("allOf")) {
      allOfTypes.add(NodeFactory.resolveType(schema, allOfNode));
    }
    return allOfTypes;
  }
}
