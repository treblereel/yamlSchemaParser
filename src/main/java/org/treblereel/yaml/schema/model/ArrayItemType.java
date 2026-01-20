package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
import java.util.Optional;

public class ArrayItemType {

  private final Applicators applicators;
  private final JsonNode node;
  private final SchemaDefinition schemaDefinition;

  public ArrayItemType(SchemaDefinition schemaDefinition, JsonNode node) {
    this.schemaDefinition = schemaDefinition;
    this.node = node;
    this.applicators = new Applicators(schemaDefinition, node);
  }

  public HasType getType() {
    return NodeFactory.resolveType(schemaDefinition, node);
  }

  public Optional<List<HasType>> oneOf() {
    return applicators.oneOf();
  }

  public Optional<List<HasType>> anyOf() {
    return applicators.anyOf();
  }

  public Optional<List<HasType>> allOf() {
    return applicators.allOf();
  }

  public boolean isNullable() {
    JsonNode typeNode = node.get("type");
    if (typeNode != null && typeNode.isArray()) {
      for (JsonNode t : typeNode) {
        if ("null".equals(t.asText())) {
          return true;
        }
      }
    }
    return false;
  }
}
