package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public record Applicators(SchemaDefinition schema, JsonNode node) {

  public boolean hasOneOf() {
    return node.get("oneOf") != null;
  }

  public List<List<String>> getOneOf() {
    List<List<String>> oneOf = new ArrayList<>();
    if (hasOneOf()) {
      for (JsonNode req : node.get("oneOf")) {
        List<String> res = new ArrayList<>();
        for (JsonNode r : req.get("required")) {
          res.add(r.asText());
        }
        oneOf.add(res);
      }
    }
    return oneOf;
  }

  public boolean hasAnyOf() {
    return node.get("anyOf") != null;
  }

  public List<HasType> getAnyOf() {
    if (node.has("anyOf")) {
      if (node.get("anyOf").isArray()) {
        List<HasType> results = new ArrayList<>();
        Iterator<JsonNode> interator = node.get("anyOf").elements();
        while (interator.hasNext()) {
          JsonNode jsonNode = interator.next();
          results.add(NodeFactory.resolveType(schema, jsonNode));
        }
        return results;
      } else {
        throw new IllegalStateException("Expected 'anyOf' to be an array");
      }
    }
    return null;
  }

  public boolean hasAllOf() {
    return node.get("allOf") != null;
  }

  public List<HasType> getAllOf() {
    if (node.has("allOf") && node.get("allOf").isArray()) {
      List<HasType> results = new ArrayList<>();
      Iterator<JsonNode> iterator = node.get("allOf").elements();
      while (iterator.hasNext()) {
        JsonNode jsonNode = iterator.next();
        results.add(NodeFactory.resolveType(schema, jsonNode));
      }
      return results;
    }

    return null;
  }
}
