package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

public record SchemaDefinition(JsonNode node) {

  public String getId() {
    if (node.get("$id") == null) {
      return null;
    }
    return node.get("$id").asText();
  }

  public String getSchema() {
    if (node.get("$schema") == null) {
      return null;
    }
    return node.get("$schema").asText();
  }

  public String getTitle() {
    if (node.get("title") == null) {
      return null;
    }
    return node.get("title").asText();
  }

  public ObjectType getObjectDefinition() {
    return new ObjectType(this, node);
  }

}
