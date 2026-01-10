package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public record Applicators(JsonNode node) {

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

  public List<List<String>> getAnyOf() {
    List<List<String>> anyOf = new ArrayList<>();
    if (hasAnyOf()) {
      for (JsonNode req : node.get("anyOf")) {
        List<String> res = new ArrayList<>();
        for (JsonNode r : req.get("required")) {
          res.add(r.asText());
        }
        anyOf.add(res);
      }
    }
    return anyOf;
  }

  public boolean hasAllOf() {
    return node.get("allOf") != null;
  }

  public List<List<String>> getAllOf() {
    List<List<String>> allOf = new ArrayList<>();
    if (hasAllOf()) {
      for (JsonNode req : node.get("allOf")) {
        List<String> res = new ArrayList<>();
        for (JsonNode r : req.get("required")) {
          res.add(r.asText());
        }
        allOf.add(res);
      }
    }
    return allOf;
  }
}
