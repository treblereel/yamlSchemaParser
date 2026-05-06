package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

/**
 * Represents a boolean type schema.
 * <p>
 * Provides access to boolean-specific constraints including const values and default values.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * BooleanType activeType = (BooleanType) objectType.properties().get().get("active");
 * Optional<Boolean> defaultValue = activeType.defaultValue(); // false
 * Optional<Boolean> constValue = activeType.constValue(); // true (if fixed value)
 * }</pre>
 * </p>
 *
 * @param node the JSON node representing this boolean type
 * @author Dmitrii Tikhomirov
 */
public record BooleanType(JsonNode node) implements HasType {

  /**
   * Returns the const value constraint.
   * <p>
   * The boolean value must exactly match this constant value.
   * </p>
   *
   * @return the const value if specified
   */
  public Optional<Boolean> constValue() {
    return Optional.ofNullable(node.get("const"))
            .filter(JsonNode::isBoolean)
            .map(JsonNode::booleanValue);
  }

  /**
   * Returns the default value.
   *
   * @return the default boolean value if specified
   */
  public Optional<Boolean> defaultValue() {
    return Optional.ofNullable(node.get("default"))
            .filter(JsonNode::isBoolean)
            .map(JsonNode::booleanValue);
  }

  /**
   * Returns the title of this boolean type.
   *
   * @return the title if present
   */
  public Optional<String> title() {
    return Optional.ofNullable(node.get("title")).map(JsonNode::asText);
  }

  /**
   * Returns the description of this boolean type.
   *
   * @return the description if present
   */
  public Optional<String> description() {
    return Optional.ofNullable(node.get("description")).map(JsonNode::asText);
  }
}
