package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;

public record SimpleType(JsonNode node) implements HasType {


  public String getType() {
    return node.has("type") ? node.get("type").asText() : null;
  }

  public String getTitle() {
    return node.has("title") ? node.get("title").asText() : null;
  }

  public String getDescription() {
    return node.has("description") ? node.get("description").asText() : null;
  }

  public int getMinLength() {
    return node.has("minLength") ? node.get("minLength").asInt() : -1;
  }

  public int getMaxLength() {
    return node.has("maxLength") ? node.get("maxLength").asInt() : -1;
  }

  public String getPattern() {
    return node.has("pattern") ? node.get("pattern").asText() : null;
  }

  public String getFormat() {
    return node.has("format") ? node.get("format").asText() : null;
  }

  public List<String> getExamples() {
    List<String> examples = new ArrayList<>();
    if (node.has("examples")) {
      for (JsonNode example : node.get("examples")) {
        examples.add(example.asText());
      }
    }
    return examples;
  }

  public boolean readOnly() {
    return node.has("readOnly") && node.get("readOnly").asBoolean();
  }

  public boolean writeOnly() {
    return node.has("writeOnly") && node.get("writeOnly").asBoolean();
  }

  public List<String> enumValues() {
    List<String> enumValues = new ArrayList<>();
    if (node.has("enum")) {
      for (JsonNode enumValue : node.get("enum")) {
        enumValues.add(enumValue.asText());
      }
    }
    return enumValues;
  }

}
