package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.List;
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
 * @param schema the parent schema definition
 * @param node the JSON node representing this boolean type
 * @author Dmitrii Tikhomirov
 */
public record BooleanType(SchemaDefinition schema, JsonNode node) implements HasType {

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

  /**
   * Returns the deprecated flag (JSON Schema Draft 2019-09+).
   * <p>
   * When true, indicates this schema is deprecated and should not be used.
   * This is metadata only and does not affect validation.
   * </p>
   *
   * @return true if deprecated, false if not deprecated, empty if not specified
   */
  public Optional<Boolean> deprecated() {
    return Optional.ofNullable(node.get("deprecated"))
            .filter(JsonNode::isBoolean)
            .map(JsonNode::booleanValue);
  }

  /**
   * Returns the readOnly flag (JSON Schema Draft 7+, OpenAPI).
   * <p>
   * When true, indicates this property should only appear in responses,
   * not in requests. Commonly used for server-generated fields like IDs
   * and timestamps.
   * </p>
   *
   * @return true if read-only, false if not, empty if not specified
   */
  public Optional<Boolean> readOnly() {
    return Optional.ofNullable(node.get("readOnly"))
            .filter(JsonNode::isBoolean)
            .map(JsonNode::booleanValue);
  }

  /**
   * Returns the writeOnly flag (JSON Schema Draft 7+, OpenAPI).
   * <p>
   * When true, indicates this property should only appear in requests,
   * not in responses. Commonly used for sensitive fields like passwords.
   * </p>
   *
   * @return true if write-only, false if not, empty if not specified
   */
  public Optional<Boolean> writeOnly() {
    return Optional.ofNullable(node.get("writeOnly"))
            .filter(JsonNode::isBoolean)
            .map(JsonNode::booleanValue);
  }

  /**
   * Returns the examples array (JSON Schema Draft 6+).
   * <p>
   * Provides example values for documentation purposes. These are metadata
   * only and do not affect validation.
   * </p>
   *
   * @return list of example values if specified
   */
  public Optional<List<JsonNode>> examples() {
    return Optional.ofNullable(node.get("examples"))
            .filter(JsonNode::isArray)
            .map(examplesNode -> {
              List<JsonNode> examples = new java.util.ArrayList<>();
              examplesNode.forEach(examples::add);
              return examples;
            });
  }

  /**
   * Returns the not combinator schema.
   * <p>
   * The boolean value must NOT validate against this schema.
   * </p>
   *
   * @return the not schema if present
   */
  public Optional<HasType> not() {
    return Optional.ofNullable(node.get("not"))
            .map(n -> NodeFactory.resolveType(schema, n));
  }
}
