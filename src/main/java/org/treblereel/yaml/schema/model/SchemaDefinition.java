package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

public record SchemaDefinition(JsonNode node) {

  public Optional<String> id() {
    return Optional.ofNullable(node.get("$id")).filter(JsonNode::isTextual).map(JsonNode::asText);
  }

  public Optional<String> schema() {
    return Optional.ofNullable(node.get("$schema")).filter(JsonNode::isTextual).map(JsonNode::asText);
  }

  public Optional<String> title() {
    return Optional.ofNullable(node.get("title")).filter(JsonNode::isTextual).map(JsonNode::asText);
  }

  public HasType model() {
    return NodeFactory.resolveType(this, node);
  }

  public Map<String, ObjectType> definitions() {
    if (node.get("$defs") == null) {
      return Collections.emptyMap();
    }
    Map<String, ObjectType> results = new HashMap<>();
    JsonNode definitionsNode = node.get("$defs");
    Iterator<Map.Entry<String, JsonNode>> iter = definitionsNode.fields();
    while (iter.hasNext()) {
      Map.Entry<String, JsonNode> entry = iter.next();
      results.put(entry.getKey(), new ObjectType(this, entry.getValue()));
    }
    return results;
  }

}
