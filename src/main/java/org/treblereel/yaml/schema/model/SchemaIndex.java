package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Index of {@code $anchor} and {@code $id} values within a schema document.
 * Built by recursively walking the JSON node tree once, then used for
 * O(1) lookups during {@code $ref} resolution.
 */
class SchemaIndex {

  private final Map<String, JsonNode> anchors = new HashMap<>();
  private final Map<String, JsonNode> ids = new HashMap<>();

  SchemaIndex(JsonNode root) {
    walk(root);
  }

  Optional<JsonNode> findByAnchor(String anchor) {
    return Optional.ofNullable(anchors.get(anchor));
  }

  Optional<JsonNode> findById(String id) {
    return Optional.ofNullable(ids.get(id));
  }

  private void walk(JsonNode node) {
    if (node == null || !node.isObject()) {
      return;
    }

    JsonNode anchorNode = node.get("$anchor");
    if (anchorNode != null && anchorNode.isTextual()) {
      anchors.put(anchorNode.asText(), node);
    }

    JsonNode idNode = node.get("$id");
    if (idNode != null && idNode.isTextual()) {
      ids.put(idNode.asText(), node);
    }

    walkObject(node, "properties");
    walkObject(node, "$defs");
    walkObject(node, "patternProperties");
    walkObject(node, "dependentSchemas");

    walkArray(node, "allOf");
    walkArray(node, "anyOf");
    walkArray(node, "oneOf");
    walkArray(node, "prefixItems");

    walkChild(node, "items");
    walkChild(node, "additionalProperties");
    walkChild(node, "not");
    walkChild(node, "if");
    walkChild(node, "then");
    walkChild(node, "else");
    walkChild(node, "contains");
    walkChild(node, "unevaluatedItems");
    walkChild(node, "unevaluatedProperties");
    walkChild(node, "propertyNames");
  }

  private void walkObject(JsonNode parent, String fieldName) {
    JsonNode obj = parent.get(fieldName);
    if (obj != null && obj.isObject()) {
      obj.fields().forEachRemaining(entry -> walk(entry.getValue()));
    }
  }

  private void walkArray(JsonNode parent, String fieldName) {
    JsonNode arr = parent.get(fieldName);
    if (arr != null && arr.isArray()) {
      arr.forEach(this::walk);
    }
  }

  private void walkChild(JsonNode parent, String fieldName) {
    JsonNode child = parent.get(fieldName);
    if (child != null && child.isObject()) {
      walk(child);
    }
  }
}
