package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

public record RefType(SchemaDefinition schema, String ref) implements HasType {


  public ObjectType refType() {
    if (ref.startsWith("#/$defs/")) {
      String elementName = ref.substring(8);
      JsonNode node = schema.node().get("$defs").get(elementName);
      return new ObjectType(schema, node);

    }
    throw new IllegalArgumentException("Only local $defs references are supported: " + ref);
  }
}
