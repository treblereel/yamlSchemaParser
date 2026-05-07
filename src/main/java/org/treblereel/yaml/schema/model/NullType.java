package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Map;
import java.util.Optional;

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

  /**
   * Returns the title of this null type.
   *
   * @return the title if present
   */
  public Optional<String> title() {
    return Optional.ofNullable(node.get("title")).map(JsonNode::asText);
  }

  /**
   * Returns the description of this null type.
   *
   * @return the description if present
   */
  public Optional<String> description() {
    return Optional.ofNullable(node.get("description")).map(JsonNode::asText);
  }

  /**
   * Returns all OpenAPI extension metadata (x- prefixed properties).
   * <p>
   * NullType has no JsonNode storage for extensions, so this always returns an empty map.
   * </p>
   *
   * @return empty Map
   */
  @Override
  public Map<String, JsonNode> getExtensions() {
    return Map.of();  // Always empty - NullType has no JsonNode storage
  }

  /**
   * Returns a specific OpenAPI extension metadata value.
   * <p>
   * NullType has no JsonNode storage for extensions, so this always returns empty.
   * </p>
   *
   * @param key extension key (must start with "x-")
   * @return always empty - extensions not supported for NullType
   */
  @Override
  public Optional<JsonNode> getExtension(String key) {
    return Optional.empty();  // Always absent
  }
}
