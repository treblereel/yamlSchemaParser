package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AdditionalProperties {

  private final JsonNode node;

  public AdditionalProperties(JsonNode node) {
    this.node = node;
  }

  public boolean isBoolean() {
    return node.isBoolean();
  }

  public boolean asBoolean() {
    return node.asBoolean();
  }

  public boolean isRef() {
    return node.has("$ref");
  }

  public String getRef() {
    return node.get("$ref").asText();
  }

  public boolean isInline() {
    return node.isObject() && node.has("type");
  }

  public boolean isInlineArray() {
    return node.isObject() && node.has("type") && node.get("type").isArray();
  }

  public String getType() {
    return node.get("type").asText();
  }

  public List<String> getTypeAsArray() {
    List<String> result = new ArrayList<>();
    Iterator<JsonNode> iter = node.get("type").elements();
    while (iter.hasNext()) {
      JsonNode node = iter.next();
      result.add(node.asText());
    }
    return result;
  }

  public boolean isOneOf() {
    return node.has("oneOf");
  }

  public boolean isAnyOf() {
    return node.has("anyOf");
  }

  public Applicators getApplicators() {
    return new Applicators(node);
  }

  public boolean isObject() {
    return node.has("type") && "object".equals(node.get("type").asText());
  }

  public ObjectType asObjectType(SchemaDefinition schema) {
    return new ObjectType(schema, node);
  }

}
