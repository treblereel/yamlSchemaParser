package org.treblereel.yaml.schema.model;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Represents a number type schema (floating-point numbers).
 * <p>
 * Provides access to number-specific constraints including range (minimum, maximum),
 * exclusive range, multipleOf constraint, enumeration values, const values, and default values.
 * </p>
 * <p>
 * Example usage:
 * <pre>{@code
 * NumberType priceType = (NumberType) objectType.properties().get().get("price");
 * Optional<Double> min = priceType.minimum(); // 0.0
 * Optional<Double> max = priceType.maximum(); // 999999.99
 * Optional<Double> multipleOf = priceType.multipleOf(); // 0.01 (for currency)
 * }</pre>
 * </p>
 *
 * @param schema the parent schema definition
 * @param node the JSON node representing this number type
 * @author Dmitrii Tikhomirov
 */
public record NumberType(SchemaDefinition schema, JsonNode node) implements HasType {

  /**
   * Returns the minimum value constraint (inclusive).
   *
   * @return the minimum number value if specified
   */
  public Optional<Double> minimum() {
    return Optional.ofNullable(node.get("minimum"))
            .filter(JsonNode::isNumber)
            .map(JsonNode::doubleValue);
  }

  /**
   * Returns the maximum value constraint (inclusive).
   *
   * @return the maximum number value if specified
   */
  public Optional<Double> maximum() {
    return Optional.ofNullable(node.get("maximum"))
            .filter(JsonNode::isNumber)
            .map(JsonNode::doubleValue);
  }

  /**
   * Returns the exclusive minimum value constraint.
   * <p>
   * The value must be greater than (not equal to) this value.
   * </p>
   *
   * @return the exclusive minimum number value if specified
   */
  public Optional<Double> exclusiveMinimum() {
    return Optional.ofNullable(node.get("exclusiveMinimum"))
            .filter(JsonNode::isNumber)
            .map(JsonNode::doubleValue);
  }

  /**
   * Returns the exclusive maximum value constraint.
   * <p>
   * The value must be less than (not equal to) this value.
   * </p>
   *
   * @return the exclusive maximum number value if specified
   */
  public Optional<Double> exclusiveMaximum() {
    return Optional.ofNullable(node.get("exclusiveMaximum"))
            .filter(JsonNode::isNumber)
            .map(JsonNode::doubleValue);
  }

  /**
   * Returns the multipleOf constraint.
   * <p>
   * The number value must be a multiple of this value.
   * Commonly used for currency (e.g., 0.01 for cent precision).
   * </p>
   *
   * @return the multipleOf value if specified
   */
  public Optional<Double> multipleOf() {
    return Optional.ofNullable(node.get("multipleOf"))
            .filter(JsonNode::isNumber)
            .map(JsonNode::doubleValue);
  }

  /**
   * Returns the enumeration values constraint.
   * <p>
   * The number value must be one of the values in this list.
   * </p>
   *
   * @return list of allowed enum values if specified
   */
  public Optional<List<Double>> enumValues() {
    return Optional.ofNullable(node.get("enum"))
            .filter(JsonNode::isArray)
            .map(array -> {
              List<Double> values = new ArrayList<>();
              array.forEach(v -> values.add(v.doubleValue()));
              return values;
            });
  }

  /**
   * Returns the const value constraint.
   * <p>
   * The number value must exactly match this constant value.
   * </p>
   *
   * @return the const value if specified
   */
  public Optional<Double> constValue() {
    return Optional.ofNullable(node.get("const"))
            .filter(JsonNode::isNumber)
            .map(JsonNode::doubleValue);
  }

  /**
   * Returns the default value.
   *
   * @return the default number value if specified
   */
  public Optional<Double> defaultValue() {
    return Optional.ofNullable(node.get("default"))
            .filter(JsonNode::isNumber)
            .map(JsonNode::doubleValue);
  }

  /**
   * Returns the title of this number type.
   *
   * @return the title if present
   */
  public Optional<String> title() {
    return Optional.ofNullable(node.get("title")).map(JsonNode::asText);
  }

  /**
   * Returns the description of this number type.
   *
   * @return the description if present
   */
  public Optional<String> description() {
    return Optional.ofNullable(node.get("description")).map(JsonNode::asText);
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
   * The number value must NOT validate against this schema.
   * </p>
   *
   * @return the not schema if present
   */
  public Optional<HasType> not() {
    return Optional.ofNullable(node.get("not"))
            .map(n -> NodeFactory.resolveType(schema, n));
  }
}
