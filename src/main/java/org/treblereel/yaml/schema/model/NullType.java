package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Represents a null type schema.
 * <p>
 * The null type is used in JSON Schema to indicate that a value must be null.
 * It's commonly used in combination with other types (e.g., ["string", "null"] for nullable strings).
 * </p>
 *
 * @param node the JSON node representing this null type
 * @author Dmitrii Tikhomirov
 */
public record NullType(JsonNode node) implements HasType {

  @Override
  public JsonNode getRawNode() {
    return node;
  }
}
