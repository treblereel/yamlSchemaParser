package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents an integer type schema.
 * <p>
 * Provides access to integer-specific constraints including range (minimum, maximum),
 * exclusive range, enumeration values, const values, and default values.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * IntegerType portType = (IntegerType) objectType.properties().get().get("port");
 * Optional<Integer> min = portType.minimum(); // 0
 * Optional<Integer> max = portType.maximum(); // 65535
 * }</pre>
 * </p>
 *
 * @param schema the parent schema definition
 * @param node the JSON node representing this integer type
 * @author Dmitrii Tikhomirov
 */
public record IntegerType(SchemaDefinition schema, JsonNode node) implements HasType {

  /**
   * Returns the minimum value constraint (inclusive).
   *
   * @return the minimum integer value if specified
   */
  public Optional<Integer> minimum() {
    return Optional.ofNullable(node.get("minimum"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the maximum value constraint (inclusive).
   *
   * @return the maximum integer value if specified
   */
  public Optional<Integer> maximum() {
    return Optional.ofNullable(node.get("maximum"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the exclusive minimum value constraint.
   * <p>
   * The value must be greater than (not equal to) this value.
   * </p>
   *
   * @return the exclusive minimum integer value if specified
   */
  public Optional<Integer> exclusiveMinimum() {
    return Optional.ofNullable(node.get("exclusiveMinimum"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the exclusive maximum value constraint.
   * <p>
   * The value must be less than (not equal to) this value.
   * </p>
   *
   * @return the exclusive maximum integer value if specified
   */
  public Optional<Integer> exclusiveMaximum() {
    return Optional.ofNullable(node.get("exclusiveMaximum"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the enumeration values constraint.
   * <p>
   * The integer value must be one of the values in this list.
   * </p>
   *
   * @return list of allowed enum values if specified
   */
  public Optional<List<Integer>> enumValues() {
    return Optional.ofNullable(node.get("enum"))
            .filter(JsonNode::isArray)
            .map(array -> {
              List<Integer> values = new ArrayList<>();
              array.forEach(v -> values.add(v.intValue()));
              return values;
            });
  }

  /**
   * Returns the const value constraint.
   * <p>
   * The integer value must exactly match this constant value.
   * </p>
   *
   * @return the const value if specified
   */
  public Optional<Integer> constValue() {
    return Optional.ofNullable(node.get("const"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the default value.
   *
   * @return the default integer value if specified
   */
  public Optional<Integer> defaultValue() {
    return Optional.ofNullable(node.get("default"))
            .filter(JsonNode::isInt)
            .map(JsonNode::intValue);
  }

  /**
   * Returns the title of this integer type.
   *
   * @return the title if present
   */
  public Optional<String> title() {
    return Optional.ofNullable(node.get("title")).map(JsonNode::asText);
  }

  /**
   * Returns the description of this integer type.
   *
   * @return the description if present
   */
  public Optional<String> description() {
    return Optional.ofNullable(node.get("description")).map(JsonNode::asText);
  }

  /**
   * Returns the not combinator schema.
   * <p>
   * The integer value must NOT validate against this schema.
   * </p>
   *
   * @return the not schema if present
   */
  public Optional<HasType> not() {
    return Optional.ofNullable(node.get("not"))
            .map(n -> NodeFactory.resolveType(schema, n));
  }
}
