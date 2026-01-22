package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

public record RefType(SchemaDefinition schema, String ref) implements HasType {

  /**
   * Resolves the reference and returns the appropriate type.
   * Can return any HasType (ObjectType, StringType, ArrayType, etc.)
   */
  public HasType resolve() {
    if (ref.startsWith("#/$defs/")) {
      String elementName = ref.substring(8);
      JsonNode node = schema.node().get("$defs").get(elementName);
      return NodeFactory.resolveType(schema, node);
    }
    throw new IllegalArgumentException("Only local $defs references are supported: " + ref);
  }
}
